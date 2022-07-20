package ca.uhn.fhir.jpa.starter.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
	value = "mqtt.enabled",
	havingValue = "true"
)
public class MqttConfig {
	@Value("${mqtt.host}")
	String host;
	@Value("${mqtt.port:1883}")
	Integer port = 1883; /* Default port */
	@Value("${mqtt.qos:1}")
	int qos;
	@Value("${mqtt.useSSL:false}")
	Boolean useSSL;
	@Value("${mqtt.username:}")
	String userName = "your username";
	@Value("${mqtt.password:}")
	String password = "Password";
	final String TCP = "tcp://";
	final String SSL = "ssl://";

	@Bean
	public MqttConnection connection() {
		MqttConnection connection = new MqttConnection(this);

		return connection;
	}

	@Override
	public String toString() {
		return "MqttConfig{" +
			"host='" + host + '\'' +
			", port=" + port +
			", useSSL=" + useSSL +
			'}';
	}
}
