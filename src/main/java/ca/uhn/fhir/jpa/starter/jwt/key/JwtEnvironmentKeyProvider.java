package ca.uhn.fhir.jpa.starter.jwt.key;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class JwtEnvironmentKeyProvider implements PublicKeyProvider{

	private final String envKey;
	private byte[] publicKey;

	public JwtEnvironmentKeyProvider(String envKey) {
		if (envKey == null || envKey.isBlank()) throw new IllegalArgumentException("Environment Key cannot be null or empty");
		this.envKey = envKey;
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
	public void refresh() throws IOException {
		String base64 = System.getenv(this.envKey);
		if (base64 == null || base64.isBlank()) throw new IOException("Environment not set: '" + envKey + "'");
		try {
			this.publicKey = Base64.decodeBase64(base64);
		} catch (Exception e) {
			throw new IOException("Error decoding Public Key from Environment Key '" + envKey + "'", e);
		}
	}

	@Override
	public String toString() {
		return "JwtEnvironmentKeyProvider(" + envKey + ')';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JwtEnvironmentKeyProvider that = (JwtEnvironmentKeyProvider) o;
		return Objects.equals(envKey, that.envKey) && Arrays.equals(publicKey, that.publicKey);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(envKey);
		result = 31 * result + Arrays.hashCode(publicKey);
		return result;
	}
}
