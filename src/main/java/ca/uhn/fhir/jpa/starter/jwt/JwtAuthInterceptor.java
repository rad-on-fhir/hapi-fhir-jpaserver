package ca.uhn.fhir.jpa.starter.jwt;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.jwt.rules.JwtRuleBuilder;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
@Interceptor
@ConditionalOnProperty(value="jwt.disabled",havingValue="false",matchIfMissing = true)
public class JwtAuthInterceptor extends AuthorizationInterceptor {

	private final JwtValidator jwtValidator;

	private final Logger logger = LoggerFactory.getLogger(JwtAuthInterceptor.class);
	private final List<JwtRuleBuilder> ruleBuilders;

	@Autowired
	public JwtAuthInterceptor(JwtValidator validator, List<JwtRuleBuilder> ruleBuilders,  IInterceptorService service) throws IOException {
		this.ruleBuilders = ruleBuilders;
		this.jwtValidator = validator;
		this.logger.info("JwtInterceptor initiated with Validator " + validator);
		service.registerInterceptor(this);
	}

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
		String authHeader = theRequestDetails.getHeader("Authorization");


		// The format of the header must be:
		// Authorization: Bearer [jwt-Token]
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new AuthenticationException(Msg.code(642) + "Missing or invalid Authorization header");
		}

		return true;
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		String authHeader = theRequestDetails.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new AuthenticationException(Msg.code(642) + "Missing or invalid Authorization header");
		}

		String base64 = authHeader.substring("Bearer ".length());
		final JwtPayload token;
		try {
			token = jwtValidator.parseAndValidate(base64);
		} catch (Exception e) {
			throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value", e);
		}

		final RuleBuilder builder = new RuleBuilder();

		for (JwtRuleBuilder ruleBuilder : ruleBuilders) {
			try {
				ruleBuilder.addRules(builder, token);
			} catch (Exception e) {
				logger.error("Error Building Rules for " + token.getPreferred_username() + " with Builder " + ruleBuilder + " aborting and deny all!", e);
				builder.denyAll();
				return builder.build();
			}
		}
		List<IAuthRule> build = builder.build();
		logger.info("Rule List has been Build with " + build.size()	 + " Entries.");
		return build;
	}
}
