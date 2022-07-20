package ca.uhn.fhir.jpa.starter.mqtt;

public class MqttFhirMessage {
	private final String topic;
	private final byte[] payload;
	private final int qos;
	private final boolean retained;

	public MqttFhirMessage(String topic, byte[] payload, int qos, boolean retained) {
		this.topic = topic;
		this.payload = payload;
		this.qos = qos;
		this.retained = retained;
	}

	public String getTopic() {
		return topic;
	}

	public byte[] getPayload() {
		return payload;
	}

	public int getQos() {
		return qos;
	}

	public boolean isRetained() {
		return retained;
	}
}
