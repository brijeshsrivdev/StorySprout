package com.storysprout.api.render;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class RendererClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper;
    private final String baseUrl;

    public RendererClient(
            ObjectMapper mapper,
            @Value("${storysprout.renderer.base-url:http://localhost:4000}") String baseUrl) {
        this.mapper = mapper;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    public byte[] render(RenderJob job) throws IOException, InterruptedException {
        ObjectNode request = mapper.createObjectNode();
        request.put("renderJobId", job.id().toString());
        request.put("compositionId", job.compositionId().toString());
        request.put("compositionVersion", job.compositionVersion());
        request.set("composition", job.snapshot());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/render"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(request)))
                .build();

        HttpResponse<byte[]> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            String detail = new String(response.body());
            throw new IOException("Renderer returned HTTP " + response.statusCode() + ": " + detail);
        }
        return response.body();
    }
}
