package ca.uhn.fhir.jpa.starter.mqtt;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.subscription.model.ResourceModifiedMessage;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.messaging.BaseResourceMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseReference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
@Interceptor
@ConditionalOnBean(
	value = MqttConnection.class
)
public class MqttInterceptor {
	private final Logger logger = LoggerFactory.getLogger(MqttInterceptor.class);
	private final ObjectMapper mapper;

	private final MqttConnection connection;

	@Autowired
	public MqttInterceptor(ObjectMapper mapper, MqttConnection connection, IInterceptorService service) {
		this.mapper = mapper;
		this.connection = connection;
		this.logger.warn("Started MqttInterceptor");
		service.registerInterceptor(this);
	}

	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_CREATED)
	public void resourceCreated(IBaseResource theResource, RequestDetails theRequest) {
		sendResourceModified(theResource, ResourceModifiedMessage.OperationTypeEnum.CREATE, theRequest);
	}

	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_DELETED)
	public void resourceDeleted(IBaseResource theResource, RequestDetails theRequest) {
		sendResourceModified(theResource, ResourceModifiedMessage.OperationTypeEnum.DELETE, theRequest);
	}

	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_UPDATED)
	public void resourceUpdated(IBaseResource theOldResource, IBaseResource theNewResource, RequestDetails theRequest) {
		sendResourceModified(theNewResource, ResourceModifiedMessage.OperationTypeEnum.UPDATE, theRequest);
	}

	private void sendResourceModified(IBaseResource theNewResource, BaseResourceMessage.OperationTypeEnum update, RequestDetails theRequest) {
		try {
			logger.debug("MQTT: start ");
			IIdType idElement = theNewResource.getIdElement();
			String topic = theNewResource.getIdElement().getResourceType() + "/" + theNewResource.getIdElement().getIdPart();
			String bUrl = idElement.getBaseUrl();
			String fullUrl = (bUrl==null?theRequest.getFhirServerBase():bUrl) + "/" + idElement.getResourceType() + "/" + idElement.getIdPart();
			MqttEvent event = new MqttEvent(update, idElement, fullUrl);
			sendEvent(topic, event);
		} catch (Exception e) {
			this.logger.warn("Error Sending Mqtt Event", e);
		}
		if (!theNewResource.getIdElement().getResourceType().equals("Patient")) {
			this.sendPatientEvent(theNewResource, update, theRequest);
		}
	}

	private void sendEvent(String topic, MqttEvent event) throws JsonProcessingException {
		byte[] payload = mapper.writeValueAsBytes(event);
		logger.debug("MQTT: sending to " + topic);
		this.connection.send(topic, payload);
		logger.debug("MQTT: DONE for " + topic);
	}

	private void sendPatientEvent(IBaseResource theNewResource, BaseResourceMessage.OperationTypeEnum update, RequestDetails theRequest) {
		ArrayList<IBaseReference> references = new ArrayList<>();
		findBaseReferences(theNewResource, references, new HashSet<>());
		HashSet<String> patients = new HashSet<>();
		HashSet<String> ressources = new HashSet<>();
		for (IBaseReference reference : references) {
			IIdType id = reference.getReferenceElement().toVersionless();
			String topic = id.getResourceType() + "/" + id.getIdPart();
			ressources.add(topic);
			if (id.getResourceType().equals("Patient")) {
				patients.add(topic);
			}
		}
		if (patients.size()>0) {
			IIdType idElement = theNewResource.getIdElement();
			ArrayList<String> children = new ArrayList<>(ressources);
			for (String patient : patients) {
				try {
					String topic = patient + "/" + idElement.getResourceType() + "/" + idElement.getIdPart();
					String bUrl = idElement.getBaseUrl();
					String fullUrl = (bUrl == null ? theRequest.getFhirServerBase() : bUrl) + "/" + idElement.getResourceType() + "/" + idElement.getIdPart();
					MqttEvent event = new MqttEvent(update, idElement, fullUrl, children);
					sendEvent(topic, event);
				} catch (JsonProcessingException e) {
					this.logger.error("Error Sending Patient Topic", e );
				}
			}
		}
	}

	public void findBaseReferences(Object instance, List<IBaseReference> result, Set<Object> loop) {
		if (instance == null) return;
		if (result == null || loop==null) {
			throw new IllegalArgumentException("2nd & 3rd Parameter must not be null");
		}
		// Überprüfen, ob die übergebene Instanz das IBaseReference-Interface implementiert
		if (instance instanceof IBaseReference) {
			this.logger.debug("Adding Reference " + instance);
			result.add((IBaseReference) instance);
		} else if (!loop.contains(instance)) {
			loop.add(instance);
			if ( instance instanceof Collection) {
				Collection collection = (Collection) instance;
				this.logger.debug("Descending into Array " + instance + " ...");
				for (Object o : collection) {
					this.logger.debug("Descending into Array Child " + o + " ...");
					findBaseReferences(o, result, loop);
				}
			} else if (instance instanceof Map) {
				Map collection = (Map) instance;
				this.logger.debug("Descending into Map " + instance + " ...");
				for (Object o : collection.values()) {
					this.logger.debug("Descending into Map Value " + o + " ...");
					findBaseReferences(o, result, loop);
				}
			} else {
				// Durchsuche die Properties der Instanz
				Field[] fields = instance.getClass().getDeclaredFields();
				for (Field field : fields) {
					try {
						if (field.trySetAccessible()) {
							Object fieldValue = field.get(instance);
							if (fieldValue != null) {
								// Rekursiver Aufruf für non-null Properties
								this.logger.debug("Descending into Field " + field.getName() + "...");
								findBaseReferences(fieldValue, result, loop);
							} else {
								this.logger.debug("Field " + field.getName() + " is NULL");
							}
						} else {
							this.logger.debug("Field " + field.getName() + " is not accessible");
						}
					} catch (Exception e) {
						this.logger.warn("Error searching References in " + instance, e);
					}
				}
			}
		} else {
			this.logger.debug("Ignoring already seen instance " + instance);
		}
	}


}
