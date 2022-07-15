package ca.uhn.fhir.jpa.starter.jwt.rules;

import ca.uhn.fhir.jpa.starter.jwt.JwtPayload;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

public interface JwtRuleBuilder {
	void addRules(RuleBuilder builder, JwtPayload jwtPayload) throws Exception;
}
