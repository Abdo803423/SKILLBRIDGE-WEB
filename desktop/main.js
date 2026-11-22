const { app, BrowserWindow, Menu, shell, ipcMain } = require('electron');
const fs = require('node:fs');
const path = require('node:path');

const DEFAULT_DEV_URL = 'https://ais-dev-pv257mvjw3hpalpoixracz-1005149195090.europe-west2.run.app';
const SHARED_PROD_URL = 'https://ais-pre-pv257mvjw3hpalpoixracz-1005149195090.europe-west2.run.app';
const LOCAL_URL = 'http://localhost:3000';

let mainWindow = null;
let serverSwitchWindow = null;

function getConfigPath() {
  return path.join(app.getPath('userData'), 'skillbridge-desktop-config.json');
}

function validateServerUrl(rawUrl) {
  try {
    const parsed = new URL(String(rawUrl || '').trim());
    if (parsed.protocol !== 'https:' && parsed.protocol !== 'http:') {
      return null;
    }
    return parsed.toString().replace(/\/+$/, '');
  } catch {
    return null;
  }
}

function loadServerUrl() {
  try {
    const configPath = getConfigPath();
    if (fs.existsSync(configPath)) {
      const data = JSON.parse(fs.readFileSync(configPath, 'utf8'));
      const valid = validateServerUrl(data?.serverUrl);
      if (valid) return valid;
    }
  } catch {
    // Fallback cleanly to default live ecosystem URL
  }
  return DEFAULT_DEV_URL;
}

function saveServerUrl(nextUrl) {
  const valid = validateServerUrl(nextUrl);
  if (!valid) {
    throw new Error('Server URL must start with https:// or http://');
  }
  const configPath = getConfigPath();
  fs.mkdirSync(path.dirname(configPath), { recursive: true });
  fs.writeFileSync(configPath, JSON.stringify({ serverUrl: valid }, null, 2), 'utf8');
  return valid;
}

function isAuthOrAppPopup(targetUrl, appOrigin) {
  try {
    const parsed = new URL(targetUrl);
    if (parsed.origin === appOrigin) return true;
    const host = parsed.hostname.toLowerCase();
    return (
      host === 'accounts.google.com' ||
      host.endsWith('.firebaseapp.com') ||
      host.endsWith('.web.app') ||
      host.endsWith('.googleusercontent.com')
    );
  } catch {
    return false;
  }
}

function loadMainAppUrl(targetUrl) {
  if (!mainWindow || mainWindow.isDestroyed()) return;
  mainWindow.loadURL(targetUrl).catch(() => {
    if (mainWindow && !mainWindow.isDestroyed()) {
      mainWindow.loadFile(path.join(__dirname, 'offline.html'));
    }
  });
}

function openServerSwitchDialog() {
  if (serverSwitchWindow && !serverSwitchWindow.isDestroyed()) {
    serverSwitchWindow.focus();
    return;
  }
  serverSwitchWindow = new BrowserWindow({
    width: 520,
    height: 440,
    resizable: false,
    minimizable: false,
    maximizable: false,
    parent: mainWindow || undefined,
    modal: Boolean(mainWindow),
    title: 'SkillBridge Server Connection',
    icon: path.join(__dirname, 'build', 'icon.ico'),
    backgroundColor: '#0B1F38',
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });
  serverSwitchWindow.loadFile(path.join(__dirname, 'server-switch.html'));
  serverSwitchWindow.on('closed', () => {
    serverSwitchWindow = null;
  });
}

