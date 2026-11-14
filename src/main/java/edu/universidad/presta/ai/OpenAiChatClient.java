package edu.universidad.presta.ai;

import edu.universidad.presta.config.AppConfig;
import edu.universidad.presta.util.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Cliente servidor-a-servidor. La clave nunca se envía al navegador. */
public final class OpenAiChatClient {
    private static final Logger LOG = Logger.getLogger(OpenAiChatClient.class.getName());
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12)).build();
    private OpenAiChatClient() {}

    public static boolean disponible() { return !AppConfig.aiApiKey().isBlank(); }

    public static String responder(List<Map<String, String>> mensajes) {
        if (!disponible()) return null;
        StringBuilder body = new StringBuilder("{\"model\":")
                .append(Json.texto(AppConfig.aiModel()))
                .append(",\"messages\":[");
        for (int i = 0; i < mensajes.size(); i++) {
            if (i > 0) body.append(',');
            Map<String, String> m = mensajes.get(i);
            body.append("{\"role\":").append(Json.texto(m.get("role")))
                    .append(",\"content\":").append(Json.texto(m.get("content"))).append('}');
        }
        body.append("],\"max_completion_tokens\":500,\"reasoning_effort\":\"none\"}");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(AppConfig.aiEndpoint()))
                    .timeout(Duration.ofSeconds(AppConfig.aiTimeoutSeconds()))
                    .header("Authorization", "Bearer " + AppConfig.aiApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.warning("Proveedor IA respondió HTTP " + response.statusCode());
                return null;
            }
            Map<String, Object> root = MiniJson.object(MiniJson.parse(response.body()));
            List<Object> choices = MiniJson.array(root.get("choices"));
            if (choices.isEmpty()) return null;
            Map<String, Object> choice = MiniJson.object(choices.get(0));
            Map<String, Object> message = MiniJson.object(choice.get("message"));
            Object content = message.get("content");
            return content instanceof String && !((String) content).isBlank() ? ((String) content).trim() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Solicitud al proveedor IA interrumpida", e);
            return null;
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo completar la consulta al proveedor IA", e);
            return null;
        }
    }
}
