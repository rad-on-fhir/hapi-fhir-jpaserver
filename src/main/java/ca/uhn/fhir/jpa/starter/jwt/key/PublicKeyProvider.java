package ca.uhn.fhir.jpa.starter.jwt.key;

import java.io.IOException;

public interface PublicKeyProvider {
	byte[] publicKey() ;
	void refresh() throws IOException;
}