function buildAppMenu() {
  const template = [
    {
      label: 'File',
      submenu: [
        {
          label: 'Reload Workspace',
          accelerator: 'CmdOrCtrl+R',
          click: () => loadMainAppUrl(loadServerUrl()),
        },
        {
          label: 'Switch Central Server...',
          click: () => openServerSwitchDialog(),
        },
        { type: 'separator' },
        { role: 'quit', label: 'Exit SkillBridge' },
      ],
    },
    {
      label: 'Server',
      submenu: [
        {
          label: 'Development Ecosystem (Primary)',
          click: () => {
            const url = saveServerUrl(DEFAULT_DEV_URL);
            loadMainAppUrl(url);
          },
        },
        {
          label: 'Shared Production Ecosystem',
          click: () => {
            const url = saveServerUrl(SHARED_PROD_URL);
            loadMainAppUrl(url);
          },
        },
        {
          label: 'Localhost (http://localhost:3000)',
          click: () => {
            const url = saveServerUrl(LOCAL_URL);
            loadMainAppUrl(url);
          },
        },
        { type: 'separator' },
        {
          label: 'Custom Server URL...',
          click: () => openServerSwitchDialog(),
        },
      ],
    },
    {
      label: 'View',
      submenu: [
        { role: 'resetZoom' },
        { role: 'zoomIn' },
        { role: 'zoomOut' },
        { type: 'separator' },
        { role: 'togglefullscreen' },
        { role: 'toggleDevTools' },
      ],
    },
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
}

function createMainWindow() {
  const activeUrl = loadServerUrl();
  const appOrigin = new URL(activeUrl).origin;

  mainWindow = new BrowserWindow({
    width: 1360,
    height: 860,
    minWidth: 1024,
    minHeight: 700,
    title: 'SkillBridge — Career & Verified Experience Ecosystem',
    icon: path.join(__dirname, 'build', 'icon.ico'),
    backgroundColor: '#0B1F38',
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  const currentUa = mainWindow.webContents.userAgent;
  if (!currentUa.includes('SkillBridgeDesktop')) {
    mainWindow.webContents.userAgent = `${currentUa} SkillBridgeDesktop/1.0.0`;
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow?.show();
  });

  mainWindow.webContents.on('did-fail-load', (_event, errorCode, _desc, validatedUrl, isMainFrame) => {
    if (isMainFrame && errorCode !== -3 && !String(validatedUrl || '').startsWith('file://')) {
      mainWindow?.loadFile(path.join(__dirname, 'offline.html'));
    }
  });

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    const currentOrigin = (() => {
      try {
        return new URL(loadServerUrl()).origin;
      } catch {
        return appOrigin;
      }
    })();

    if (isAuthOrAppPopup(url, currentOrigin)) {
      return {
        action: 'allow',
        overrideBrowserWindowOptions: {
          width: 520,
          height: 680,
          parent: mainWindow || undefined,
          autoHideMenuBar: true,
          webPreferences: {
            contextIsolation: true,
            nodeIntegration: false,
            sandbox: true,
          },
        },
      };
    }

    const parsed = validateServerUrl(url);
    if (parsed) {
      shell.openExternal(parsed);
    }
    return { action: 'deny' };
  });

  loadMainAppUrl(activeUrl);
}

const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }
  });

  app.whenReady().then(() => {
    ipcMain.handle('skillbridge:get-server-config', () => ({
      serverUrl: loadServerUrl(),
      presets: {
        development: DEFAULT_DEV_URL,
        shared: SHARED_PROD_URL,
        local: LOCAL_URL,
      },
    }));

    ipcMain.handle('skillbridge:set-server-url', (_event, rawUrl) => {
      const saved = saveServerUrl(rawUrl);
      loadMainAppUrl(saved);
      if (serverSwitchWindow && !serverSwitchWindow.isDestroyed()) {
        serverSwitchWindow.close();
      }
      return saved;
    });

    ipcMain.on('skillbridge:reload-app', () => {
      loadMainAppUrl(loadServerUrl());
    });

    ipcMain.on('skillbridge:close-server-dialog', () => {
      if (serverSwitchWindow && !serverSwitchWindow.isDestroyed()) {
        serverSwitchWindow.close();
      }
    });

    buildAppMenu();
    createMainWindow();
  });

  app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
      app.quit();
    }
  });
}
