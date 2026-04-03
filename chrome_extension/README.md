## Chrome Extension (Manifest V3)

### What it does
- Right-click a text selection on any webpage
- Click **Translate to Darija**
- Opens the extension **Side Panel**
- Sends the selected text to your REST endpoint
- Displays the translation and tries to auto-copy it to the clipboard

### Setup
1. Make sure WildFly is running and your endpoint is reachable at:
   - `http://localhost:8080/darija-translator/api/translate`
2. Install the extension:
   - Chrome → `chrome://extensions`
   - Enable **Developer mode**
   - Click **Load unpacked**
   - Select the folder `chrome_extension/`

### Configure Basic Auth
In the side panel UI:
- Enter your Basic Auth `username` and `password`
- (Optional) adjust the REST endpoint URL

The extension stores these values locally.

