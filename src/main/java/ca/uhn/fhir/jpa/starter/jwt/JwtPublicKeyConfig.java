package ca.uhn.fhir.jpa.starter.jwt;

import ca.uhn.fhir.jpa.starter.jwt.key.JwtEnvironmentKeyProvider;
import ca.uhn.fhir.jpa.starter.jwt.key.JwtKeyFileProvider;
import ca.uhn.fhir.jpa.starter.jwt.key.PublicKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;

@Configuration
@ConditionalOnProperty(value="jwt.disabled",havingValue="false",matchIfMissing = true)
public class JwtPublicKeyConfig {

	@Bean
	@Primary
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
