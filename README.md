# ICP Android (v3)

Dieses Repository enthält die Portierung des bisherigen ICP-Konsolenprogramms auf Android mit grafischer Oberfläche.

## Funktionen

- **Zinsen berechnen**
  - über Anzahl der Tage
  - über Anfangsdatum und Enddatum
  - Methoden: täglich und jährlich
- **Aufträge verwalten**
  - Auftrag erstellen
  - Auftrag auslesen
  - Auftrag löschen (mit Bestätigung)
- **Cloud-Sync (Dropbox)**
  - OAuth-Setup über Browser/Redirect
  - Access-/Refresh-Token-Verwaltung
  - automatischer Token-Refresh
  - Upload/Download von `/Zinsen/save.toml`
- **Lokale Speicherung im privaten App-Speicher**
  - `context.getFilesDir()/save.toml`
  - `context.getFilesDir()/set.toml`

## Wichtige Kompatibilität

Das bestehende TOML-Format bleibt erhalten:

```toml
[Auftragsname]
money=10.0
zins=40.0
methode=2
date=09.08.2026
```

Vorhandene `save.toml`-Dateien können weiterhin gelesen, bearbeitet, gelöscht und gespeichert werden.

## TOML-Quelle aus `f_lib`

Die benötigte Konfigurationslogik wurde direkt in dieses Repository übernommen:

- `app/src/main/java/com/ktf/config/Config.java`

Es besteht **keine Build-Abhängigkeit** auf das private Repository `f_lib`.

## Build-Anleitung

Voraussetzungen:

- JDK 17
- Android SDK (API 34)

Debug-APK bauen:

```bash
./gradlew :app:assembleDebug
```

Tests ausführen:

```bash
./gradlew :app:testDebugUnitTest
```

Die Debug-APK liegt anschließend unter:

- `app/build/outputs/apk/debug/app-debug.apk`

## APK installieren

Beispiel mit `adb`:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Bedienung der App

1. **Hauptmenü**
   - Zinsen berechnen
   - Aufträge (erstellen/auslesen)
   - Settings (Cloud-Sync)
2. **Zinsen berechnen**
   - Eingaben erfassen, Ergebnis wird auf separater Seite angezeigt
3. **Auftrag erstellen**
   - gleiche Berechnungsdaten wie bei normaler Berechnung
   - zusätzlicher Auftragsname + Auftragsdatum
4. **Auftrag auslesen**
   - alle Aufträge als Liste
   - Detailansicht mit aktueller Tages-/Zinsberechnung
   - Löschen mit Bestätigungsdialog

## Dropbox-App einrichten

1. Dropbox-App unter https://www.dropbox.com/developers/apps erstellen
2. Berechtigungen aktivieren:
   - `files.metadata.write`
   - `files.content.write`
   - `files.content.read`
3. In der App unter **Settings → Cloud-Sync einrichten** App Key und App Secret eintragen
4. OAuth im Browser durchführen
5. Die App verwendet die Datei:
   - `/Zinsen/save.toml`

## Datenschutz- und Token-Hinweise

- Access Token, Refresh Token und Ablaufzeit werden lokal in der privaten Datei `set.toml` gespeichert.
- Es werden keine privaten Zugangsdaten im Repository hinterlegt.
- Netzwerkfehler werden abgefangen und mit verständlichen Meldungen angezeigt.
- Upload/Download laufen asynchron (nicht im UI-Thread).

## Hinweise zur bisherigen CLI

Die bisherige CLI-Quelle bleibt im Repository vorhanden (`src/zinsen`).
