package translator.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    @Inject
    @ConfigProperty(name = "basic.auth.username")
    Optional<String> username;

    @Inject
    @ConfigProperty(name = "basic.auth.password")
    Optional<String> password;

    @Inject
    @ConfigProperty(name = "basic.auth.realm")
    Optional<String> realm;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String configuredUsername = username.map(String::trim).orElse("");
        String configuredPassword = password.orElse("");
        String configuredRealm = realm.map(r -> r.trim().isEmpty() ? "Darija Translator API" : r.trim())
            .orElse("Darija Translator API");

        if (configuredUsername.isBlank() || configuredPassword.isBlank()) {
            // If auth isn’t configured, fail closed to avoid exposing your endpoint.
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Basic auth is not configured (set basic.auth.username/password).")
                .build());
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Basic ", 0, 6)) {
            challenge(requestContext, configuredRealm);
            return;
        }

        String base64Credentials = authHeader.substring(6).trim();
        String decoded;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            decoded = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            challenge(requestContext, configuredRealm);
            return;
        }

        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            challenge(requestContext, configuredRealm);
            return;
        }

        String requestUsername = decoded.substring(0, separatorIndex);
        String requestPassword = decoded.substring(separatorIndex + 1);

        if (!configuredUsername.equals(requestUsername) || !configuredPassword.equals(requestPassword)) {
            challenge(requestContext, configuredRealm);
        }
    }

    private static void challenge(ContainerRequestContext requestContext, String realm) {
        String wwwAuthenticate = "Basic realm=\"" + realm + "\"";
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate)
            .type(MediaType.TEXT_PLAIN_TYPE)
            .entity("Unauthorized")
            .build());
    }
}
