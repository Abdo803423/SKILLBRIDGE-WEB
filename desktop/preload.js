const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('skillbridgeDesktop', {
  isNativeDesktop: true,
  platform: process.platform,
  version: '1.0.0',
  getServerConfig: () => ipcRenderer.invoke('skillbridge:get-server-config'),
  setServerUrl: (url) => ipcRenderer.invoke('skillbridge:set-server-url', url),
  reloadApp: () => ipcRenderer.send('skillbridge:reload-app'),
  closeDialog: () => ipcRenderer.send('skillbridge:close-server-dialog'),
});
