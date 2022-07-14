package ca.uhn.fhir.jpa.starter.jwt;

import java.util.*;

public class JwtPayload  {
	private final String email;
	private final String familyName;
	private final String givenName;
	private final String issuer;
	private final String subject;
	private final String id;
	private final String name;
	private final String preferred_username;

	private final HashSet<String> roles;

	public JwtPayload(String email, String familyName, String givenName, String issuer, String subject, String id, String name, String preferred_username, HashSet<String> roles) {
		this.email = email;
		this.familyName = familyName;
		this.givenName = givenName;
		this.issuer = issuer;
		this.subject = subject;
		this.id = id;
		this.name = name;
		this.preferred_username = preferred_username;
		this.roles = roles;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getSubject() {
		return subject;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getName() {
		return name;
	}

	public String getPreferred_username() {
		return preferred_username;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public boolean hasRole(String role) {
		if (role == null || role.isBlank()) return false;
		return roles.contains(role);
	}

	@Override
	public String toString() {
		return ("'" + name + "' <" + email + "> " + (" { " + preferred_username).trim() + " " + roles + " }").trim();
	}
}
