package io.github.cqyll.todoapi.adapter.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.cqyll.todoapi.application.port.inbound.BasicLoginUseCase;
import io.github.cqyll.todoapi.dto.BasicAuthRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class LoginController implements HttpHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final BasicLoginUseCase useCase;

    public LoginController(BasicLoginUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.getResponseHeaders().set("Allow", "POST");
            ex.sendResponseHeaders(405, -1);
            return;
        }

        try {
            BasicAuthRequest req = mapper.readValue(ex.getRequestBody(), BasicAuthRequest.class);
            req.validate();

            String token = useCase.login(req.getEmail(), req.getPassword());
            writeJson(ex, 200, Map.of("token", token));

        } catch (IllegalArgumentException e) {
            writeJson(ex, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            writeJson(ex, 500, Map.of("error", "Internal Server Error"));
        }
    }

    private void writeJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
