# Diabetify

Android client for Diabetify.

## Local Build
```powershell
Copy-Item local.properties.example local.properties
.\gradlew.bat assembleDebug
```

`local.properties` is intentionally local-only. Use `API_BASE_URL=http://10.0.2.2:8080/` for the Android emulator, or replace it with your laptop IP when testing on a physical device, for example `http://192.168.1.10:8080/`.

## Quality Checks
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```
