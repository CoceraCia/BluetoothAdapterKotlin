# BluetoothAdapterKotlin 📶🤖

An Android application built with **Kotlin** to demonstrate classic Bluetooth discovery and connection.  
This project was developed as **Activity 1** for the *Multimedia Programming & Mobile Devices* course (UT3), focusing on using `BluetoothAdapter` to enable Bluetooth, discover nearby devices, connect to a selected device, and report connection status to the user.🚀

---

## Features ✨

- **Bluetooth activation** 🔵
  - Checks whether Bluetooth is supported on the device.
  - Requests enabling Bluetooth if it is disabled.
  - Handles runtime permissions required for scanning/connecting. :contentReference[oaicite:3]{index=3}

- **Nearby device discovery** 📡
  - Scans for and displays nearby Bluetooth devices in a list.
  - Keeps the list updated as new devices are found. :contentReference[oaicite:4]{index=4}:contentReference[oaicite:5]{index=5}

- **Device selection & connection** 🔗
  - Allows selecting a discovered device.
  - Attempts a connection to the chosen device.
  - Shows a **clear status message** indicating whether the connection succeeded or failed. :contentReference[oaicite:6]{index=6}:contentReference[oaicite:7]{index=7}

---

## Tech Stack 🛠️

- **Language:** Kotlin  
- **UI:** XML layouts  
- **Bluetooth API:** Android Classic Bluetooth (`BluetoothAdapter`, discovery, connection) :contentReference[oaicite:8]{index=8}

---

## Getting Started 🚀

### 1) Clone the repository 📥

```bash
git clone https://github.com/CoceraCia/BluetoothAdapterKotlin.git
cd BluetoothAdapterKotlin
```

### 2) Open in Android Studio 🧩

- Android Studio Giraffe or newer recommended.
- Let Gradle sync and download dependencies.

### 3) Run on a real device 📱

Bluetooth discovery and connections are best tested on a physical Android phone.
Make sure the device has Bluetooth enabled and is running Android 6.0+.

---

## Permissions 🔐
This app requires Bluetooth-related permissions to scan and connect.

Depending on Android version, you may need:
- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (needed for discovery on older Android)
- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`

The project requests these at runtime when needed.

---

## How It Works (Flow) 🧭

1. Start app
2. Check Bluetooth support and status
3. If Bluetooth is off → ask user to enable
4. Start discovery
5. Show discovered devices in a list
6. User selects a device
7. App attempts connection
8. UI shows success or failure message

---

## Notes 📝
- The discovery process can take a few seconds.
- Some devices may require pairing before a connection can be established.
- Connection success depends on the target device’s Bluetooth profile and availability.

---

## 📽️Demo Video
[![Demo Short](https://img.youtube.com/vi/KyvVc5z6GpI/maxresdefault.jpg)](https://youtube.com/shorts/KyvVc5z6GpI)

---

## Author 😄
My Github profile:

[![Github](https://img.shields.io/badge/Github-CoceraCia-blue?logo=github)](https://github.com/CoceraCia)
 

