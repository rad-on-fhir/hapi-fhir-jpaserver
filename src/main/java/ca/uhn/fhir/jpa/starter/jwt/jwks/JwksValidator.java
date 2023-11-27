package ca.uhn.fhir.jpa.starter.jwt.jwks;

import ca.uhn.fhir.jpa.starter.jwt.JwtPayload;
import ca.uhn.fhir.jpa.starter.jwt.JwtValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JwksValidator implements JwtValidator {
	private final JwtConsumer jwtConsumer;
	private final ObjectMapper mapper;
	private final Logger logger = LoggerFactory.getLogger(JwksValidator.class);

	public JwksValidator(JwksConfig jwksConfig, ObjectMapper mapper) {
		this.mapper = mapper;
		HttpsJwks httpsJkws = new HttpsJwks(jwksConfig.getJwksUrl());
		HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
		JwtConsumerBuilder builder = new JwtConsumerBuilder()
			.setExpectedIssuer(jwksConfig.getIssuerUrl())
			.setVerificationKeyResolver(httpsJwksKeyResolver);
		if (jwksConfig.hasAudience()) {
			builder.setExpectedAudience(jwksConfig.getAudience());
		}
		this.logger.info("Initializing JWKS Validator with " + jwksConfig);
		this.jwtConsumer = builder.build();
	}

	@Override
	public JwtPayload parseAndValidate(String token) throws InvalidJwtException, IOException {
		try {
			JwtContext result = jwtConsumer.process(token);
			String payload = result.getJwtClaims().getRawJson();
			JsonNode node = mapper.readTree(payload);
			return JwtPayload.generateFromJsonNode(node);
		} catch (InvalidJwtException e) {
			this.logger.warn("Invalid JWT Exception for Token\n" + token + "\n", e);
			throw e;
		} catch (Exception e) {
			this.logger.error("JWT Parsing Exception for Token\n" + token + "\n", e);
			throw new IOException("Could not Parse Payload", e);
		}
	}

}
