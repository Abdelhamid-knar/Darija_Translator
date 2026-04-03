package translator;

import com.google.gson.*;
import okhttp3.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped  // One instance shared across the app (CDI bean)
public class GeminiService {

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/" +
        "gemini-2.5-flash:generateContent";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    @Inject
    @ConfigProperty(name = "gemini.api.key")
    Optional<String> apiKey;

    public String translate(String text) throws IOException {
        String configuredApiKey = apiKey
            .map(String::trim)
            .filter(key -> !key.isEmpty())
            .orElseThrow(() -> new IOException("Gemini API key is not configured"));

        if (configuredApiKey.isBlank()) {
            throw new IOException("Gemini API key is not configured");
        }

        // Build the prompt — this is the key instruction to the LLM
        String prompt = "Translate the following text to Moroccan Arabic Dialect (Darija). " +
                        "Return ONLY the translation, no explanation:\n\n" + text;

        // Build the JSON body Gemini expects
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        // Make the HTTP POST request to Gemini
        RequestBody body = RequestBody.create(
            gson.toJson(requestBody),
            MediaType.get("application/json")
        );

        Request request = new Request.Builder()
            .url(GEMINI_URL)
            .addHeader("x-goog-api-key", configuredApiKey)
            .post(body)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: " + response.code() +
                    (responseBody.isBlank() ? "" : " - " + responseBody));
            }

            // Parse the Gemini JSON response
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);

            // Navigate the nested JSON: candidates[0].content.parts[0].text
            return json.getAsJsonArray("candidates")
                       .get(0).getAsJsonObject()
                       .getAsJsonObject("content")
                       .getAsJsonArray("parts")
                       .get(0).getAsJsonObject()
                       .get("text").getAsString();
        }
    }
}
