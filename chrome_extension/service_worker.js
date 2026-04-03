// Service worker for creating the context menu and sending the selected text
// to the side panel so it can call the REST endpoint immediately.

const MENU_ID = "darija_translate_selection";
const DEFAULT_BASE_URL = "http://localhost:8080/darija-translator/api/translate";

function openSidePanelForTab(tabId) {
  return chrome.sidePanel.open({ tabId });
}

async function ensureContextMenu() {
  try {
    chrome.contextMenus.removeAll();
  } catch (e) {}

  chrome.contextMenus.create({
    id: MENU_ID,
    title: "Translate to Darija",
    contexts: ["selection"],
  });
}

// Ensure menu exists even if `onInstalled` didn’t run for this reload.
ensureContextMenu();

chrome.runtime.onInstalled.addListener(async () => {
  await ensureContextMenu();

  // Initialize defaults if not set.
  const existing = await chrome.storage.local.get(["baseUrl", "username", "password"]);
  if (!existing.baseUrl) {
    await chrome.storage.local.set({ baseUrl: DEFAULT_BASE_URL });
  }
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (!tab || typeof tab.id !== "number") return;
  const selectionText = (info.selectionText || "").trim();
  if (!selectionText) return;

  // Persist latest selection so side panel can also use it when it reloads.
  await chrome.storage.local.set({ latestText: selectionText });

  await openSidePanelForTab(tab.id);

  // Send message to the side panel.
  chrome.runtime.sendMessage({
    type: "TRANSLATE_SELECTION",
    text: selectionText,
  });
});

