package ca.uhn.fhir.jpa.starter.jwt.key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConditionalOnProperty(value="jwt.mode",havingValue="publickey")
public class JwtPublicKeyConfig {

	@Bean
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
