package io.github.cqyll.todoapi.dto;

import java.util.HashMap;
import java.util.Map;

public class OAuthTokenRequest extends AuthRequest {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private String refreshToken;
    private String scope;
    private String redirectUri;

    public OAuthTokenRequest() { super("oauth"); }

    public String getGrantType() { return grantType; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRefreshToken() { return refreshToken; }
    public String getScope() { return scope; }
    public String getRedirectUri() { return redirectUri; }

    @Override
    public Map<String, String> toCredentials() {
        Map<String, String> credentials = new HashMap<>();
        putIfNotNull(credentials, "grant_type", grantType);
        putIfNotNull(credentials, "client_id", clientId);
        putIfNotNull(credentials, "client_secret", clientSecret);
        putIfNotNull(credentials, "username", username);
        putIfNotNull(credentials, "password", password);
        putIfNotNull(credentials, "refresh_token", refreshToken);
        putIfNotNull(credentials, "scope", scope);
        putIfNotNull(credentials, "redirect_uri", redirectUri);
        return credentials;
    }

    private void putIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }

    @Override
    public void validate() {
        if (clientId == null || clientId.isBlank()) throw new IllegalArgumentException("invalid_request");
        if (grantType == null || grantType.isBlank()) throw new IllegalArgumentException("invalid_request");

        // You only implement password right now
        if (!"password".equals(grantType)) throw new IllegalArgumentException("unsupported_grant_type");

        if (username == null || username.isBlank()) throw new IllegalArgumentException("invalid_request");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("invalid_request");
    }

    public void setGrantType(String grantType) { this.grantType = grantType; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setScope(String scope) { this.scope = scope; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
