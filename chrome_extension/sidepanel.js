const $ = (id) => document.getElementById(id);

const statusEl = $("status");
const textInputEl = $("textInput");
const translationBoxEl = $("translationBox");
const usernameEl = $("username");
const passwordEl = $("password");
const baseUrlEl = $("baseUrl");
const translateBtn = $("translateBtn");
const copyBtn = $("copyBtn");

const DEFAULT_BASE_URL = "http://localhost:8080/darija-translator/api/translate";

function setStatus(msg) {
  statusEl.textContent = msg;
}

async function loadSettings() {
  const { baseUrl, username, password, latestText } = await chrome.storage.local.get([
    "baseUrl",
    "username",
    "password",
    "latestText",
  ]);

  baseUrlEl.value = baseUrl || DEFAULT_BASE_URL;
  usernameEl.value = username || "";
  passwordEl.value = password || "";

  // If we already have a selection from the context menu, reuse it.
  if (latestText) {
    textInputEl.value = latestText;
    setStatus("Selected text loaded.");
  }
}

async function saveSettings() {
  await chrome.storage.local.set({
    baseUrl: baseUrlEl.value.trim(),
    username: usernameEl.value,
    password: passwordEl.value,
  });
}

function basicAuthHeader(username, password) {
  const token = btoa(`${username}:${password}`);
  return `Basic ${token}`;
}

async function translateText(text) {
  const baseUrl = baseUrlEl.value.trim() || DEFAULT_BASE_URL;
  const username = usernameEl.value;
  const password = passwordEl.value;

  if (!username || !password) {
    setStatus("Missing Basic Auth credentials. Enter username/password and click Translate.");
    return;
  }

  setStatus("Translating...");
  translationBoxEl.textContent = "";
  copyBtn.disabled = true;

  try {
    const resp = await fetch(baseUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=UTF-8",
        Authorization: basicAuthHeader(username, password),
      },
      body: JSON.stringify({ text }),
    });

    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) {
      const err = data?.error || `HTTP ${resp.status}`;
      setStatus(`Error: ${err}`);
      return;
    }

    const translation = data.translation || "";
    translationBoxEl.textContent = translation;
    setStatus("Done.");
    copyBtn.disabled = !translation;

    // Auto-copy attempt (may be blocked; user can still click Copy translation).
    try {
      await navigator.clipboard.writeText(translation);
    } catch (e) {}
  } catch (e) {
    setStatus(`Request failed: ${String(e)}`);
  }
}

translateBtn.addEventListener("click", async () => {
  const text = (textInputEl.value || "").trim();
  if (!text) {
    setStatus("Please enter or select text first.");
    return;
  }
  await saveSettings();
  await translateText(text);
});

copyBtn.addEventListener("click", async () => {
  const translation = (translationBoxEl.textContent || "").trim();
  if (!translation) return;
  try {
    await navigator.clipboard.writeText(translation);
    setStatus("Copied to clipboard.");
  } catch (e) {
    setStatus("Copy failed. Your browser may block clipboard access.");
  }
});

chrome.runtime.onMessage.addListener(async (msg) => {
  if (!msg || msg.type !== "TRANSLATE_SELECTION") return;
  const text = (msg.text || "").trim();
  if (!text) return;

  textInputEl.value = text;
  setStatus("Selection received. Translating...");
  await saveSettings();
  await translateText(text);
});

// Load settings and maybe translation immediately if latestText exists.
loadSettings().then(async () => {
  const existingText = (textInputEl.value || "").trim();
  if (existingText) {
    setStatus("Selected text loaded. Translating...");
    await translateText(existingText);
  } else {
    setStatus("Waiting for selection...");
  }
});

// (no chrome.storage listener in this simpler version)

