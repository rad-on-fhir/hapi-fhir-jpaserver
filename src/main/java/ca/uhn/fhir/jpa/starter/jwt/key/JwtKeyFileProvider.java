package ca.uhn.fhir.jpa.starter.jwt.key;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;

public class JwtKeyFileProvider implements PublicKeyProvider{
	private final File publicKeyFile;
	private byte[] publicKey;
	private final Logger logger = LoggerFactory.getLogger(JwtKeyFileProvider.class);

	public JwtKeyFileProvider(File publicKeyFile) {
		this.publicKeyFile = publicKeyFile;
		try {
			this.refresh();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] publicKey() {
		return publicKey;
	}

	@Override
	public synchronized void refresh() throws IOException {
		try {
			byte[] loadedPublicKey = readPublicKey(publicKeyFile);
			if (loadedPublicKey != null) {
				X509EncodedKeySpec keySpec = new X509EncodedKeySpec(loadedPublicKey);
				logger.info("KeySpec Loaded with Algorithm " + keySpec.getAlgorithm());
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PublicKey pubKey = keyFactory.generatePublic(keySpec);
				logger.info("Public Key with Algorithm " + pubKey.getAlgorithm());
			}
			this.publicKey = loadedPublicKey;
			logger.debug((publicKey != null ? publicKey.length : " NULL ") + " Bytes has been loaded from Public Key from File '" + publicKeyFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Error loading Public Key from File " + publicKeyFile.getAbsolutePath());
			throw e;
		} catch (Exception e) {
			logger.error("Error parsing Public Key from File " + publicKeyFile.getAbsolutePath());
			throw new IOException("Error Parsing Key from File " + publicKeyFile.getAbsolutePath(), e);
		}
	}

	private static byte[] readPublicKey(File publicKeyFile) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (FileInputStream fileInputStream = new FileInputStream(publicKeyFile)) {
			StreamUtils.copy(fileInputStream, outputStream);
		}
		String pemString = outputStream.toString(StandardCharsets.UTF_8);
		int startIndex = pemString.indexOf("-----BEGIN PUBLIC KEY-----");
		int endIndex = pemString.indexOf("-----END PUBLIC KEY-----");
		if (startIndex >= 0 && endIndex > startIndex) {
			pemString = pemString.substring(startIndex + "-----BEGIN PUBLIC KEY-----".length(), endIndex).trim();
		}
		System.err.println("Using Public Key String " + pemString);
		return Base64.decodeBase64(pemString);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JwtKeyFileProvider that = (JwtKeyFileProvider) o;
		return Objects.equals(publicKeyFile, that.publicKeyFile) && Arrays.equals(publicKey, that.publicKey);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(publicKeyFile);
		result = 31 * result + Arrays.hashCode(publicKey);
		return result;
	}

	@Override
	public String toString() {
		return "JwtKeyFileProvider( " +publicKeyFile.getAbsolutePath() +" )";
	}
}
