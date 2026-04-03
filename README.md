# Darija Translator

Java REST API that translates text into Moroccan Arabic Dialect (Darija) using Gemini. The repository also includes:

- a Chrome extension that sends selected text to the API
- a Python client for quick local testing

## Repository Layout

- `src/`: Java API source
- `chrome_extension/`: Chrome side-panel extension
- `python_client/`: simple CLI client
- `pom.xml`: Maven build file

## API Endpoint

Default local endpoint:

`POST http://localhost:8080/darija-translator/api/translate`

Request body:

```json
{
  "text": "Hello world"
}
```

Response body:

```json
{
  "translation": "...",
  "error": null
}
```

## Configuration

Set these environment variables before running the application:

```powershell
$env:GEMINI_API_KEY="your-gemini-api-key"
$env:BASIC_AUTH_USERNAME="your-username"
$env:BASIC_AUTH_PASSWORD="your-strong-password"
```

The app reads those values from `src/main/resources/META-INF/microprofile-config.properties`.

## Build

Requirements:

- Java 17
- Maven
- WildFly / Jakarta EE 10 compatible server

Build the WAR:

```powershell
mvn clean package
```

WAR output:

`target/darija-translator.war`

## Chrome Extension

See [chrome_extension/README.md](chrome_extension/README.md).

## Python Client

See [python_client/README.md](python_client/README.md).

## Security Notes

- Do not commit real API keys, passwords, or `.env` files.
- Rotate any key that was ever committed, even if it was later removed.
- Keep local credentials outside Git-tracked files.
