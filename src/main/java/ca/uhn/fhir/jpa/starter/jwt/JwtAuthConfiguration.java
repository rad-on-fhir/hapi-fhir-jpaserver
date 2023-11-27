package ca.uhn.fhir.jpa.starter.jwt;

import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.jpa.starter.jwt.jwks.JwksConfig;
import ca.uhn.fhir.jpa.starter.jwt.jwks.JwksValidator;
import ca.uhn.fhir.jpa.starter.jwt.key.JwtEnvironmentKeyProvider;
import ca.uhn.fhir.jpa.starter.jwt.key.JwtKeyFileProvider;
import ca.uhn.fhir.jpa.starter.jwt.key.JwtPublicKeyValidator;
import ca.uhn.fhir.jpa.starter.jwt.key.PublicKeyProvider;
import ca.uhn.fhir.jpa.starter.jwt.rules.JwtRuleBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Configuration
public class JwtAuthConfiguration {

	@Bean

	public Optional<JwtAuthInterceptor> jwtAuthInterceptor(
		@Value("${jwt.mode:disabled}") String mode,
		List<JwtRuleBuilder> ruleBuilders,
		IInterceptorService service,
		@Value("${jwt.publickeyfile:}") String publicKeyFilePath,
		Optional<JwksConfig> jwksConfig,
		ObjectMapper mapper
	) throws IOException {
		JwtValidator jwtValidator = null;
		if (mode.equalsIgnoreCase("publickey")) {
			jwtValidator = this.publicKeyValidator(publicKeyFilePath, mapper);
		} else if (mode.equalsIgnoreCase("jwks")) {
			jwtValidator = this.jwksValidator(jwksConfig, mapper);
		}
		if (jwtValidator != null) {
			JwtAuthInterceptor jwtAuthInterceptor = new JwtAuthInterceptor(jwtValidator, ruleBuilders);
			service.registerInterceptor(jwtAuthInterceptor);
			return Optional.of(jwtAuthInterceptor);
		}
		return Optional.empty();
	}

	private JwtValidator jwksValidator(Optional<JwksConfig> jwksConfig, ObjectMapper mapper) {
		if (jwksConfig.isPresent()) {
			return new JwksValidator(jwksConfig.get(), mapper);
		}
		throw new IllegalArgumentException("No JWKS Config present.");
	}

	private JwtValidator publicKeyValidator(String publicKeyFilePath, ObjectMapper mapper) {
		PublicKeyProvider publicKeyProvider = this.publicKeyProvider(publicKeyFilePath);
		return new JwtPublicKeyValidator(publicKeyProvider, mapper);
	}


	public PublicKeyProvider publicKeyProvider(@Value("${jwt.publickeyfile:}") String publicKeyFilePath) {
		final PublicKeyProvider provider;
		if (publicKeyFilePath != null && !publicKeyFilePath.isBlank()) {
			File publicKeyFile = new File(publicKeyFilePath);
			provider = new JwtKeyFileProvider(publicKeyFile);
		} else if (System.getenv().containsKey("JWT_PUBLIC_KEY")) {
			provider = new JwtEnvironmentKeyProvider("JWT_PUBLIC_KEY");
		} else {
			throw new IllegalArgumentException("Please Provide a Public Key File in application.properties under 'jwt.publickeyfile' or set ENV 'JWT_PUBLIC_KEY' with the Base64-String");
		}
		return provider;
	}
}
