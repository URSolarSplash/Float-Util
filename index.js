const {app, process, ipcMain, BrowserWindow} = require('electron')
require('electron-reload')(__dirname);
var Chart = require('chart.js');

let mainWindow;
let backgroundWindow;

// Create the main browser window. This handles all UI and most application logic.
function createMainWindow () {
    mainWindow = new BrowserWindow({width: 1024, height: 768, minWidth: 1024, minHeight: 700})
    mainWindow.loadFile('index.html')
    mainWindow.on('closed', () => {
      mainWindow = null;
      app.quit();
    })
}

// Create the background browser window. This is used to run mesh floatation simulations.
function createBackgroundWindow () {
    backgroundWindow = new BrowserWindow({width: 100, height: 100, show: false})
    backgroundWindow.loadFile('background.html')
    backgroundWindow.on('closed', () => {
      backgroundWindow = null
    })
}

app.on('ready', () => {
    createMainWindow();
    createBackgroundWindow();
});

app.on('window-all-closed', () => { app.quit(); })

app.on('activate-with-no-open-windows', () => {
    if (win === null) {
        createWindow();
    }
});

app.on('before-quit', function(){
    console.log("Quitting...");
});

ipcMain.on('simulation-start', (event, payload) => backgroundWindow.webContents.send('simulation-start', payload));
ipcMain.on('simulation-cancel', (event, payload) => backgroundWindow.webContents.send('simulation-cancel', payload));
ipcMain.on('simulation-update-progress', (event, payload) => mainWindow.webContents.send('simulation-update-progress', payload));
ipcMain.on('simulation-success', (event, payload) => mainWindow.webContents.send('simulation-success', payload));
ipcMain.on('simulation-failure', (event, payload) => mainWindow.webContents.send('simulation-failure', payload));
