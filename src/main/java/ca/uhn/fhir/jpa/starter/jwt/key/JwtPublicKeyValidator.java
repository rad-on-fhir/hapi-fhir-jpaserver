package ca.uhn.fhir.jpa.starter.jwt.key;

import ca.uhn.fhir.jpa.starter.jwt.JwtPayload;
import ca.uhn.fhir.jpa.starter.jwt.JwtValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.regex.Pattern;

@Component
@ConditionalOnBean(PublicKeyProvider.class)
public class JwtPublicKeyValidator implements JwtValidator {

	private final Logger logger = LoggerFactory.getLogger(JwtPublicKeyValidator.class);
	private final ObjectMapper mapper;
	private final PublicKeyProvider provider;
	private static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})(?:[\\r\\n]{0,2})$");

	public JwtPublicKeyValidator(PublicKeyProvider provider, ObjectMapper mapper) {
		this.mapper = mapper;
		this.provider = provider;
		this.logger.info("Initializing public Key Validator with Provider " + provider);
	}


	@Override
	public JwtPayload parseAndValidate(String token) throws InvalidJwtException {
		final byte[] publicKey = provider.publicKey();
		if (publicKey == null) throw new RuntimeException("System Error: No Public Key loaded");
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFactory.generatePublic(keySpec);

			JwtConsumer consumer = new JwtConsumerBuilder()
				.setVerificationKey(pubKey)
				.build();

			logger.info("verifying Token: " + token);
			JwtContext result = consumer.process(token);
			String payload = result.getJwtClaims().getRawJson();

			return createPayload(payload);
		} catch (InvalidJwtException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
			//Invalid signature/claims
			logger.error(exception.getMessage(), exception);
			if (exception instanceof InvalidJwtException) {
				throw (InvalidJwtException) exception;
			} else {
				throw new RuntimeException("System Error while validating JWT-Token", exception);
			}
		}
	}

	public JwtPayload createPayload(String base64Payload) {
		try {
			JsonNode node = BASE64_PATTERN.matcher(base64Payload).matches()
						? mapper.readTree(Base64.decodeBase64(base64Payload))
				      : mapper.readTree(base64Payload);
			return JwtPayload.generateFromJsonNode(node);

		} catch (IOException e) {
			throw new RuntimeException("Payload could not be parsed", e);
		}
	}

}
