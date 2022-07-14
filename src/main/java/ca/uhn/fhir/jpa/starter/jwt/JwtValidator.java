package ca.uhn.fhir.jpa.starter.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Locale;

public class JwtValidator {

	private final File publicKeyFile;
	private final Logger logger = LoggerFactory.getLogger(JwtValidator.class);
	private final ObjectMapper mapper = new ObjectMapper();
	private byte[] publicKey;

	public JwtValidator(File publicKeyFile) throws IOException {
		this.publicKeyFile = publicKeyFile;
		this.refreshPublicKey();
	}

	private static byte[] readPublicKey(File publicKeyFile) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (FileInputStream fileInputStream = new FileInputStream(publicKeyFile)) {
			StreamUtils.copy(fileInputStream, outputStream);
		}
		return Base64.decodeBase64(outputStream.toByteArray());
	}

	private synchronized void refreshPublicKey() throws IOException {
		this.publicKey = JwtValidator.readPublicKey(publicKeyFile);
		logger.debug((publicKey != null ? publicKey.length : " NULL ") + " Bytes has been loaded from Public Key from File '" + publicKeyFile.getAbsolutePath());
	}

	public JwtPayload parseAndValidate(String token) throws JWTVerificationException {
		if (publicKey == null) throw new RuntimeException("System Error: No Public Key loaded");
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFactory.generatePublic(keySpec);

			final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) pubKey, null);
			Verification builder = JWT.require(algorithm);
			// TODO: make this Configureable!
			// builder.withIssuer("https://isac.svint.infocert.it");

			// finally : verify...
			final JWTVerifier verifier = builder.build(); //Reusable verifier instance
			final DecodedJWT jwt = verifier.verify(token);
			return createPayload(jwt.getPayload());
		} catch (JWTVerificationException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
			//Invalid signature/claims
			logger.error(exception.getMessage(), exception);
			if (exception instanceof JWTVerificationException) {
				throw (JWTVerificationException) exception;
			} else {
				throw new RuntimeException("System Error while validating JWT-Token", exception);
			}
		}
	}

	public JwtPayload createPayload(String base64Payload) {
		JsonNode node = null;
		try {
			node = mapper.readTree(net.iharder.Base64.decode(base64Payload));
			String issuer = "";
			String subject = "";
			String id = "";
			String name = "";
			String preferred_username = "";
			String givenName = "";
			String familyName = "";
			String email = "";
			if (node.has("iss")) {
				issuer = node.get("iss").asText();
			}
			if (node.has("sub")) {
				subject = node.get("sub").asText();
			}
			if (node.has("sid")) {
				id = node.get("sid").asText();
			}

			if (node.has("name")) {
				name = node.get("name").asText();
			}
			if (node.has("preferred_username")) {
				preferred_username = node.get("preferred_username").asText();
			}
			if (node.has("given_name")) {
				givenName = node.get("given_name").asText();
			}
			if (node.has("family_name")) {
				familyName = node.get("family_name").asText();
			}
			if (node.has("email")) {
				email = node.get("email").asText();
			}
			final HashSet<String> roles = new HashSet<>();

			if (node.has("realm_access")
				&& node.get("realm_access").has("roles")
				&& node.get("realm_access").get("roles").isArray()) {
				node.get("realm_access").get("roles").forEach(roleNode -> {
					String e = roleNode.asText();
					if (e != null) {
						roles.add(e.toUpperCase(Locale.ROOT));
					}
				});
			}

			if (node.has("resource_access")) {
				node.get("resource_access").forEach(resourceNode -> {
					if (resourceNode.has("roles") && resourceNode.get("roles").isArray()) {
						resourceNode.get("roles").forEach(roleNode -> {
							String e = roleNode.asText();
							if (e != null) {
								roles.add(e.toUpperCase(Locale.ROOT));
							}
						});
					}
				});
			}

			return new JwtPayload(
				email,
				familyName,
				givenName,
				issuer,
				subject,
				id,
				name,
				preferred_username,
				roles
			);

		} catch (IOException e) {
			throw new RuntimeException("Payload could not be parsed", e);
		}


	}
}
