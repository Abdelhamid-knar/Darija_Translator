package translator;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

@Path("/translate")           // Full URL: http://localhost:8080/darija-translator/api/translate
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslatorResource {

    @Inject
    private GeminiService geminiService;  // CDI injects the service automatically

    @POST
    public Response translate(TranslationRequest request) {
        // Validate input
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(new TranslationResponse(null, "Text cannot be empty"))
                           .type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name()))
                           .build();
        }

        try {
            String translated = geminiService.translate(request.getText());
            return Response.ok(new TranslationResponse(translated, null))
                           .type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name()))
                           .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new TranslationResponse(null, "Translation failed: " + e.getMessage()))
                           .type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name()))
                           .build();
        }
    }

    // ----- Inner DTO classes (Data Transfer Objects) -----

    // What the client SENDS
    public static class TranslationRequest {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    // What the server RETURNS
    public static class TranslationResponse {
        private String translation;
        private String error;

        public TranslationResponse(String translation, String error) {
            this.translation = translation;
            this.error = error;
        }
        public String getTranslation() { return translation; }
        public String getError() { return error; }
    }
}