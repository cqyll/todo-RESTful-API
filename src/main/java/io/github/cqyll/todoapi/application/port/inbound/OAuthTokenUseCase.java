package io.github.cqyll.todoapi.application.port.inbound;

import java.util.Map;

import io.github.cqyll.todoapi.dto.OAuthTokenRequest;

public interface OAuthTokenUseCase {
	Map<String, Object> token(OAuthTokenRequest request);
}
