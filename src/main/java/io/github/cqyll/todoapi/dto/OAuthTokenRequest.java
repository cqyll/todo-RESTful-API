package io.github.cqyll.todoapi.dto;

import java.util.HashMap;
import java.util.Map;

public class OAuthTokenRequest {
	private String grantType;
	private String clientId;
	private String clientSecret;
	private String username;
	private String password;
	private String refreshToken;
	private String code;
	private String scope;
	private String redirectUri;
	
	public OAuthTokenRequest() {
	}
	
	
<<<<<<< HEAD
	public Map<String, String> toCredentials() {
        Map<String, String> creds = new HashMap<>();
        putIfNotNull(creds, "grant_type", grantType);
        putIfNotNull(creds, "client_id", clientId);
        putIfNotNull(creds, "client_secret", clientSecret);
        putIfNotNull(creds, "username", username);
        putIfNotNull(creds, "password", password);
        putIfNotNull(creds, "refresh_token", refreshToken);
        putIfNotNull(creds, "code", code);
        putIfNotNull(creds, "redirect_uri", redirectUri);
        putIfNotNull(creds, "scope", scope);
        return creds;
    }
	
	private void putIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
	
	// unused method, validation handled by service -- jan 14
=======
	/**
	 * Creates an {@code OAuthTokenRequest} from parsed form parameters and client credentials.
	 * 
	 * <p>Maps OAuth token request parameter names to DTO fields. Duplicate form parameters are assumed to have already been resolved. </p>
	 * 
	 * @param form parsed token request parameters
	 * @param clientId resolved client identifier
	 * @param clientSecret resolved client secret
	 * @return populated request DTO
	 */
	public static OAuthTokenRequest from(Map<String, String> form, String clientId, String clientSecret) {
		OAuthTokenRequest req = new OAuthTokenRequest();
	    req.setGrantType(form.get("grant_type"));
	    req.setUsername(form.get("username"));
	    req.setPassword(form.get("password"));
	    req.setRefreshToken(form.get("refresh_token"));
	    req.setCode(form.get("code"));
	    req.setScope(form.get("scope"));
	    req.setRedirectUri(form.get("redirect_uri"));
	    req.setClientId(clientId);
	    req.setClientSecret(clientSecret);
	    return req;
	}
	
>>>>>>> 20bec2f (WIP: ongoing OAuth token flow refactor)
    public void validate() {
        // Basic validation - client_id is required for all OAuth flows
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("client_id is required");
        }
        
        // Validate based on grant type
        if (grantType == null) {
            throw new IllegalArgumentException("grant_type is required");
        }
        
        switch (grantType) {
            case "password":
                if (username == null || username.isBlank()) {
                    throw new IllegalArgumentException("username is required for password grant");
                }
                if (password == null || password.isBlank()) {
                    throw new IllegalArgumentException("password is required for password grant");
                }
                break;
                
            case "authorization_code":
                if (code == null || code.isBlank()) {
                    throw new IllegalArgumentException("code is required for authorization_code grant");
                }
                break;
                
            case "refresh_token":
                if (refreshToken == null || refreshToken.isBlank()) {
                    throw new IllegalArgumentException("refresh_token is required for refresh_token grant");
                }
                break;
                
            case "client_credentials":
                // Only client_id and client_secret needed
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported grant_type: " + grantType);
        }
    }

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	
	
    
}

