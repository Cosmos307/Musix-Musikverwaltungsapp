# Musix – Mobile Computing Projekt

## Projektbeschreibung

Musix ist eine App zur Katalogisierung von Musik. Sie ermöglicht das Verwalten, Suchen und Bewerten von Musikstücken, Alben, Künstlern und Playlists. Die App 2024 wurde von 3 Bachelor-Studenten im Rahmen des von AppsFactory betreuten Moduls "Mobile Computing" entwickelt und erhielt als Bewertung eine 1,7.

Das Backend basiert auf Azure Functions (.NET), das Frontend ist eine Android-App (Kotlin, Jetpack Compose). Zusätzlich wird die LastFM API genutzt, um Metadaten und Informationen zu Songs, Alben und Künstlern abzurufen.

---

## Features

- **Musik-Katalogisierung:** Erfassen und Verwalten von Songs, Alben, Künstlern und Playlists
- **Suche:** Suchfunktion nach Künstlern, Songs, Alben und Playlists
- **Azure-Integration:** Synchronisation und Speicherung der Daten in der Cloud (Azure Table Storage)
- **Bewertungen:** Möglichkeit, Songs und Alben zu bewerten
- **Moderne UI:** Umsetzung mit Jetpack Compose
- **Offline-Fähigkeit:** Grundlegende Nutzung auch ohne Internetverbindung möglich

---

## Projektstruktur

```
Mobile-Computing/
│
├── Musix-App/                   # Android-App (Kotlin, Jetpack Compose)
│
└── Muix-Azure-Backend-main/     # Azure Functions Backend (.NET)
```

---

## Installation & Ausführung

### Android-App

1. Öffne das Projekt `Musix-App` in Android Studio.
2. Stelle sicher, dass ein aktuelles Android SDK installiert ist.
3. Baue und starte die App auf einem Emulator oder Android-Gerät.

## Bedienung

- Beim Start der App können Musikstücke, Alben, Künstler und Playlists durchsucht, hinzugefügt und bewertet werden.
- Die Synchronisation mit Azure erfolgt automatisch im Hintergrund.
- Über die Suchfunktion können Einträge schnell gefunden werden.

---

## Entwickler

- Max 
- K. S.
- L. M.

---

## Weiterführende Links

- [Azure Functions Dokumentation](https://learn.microsoft.com/en-us/azure/azure-functions/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Last.fm API Dokumentation](https://www.last.fm/api)