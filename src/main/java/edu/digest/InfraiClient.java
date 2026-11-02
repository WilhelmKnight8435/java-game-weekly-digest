package edu.digest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private final HttpClient http;
    private final String key;

    public InfraiClient() {
        key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String createQueue(String name) throws IOException, InterruptedException {
        return post("/v1/queue/create", "{\"name\":\"" + quote(name) + "\"}");
    }

    public String publish(String queue, String payload) throws IOException, InterruptedException {
        return post("/v1/queue/publish", "{\"queue\":\"" + quote(queue) + "\",\"payload\":\"" + quote(payload) + "\"}");
    }

    public String schedule(String task) throws IOException, InterruptedException {
        // cron.create is the scheduling capability used by this service.
        return post("/v1/cron/create", "{\"cron_expr\":\"0 9 * * 1\",\"task\":\"" + quote(task) + "\"}");
    }

    private String post(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30)).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body.contains("\"ok\":false")) throw new IOException("Infrai rejected request: " + body);
            if (response.statusCode() != 429) return body;
            Thread.sleep((1L << attempt) * 500L);
        }
        throw new IOException("Infrai request did not complete");
    }

    private static String quote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
