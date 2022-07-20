package ca.uhn.fhir.jpa.starter.mqtt;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.subscription.model.ResourceModifiedMessage;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.messaging.BaseResourceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

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
	public MqttInterceptor(ObjectMapper mapper, MqttConnection connection) {
		this.mapper = mapper;
		this.connection = connection;
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
//		if (!myDaoConfig.isTriggerSubscriptionsForNonVersioningChanges()) {
//			if (theOldResource != null && theNewResource != null) {
//				String oldVersion = theOldResource.getIdElement().getVersionIdPart();
//				String newVersion = theNewResource.getIdElement().getVersionIdPart();
//				if (isNotBlank(oldVersion) && isNotBlank(newVersion) && oldVersion.equals(newVersion)) {
//					return;
//				}
//			}
//		}

		sendResourceModified(theNewResource, ResourceModifiedMessage.OperationTypeEnum.UPDATE, theRequest);
	}

	private void sendResourceModified(IBaseResource theNewResource, BaseResourceMessage.OperationTypeEnum update, RequestDetails theRequest) {
//		byte[] payload = jsonParser.encodeResourceToString(theNewResource).getBytes(StandardCharsets.UTF_8);
		try {
			logger.info("MQTT: start ");
			IIdType idElement = theNewResource.getIdElement();
			String topic = theNewResource.getIdElement().getResourceType() + "/" + theNewResource.getIdElement().getIdPart();
			String bUrl = idElement.getBaseUrl();
			String fullUrl = (bUrl==null?theRequest.getFhirServerBase():bUrl) + "/" + idElement.getResourceType() + "/" + idElement.getIdPart();
			MqttEvent event = new MqttEvent(update, idElement, fullUrl);
			byte[] payload = mapper.writeValueAsBytes(event);
			logger.warn("MQTT: sending to " + topic);
			this.connection.send(topic, payload);
			logger.warn("MQTT: DONE for " + topic);
		} catch (Exception e) {
			this.logger.warn("Error Sending Mqtt Event", e);
		}
	}


}
