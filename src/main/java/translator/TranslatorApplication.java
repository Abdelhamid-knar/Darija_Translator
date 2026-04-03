package translator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import translator.auth.BasicAuthFilter;
import java.util.Set;

// All REST endpoints will be under /api/*
@ApplicationPath("/api")
public class TranslatorApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        // Explicit registration avoids relying on classpath scanning.
        return Set.of(
            TranslatorResource.class,
            BasicAuthFilter.class
        );
    }
}