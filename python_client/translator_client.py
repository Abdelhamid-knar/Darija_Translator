import argparse
import base64
import json
import os
import sys
import urllib.error
import urllib.request


def _b64_basic_auth(username: str, password: str) -> str:
    token = f"{username}:{password}".encode("utf-8")
    return base64.b64encode(token).decode("ascii")


def translate(text: str, url: str, username: str, password: str, timeout_sec: int) -> dict:
    payload = {"text": text}
    body_bytes = json.dumps(payload, ensure_ascii=False).encode("utf-8")

    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "Accept": "application/json",
        "Authorization": f"Basic {_b64_basic_auth(username, password)}",
    }

    req = urllib.request.Request(url=url, data=body_bytes, headers=headers, method="POST")

    try:
        with urllib.request.urlopen(req, timeout=timeout_sec) as resp:
            resp_bytes = resp.read()
            resp_text = resp_bytes.decode("utf-8", errors="replace")
            try:
                return json.loads(resp_text)
            except json.JSONDecodeError:
                return {"translation": None, "error": resp_text}
    except urllib.error.HTTPError as e:
        # Read the error body for debugging (WildFly returns JSON with our error message)
        err_bytes = e.read() if e.fp is not None else b""
        err_text = err_bytes.decode("utf-8", errors="replace")
        try:
            return json.loads(err_text)
        except json.JSONDecodeError:
            return {"translation": None, "error": err_text or str(e)}


def main() -> int:
    # Ensure correct UTF-8 rendering in Windows terminals.
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

    parser = argparse.ArgumentParser(description="Python client for the Darija Translator REST API.")
    parser.add_argument("--url", default=os.getenv("TRANSLATOR_URL", "http://localhost:8080/darija-translator/api/translate"))
    parser.add_argument("--username", default=os.getenv("BASIC_AUTH_USERNAME", "admin"))
    parser.add_argument("--password", default=os.getenv("BASIC_AUTH_PASSWORD", "admin"))
    parser.add_argument("--text", default=os.getenv("TRANSLATOR_TEXT", "Hello world"))
    parser.add_argument("--timeout", type=int, default=60)
    args = parser.parse_args()

    result = translate(
        text=args.text,
        url=args.url,
        username=args.username,
        password=args.password,
        timeout_sec=args.timeout,
    )

    if result.get("error"):
        print(result["error"])
        return 1

    print(result.get("translation", ""))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

