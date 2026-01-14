package io.github.cqyll.todoapi.application.service;

import io.github.cqyll.todoapi.application.port.inbound.OAuthTokenUseCase;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;
import io.github.cqyll.todoapi.domain.User;
import io.github.cqyll.todoapi.dto.OAuthTokenRequest;
import io.github.cqyll.todoapi.adapter.inbound.web.OAuthError;

import java.util.Map;

public class OAuthTokenService implements OAuthTokenUseCase {
    private final BasicAuthenticationStrategy basicAuth;
    private final TokenProviderPort tokenProvider;

    // Replace with ClientRepositoryPort later (recommended).
    private final String expectedClientId;
    private final String expectedClientSecret;

    public OAuthTokenService(
            BasicAuthenticationStrategy basicAuth,
            TokenProviderPort tokenProvider,
            String expectedClientId,
            String expectedClientSecret
    ) {
        this.basicAuth = basicAuth;
        this.tokenProvider = tokenProvider;
        this.expectedClientId = expectedClientId;
        this.expectedClientSecret = expectedClientSecret;
    }

    @Override
    public Map<String, Object> token(OAuthTokenRequest req) {
        // RFC 6749 required params
        if (isBlank(req.getGrantType())) {
            throw OAuthError.invalidRequest("grant_type is required");
        }

        // Validate client (token endpoint requires confidential client auth in many deployments).
        if (isBlank(req.getClientId())) {
            throw OAuthError.invalidClient("client authentication failed");
        }
        if (!expectedClientId.equals(req.getClientId())
                || !expectedClientSecret.equals(nullToEmpty(req.getClientSecret()))) {
            throw OAuthError.invalidClient("client authentication failed");
        }

        String grant = req.getGrantType();
        if (!"password".equals(grant)) {
            // since you currently implement only password
            throw OAuthError.unsupportedGrantType("grant_type not supported");
        }

        if (isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw OAuthError.invalidRequest("username and password are required for password grant");
        }

        final User user;
        try {
        	 user = basicAuth.authenticate(Map.of(
                     "email", req.getUsername(),
                     "password", req.getPassword()
             ));
        } catch (RuntimeException e) {
        	throw OAuthError.invalidGrant("invalid resource owner credentials");
        }
       
        if (!user.isActive()) {
            // OAuth2 commonly uses invalid_grant for bad user creds / invalid grant usage.
            throw OAuthError.invalidGrant("invalid resource owner credentials");
        }

        String access = tokenProvider.createToken(user.getId());

        return Map.of(
                "access_token", access,
                "token_type", "Bearer",
                "expires_in", 3600
        );
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
