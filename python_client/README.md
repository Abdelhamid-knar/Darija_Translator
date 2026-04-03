## Python client (Basic Auth)

This script calls the REST endpoint:

`POST /darija-translator/api/translate`

and prints the `translation` field.

### Run

```bash
python python_client/translator_client.py --text "Hello world"
```

### Configure Basic Auth

The script reads credentials from environment variables (recommended). If you do not set them, the script uses local demo defaults, so set your own values before sharing or deploying.

Set:

```powershell
$env:BASIC_AUTH_USERNAME="your-username"
$env:BASIC_AUTH_PASSWORD="your-password"
```

### Custom URL / text

```bash
python python_client/translator_client.py --url "http://localhost:8080/darija-translator/api/translate" --text "How are you?"
```

