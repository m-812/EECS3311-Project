package recipeagent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

public class HttpLLMClient implements LLMClient {
    private String apiKey;
    private String model;
    private String endpoint;

    public HttpLLMClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        endpoint = "https://api.anthropic.com/v1/messages";
    }

    @Override
    public String sendMessage(String systemPrompt, List<ChatMessage> messages) {
        JsonArray turns = new JsonArray();
        for (ChatMessage message : messages) {
            JsonObject turn = new JsonObject();
            turn.addProperty("role", message.getAuthor().equals("AI") ? "assistant" : "user");
            turn.addProperty("content", message.getContent());
            turns.add(turn);
        }
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("max_tokens", 16000);
        body.addProperty("system", systemPrompt);
        body.add("messages", turns);

        JsonObject response = JsonParser.parseString(sendRequest(body.toString())).getAsJsonObject();
        String stopReason = response.get("stop_reason").getAsString();
        if (stopReason.equals("refusal")) {
            throw new RuntimeException("The AI declined to answer this request.");
        }
        if (!stopReason.equals("end_turn")) {
            throw new RuntimeException("The AI's reply was cut off before it finished. Try a shorter request.");
        }
        String text = "";
        for (JsonElement block : response.getAsJsonArray("content")) {
            JsonObject content = block.getAsJsonObject();
            if (content.get("type").getAsString().equals("text")) {
                text += content.get("text").getAsString();
            }
        }
        return text;
    }

    private String sendRequest(String body) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .header("anthropic-version", "2026-10-21")
                .header("content-type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String problem = "";
        try {
            for (int attempt = 1; attempt <= 3; attempt++) {
                if (attempt > 1) {
                    Thread.sleep(2000L * (attempt - 1));
                }
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    int status = response.statusCode();
                    if (status == 200) {
                        return response.body();
                    }
                    problem = "HTTP " + status + ": " + response.body();
                    if (status < 500 && status != 408 && status != 409 && status != 429) {
                        break;
                    }
                } catch (HttpTimeoutException e) {
                    throw new RuntimeException("The AI took too long to reply.");
                } catch (IOException e) {
                    problem = e.toString();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("The AI request was interrupted.");
        }
        throw new RuntimeException("The AI request failed. " + problem);
    }
}
