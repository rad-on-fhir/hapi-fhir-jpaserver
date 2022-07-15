package ca.uhn.fhir.jpa.starter.jwt.key;

import ca.uhn.fhir.jpa.starter.jwt.JwtValidator;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
			this.publicKey = readPublicKey(publicKeyFile);
			logger.debug((publicKey != null ? publicKey.length : " NULL ") + " Bytes has been loaded from Public Key from File '" + publicKeyFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Error loading Public Key from File " + publicKeyFile.getAbsolutePath());
			throw e;
		}
	}

	private static byte[] readPublicKey(File publicKeyFile) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (FileInputStream fileInputStream = new FileInputStream(publicKeyFile)) {
			StreamUtils.copy(fileInputStream, outputStream);
		}
		return Base64.decodeBase64(outputStream.toByteArray());
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
