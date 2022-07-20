package ca.uhn.fhir.jpa.starter.mqtt;

import ca.uhn.fhir.rest.server.messaging.BaseResourceMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hl7.fhir.instance.model.api.IIdType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MqttEvent {

	@JsonProperty("event")
	public final String event;
	@JsonProperty("fullUrl")
	public final String fullUrl;
	@JsonProperty("resourceType")
	public final String resourceType;
	@JsonProperty("id")
	public final String id;
	@JsonProperty("version")
	public final String version;

	public MqttEvent(BaseResourceMessage.OperationTypeEnum update, IIdType idElement, String fullUrl) {
		this.event = update.name();
		this.fullUrl = fullUrl;
		this.resourceType = idElement.getResourceType();
		this.id = idElement.getIdPart();
		this.version = idElement.getVersionIdPart();
	}

	@JsonCreator
	public MqttEvent(String event, String fullUrl, String resourceType, String id, String version) {
		this.event = event;
		this.fullUrl = fullUrl;
		this.resourceType = resourceType;
		this.id = id;
		this.version = version;
	}
}