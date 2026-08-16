const { app, BrowserWindow } = require("electron");
const { spawn } = require("child_process");
const fs = require("fs");
const http = require("http");
const path = require("path");

let backendProcess = null;
let mainWindow = null;

const BACKEND_PORT = 18080;
const BACKEND_URL = `http://127.0.0.1:${BACKEND_PORT}`;

function jarPath() {
  return path.join(process.resourcesPath, "lcu-query.jar");
}

function startBackend() {
  const jar = jarPath();
  if (!fs.existsSync(jar)) {
    console.warn("Backend jar not found at", jar);
    return;
  }
  backendProcess = spawn("java", ["-jar", jar], {
    stdio: "pipe",
    windowsHide: true
  });
  backendProcess.stdout.on("data", (chunk) => process.stdout.write(chunk));
  backendProcess.stderr.on("data", (chunk) => process.stderr.write(chunk));
  backendProcess.on("exit", (code) => {
    console.log("Backend exited", code);
    backendProcess = null;
  });
}

function waitForBackend(timeoutMs = 20000) {
  const started = Date.now();
  return new Promise((resolve, reject) => {
    const ping = () => {
      const req = http.get(`${BACKEND_URL}/api/status`, (res) => {
        res.resume();
        if (res.statusCode && res.statusCode < 500) {
          resolve();
          return;
        }
        retry();
      });
      req.on("error", retry);
      req.setTimeout(1000, () => {
        req.destroy();
        retry();
      });
    };
    const retry = () => {
      if (Date.now() - started > timeoutMs) {
        reject(new Error("Backend did not start in time"));
        return;
      }
      setTimeout(ping, 300);
    };
    ping();
  });
}

async function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 840,
    minWidth: 960,
    minHeight: 640,
    backgroundColor: "#0b0f16",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true
    }
  });

  const devUrl = process.env.VITE_DEV_SERVER_URL;
  if (devUrl) {
    await mainWindow.loadURL(devUrl);
    mainWindow.webContents.openDevTools({ mode: "detach" });
    return;
  }

  if (!process.env.SKIP_BACKEND) {
    startBackend();
    try {
      await waitForBackend();
      await mainWindow.loadURL(BACKEND_URL);
      return;
    } catch (error) {
      console.error(error);
    }
  }

  await mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));
}

function stopBackend() {
  if (!backendProcess) {
    return;
  }
  if (process.platform === "win32") {
    spawn("taskkill", ["/pid", String(backendProcess.pid), "/f", "/t"]);
  } else {
    backendProcess.kill("SIGTERM");
  }
  backendProcess = null;
}

app.whenReady().then(createWindow);

app.on("window-all-closed", () => {
  stopBackend();
  app.quit();
});

app.on("before-quit", stopBackend);
