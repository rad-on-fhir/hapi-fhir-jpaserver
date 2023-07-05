package ca.uhn.fhir.jpa.starter.jwt.rules;

import ca.uhn.fhir.jpa.starter.jwt.JwtPayload;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	value = "jwt.allowAnyRole",
	havingValue = "true",
	matchIfMissing = true
)
public class JwtBuildRuleAnyRole implements JwtRuleBuilder {

	public JwtBuildRuleAnyRole() {
		LoggerFactory.getLogger(JwtBuildRuleAnyRole.class).info("Rule Builder for any FHIR-Role has been added.");
	}

	@Override
	public void addRules(RuleBuilder builder, JwtPayload jwtPayload) throws Exception {
		for (String role : jwtPayload.getRoles()) {
			if (role.toUpperCase().startsWith("PINK.")) {
				builder.allowAll();
				return;
			}
		}
		builder.denyAll();
	}
}
