package ca.uhn.fhir.jpa.starter.mqtt;

import ca.uhn.fhir.interceptor.api.IInterceptorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfiguration {

	@Bean
	@ConditionalOnBean(value = MqttConnection.class)
	public MqttInterceptor mqttInterceptor(ObjectMapper mapper, MqttConnection connection, IInterceptorService service) {
		MqttInterceptor mqttInterceptor = new MqttInterceptor(mapper, connection);
		service.registerInterceptor(mqttInterceptor);
		return mqttInterceptor;
	}
}
