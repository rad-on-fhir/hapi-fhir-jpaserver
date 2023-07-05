package ca.uhn.fhir.jpa.starter.mqtt;

import ca.uhn.fhir.rest.server.messaging.BaseResourceMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hl7.fhir.instance.model.api.IIdType;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

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
	@JsonProperty("children")
	public final List<String> children;
	@JsonProperty("createdAt")
	public final ZonedDateTime createdAt;

	public MqttEvent(BaseResourceMessage.OperationTypeEnum update, IIdType idElement, String fullUrl) {
		this(update, idElement, fullUrl, Collections.emptyList());
	}

	public MqttEvent(BaseResourceMessage.OperationTypeEnum update, IIdType idElement, String fullUrl, List<String> children) {
		this(
			update.name(),
			fullUrl,
			idElement.getResourceType(),
			idElement.getIdPart(),
			idElement.getVersionIdPart(),
			ZonedDateTime.now(),
			children
		);
	}

	@JsonCreator
	public MqttEvent(String event, String fullUrl, String resourceType, String id, String version, ZonedDateTime createdAt, List<String> children) {
		this.event = event;
		this.fullUrl = fullUrl;
		this.resourceType = resourceType;
		this.id = id;
		this.version = version;
		this.createdAt = createdAt;
		this.children = children;
	}
}
