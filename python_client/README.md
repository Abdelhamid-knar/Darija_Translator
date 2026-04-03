## Python client (Basic Auth)

This script calls the REST endpoint:

`POST /darija-translator/api/translate`

and prints the `translation` field.

### Run

```bash
python python_client/translator_client.py --text "Hello world"
```

### Configure Basic Auth

The script reads credentials from environment variables (recommended) or defaults to:
`admin / admin`

Set:

```bash
set BASIC_AUTH_USERNAME=admin
set BASIC_AUTH_PASSWORD=admin
```

### Custom URL / text

```bash
python python_client/translator_client.py --url "http://localhost:8080/darija-translator/api/translate" --text "How are you?"
```

