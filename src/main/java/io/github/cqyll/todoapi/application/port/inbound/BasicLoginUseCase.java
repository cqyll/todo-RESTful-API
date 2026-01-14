package io.github.cqyll.todoapi.application.port.inbound;

public interface BasicLoginUseCase {
    String login(String email, String password);
}
