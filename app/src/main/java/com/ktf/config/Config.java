package com.ktf.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Erweiterte Konfigurationsklasse mit vollständiger Sektionsunterstützung.
 * <p>
 * Diese Klasse ermöglicht das Lesen, Schreiben und Verwalten von Konfigurationen
 * in einer Textdatei. Sie unterstützt sowohl einfache Key-Value-Konfigurationen
 * als auch strukturierte Konfigurationen mit Sektionen.
 * </p>
 * 
 * <h2>Beispiel für ein Konfigurationsdatei-Format:</h2>
 * <pre>
 * # Globale Einstellungen
 * app.name=MyApplication
 * app.version=1.0.0
 * 
 * [Database]
 * host=localhost
 * port=3306
 * username=admin
 * </pre>
 * 
 * @author KTF
 * @version 5.0
 * @since 1.0
 */
public class Config {
    
    /** Pfad zur Konfigurationsdatei */
    private final Path configPath;
    
    /** Debug-Modus aktiviert/deaktiviert */
    private final boolean debug;

    /**
     * Erstellt eine neue Konfigurationsinstanz mit Debug-Option.
     * 
     * @param configPath Pfad zur Konfigurationsdatei (darf nicht null sein)
     * @param debug      true aktiviert Debug-Ausgaben, false deaktiviert sie
     * @throws NullPointerException wenn configPath null ist
     */
    public Config(String configPath, boolean debug) {
        this.configPath = Paths.get(Objects.requireNonNull(configPath, "Config path cannot be null"));
        this.debug = debug;
        
        if (debug) {
            System.out.println("[DEBUG] Konfiguration initialisiert: " + configPath);
        }
    }

    /**
     * Erstellt eine neue Konfigurationsinstanz ohne Debug-Modus.
     * 
     * @param configPath Pfad zur Konfigurationsdatei (darf nicht null sein)
     * @throws NullPointerException wenn configPath null ist
     */
    public Config(String configPath) {
        this(configPath, false);
    }

    // ==================== BASIS METHODEN ====================

    /**
     * Liest einen globalen Wert aus der Konfiguration.
     * 
     * @param key Der Schlüssel des Wertes (darf nicht null sein)
     * @return Der Wert des Schlüssels oder null, wenn der Schlüssel nicht gefunden wurde
     * @throws NullPointerException wenn key null ist
     */
    public String getConfig(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        try {
            List<String> lines = readConfigFile();
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith(key + "=") && !trimmedLine.startsWith("[")) {
                    return trimmedLine.substring(key.length() + 1).trim();
                }
            }
        } catch (IOException e) {
            handleException("Lesen der Konfiguration", e);
        }
        return null;
    }

    /**
     * Setzt einen globalen Wert in der Konfiguration.
     * 
     * @param key   Der Schlüssel (darf nicht null sein)
     * @param value Der Wert (darf nicht null sein)
     * @return true wenn erfolgreich, false bei Fehler
     * @throws NullPointerException wenn key oder value null ist
     */
    public boolean setConfig(String key, String value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        try {
            List<String> configLines = readConfigFile();
            boolean keyFound = false;

            for (int i = 0; i < configLines.size(); i++) {
                String trimmedLine = configLines.get(i).trim();
                if (trimmedLine.startsWith(key + "=") && !trimmedLine.startsWith("[")) {
                    configLines.set(i, key + "=" + value);
                    keyFound = true;
                    break;
                }
            }

            if (!keyFound) {
                configLines.add(key + "=" + value);
            }

            writeConfigFile(configLines);
            return true;
        } catch (IOException e) {
            handleException("Setzen der Konfiguration für Schlüssel: " + key, e);
            return false;
        }
    }

    /**
     * Fügt einen globalen Schlüssel-Wert-Paar hinzu, ohne vorhandene zu überschreiben.
     * 
     * @param key   Der Schlüssel (darf nicht null sein)
     * @param value Der Wert (darf nicht null sein)
     * @return true wenn der Schlüssel erfolgreich hinzugefügt wurde,
     *         false wenn der Schlüssel bereits existiert oder ein Fehler auftrat
     * @throws NullPointerException wenn key oder value null ist
     */
    public boolean addConfig(String key, String value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        try {
            List<String> configLines = readConfigFile();
            
            boolean keyExists = configLines.stream()
                .anyMatch(line -> line.trim().startsWith(key + "=") && !line.trim().startsWith("["));

            if (!keyExists) {
                configLines.add(key + "=" + value);
                writeConfigFile(configLines);
                return true;
            }
            return false;
        } catch (IOException e) {
            handleException("Hinzufügen der Konfiguration für Schlüssel: " + key, e);
            return false;
        }
    }

    /**
     * Entfernt einen globalen Schlüssel aus der Konfiguration.
     * 
     * @param key Der zu entfernende Schlüssel (darf nicht null sein)
     * @return true wenn der Schlüssel erfolgreich entfernt wurde,
     *         false wenn der Schlüssel nicht gefunden wurde oder ein Fehler auftrat
     * @throws NullPointerException wenn key null ist
     */
    public boolean removeConfig(String key) {
        Objects.requireNonNull(key, "Key cannot be null");

        try {
            List<String> configLines = readConfigFile();
            List<String> updatedLines = configLines.stream()
                .filter(line -> !line.trim().startsWith(key + "=") || line.trim().startsWith("["))
                .collect(Collectors.toList());

            if (updatedLines.size() < configLines.size()) {
                writeConfigFile(updatedLines);
                return true;
            }
            return false;
        } catch (IOException e) {
            handleException("Entfernen der Konfiguration für Schlüssel: " + key, e);
            return false;
        }
    }

    // ==================== SEKTIONS METHODEN ====================

    /**
     * Erstellt eine neue Sektion in der Konfiguration.
     * 
     * @param section Name der zu erstellenden Sektion (darf nicht null sein)
     * @return true wenn die Sektion erfolgreich erstellt wurde,
     *         false wenn die Sektion bereits existiert oder ein Fehler auftrat
     * @throws NullPointerException wenn section null ist
     */
    public boolean createSection(String section) {
        Objects.requireNonNull(section, "Section cannot be null");

        if (debug) {
            System.out.printf("[DEBUG] Erstelle Sektion '%s'%n", section);
        }

        try {
            List<String> configLines = readConfigFile();
            
            boolean sectionExists = configLines.stream()
                .anyMatch(line -> isSectionLine(line, section));

            if (sectionExists) {
                if (debug) {
                    System.out.printf("[DEBUG] Sektion '%s' existiert bereits%n", section);
                }
                return false;
            }

            if (!configLines.isEmpty() && !configLines.get(configLines.size() - 1).trim().isEmpty()) {
                configLines.add("");
            }
            configLines.add("[" + section + "]");
            writeConfigFile(configLines);
            return true;
        } catch (IOException e) {
            handleException("Erstellen der Sektion: " + section, e);
            return false;
        }
    }

    /**
     * Löscht eine Sektion und alle darin enthaltenen Schlüssel-Wert-Paare.
     * 
     * @param section Name der zu löschenden Sektion (darf nicht null sein)
     * @return true wenn die Sektion erfolgreich gelöscht wurde,
     *         false wenn die Sektion nicht gefunden wurde oder ein Fehler auftrat
     * @throws NullPointerException wenn section null ist
     */
    public boolean deleteSection(String section) {
        Objects.requireNonNull(section, "Section cannot be null");

        if (debug) {
            System.out.printf("[DEBUG] Lösche Sektion '%s'%n", section);
        }

        try {
            List<String> configLines = readConfigFile();
            List<String> updatedLines = new ArrayList<>();
            boolean inSection = false;
            boolean sectionFound = false;

            for (String line : configLines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    String currentSection = trimmedLine.substring(1, trimmedLine.length() - 1);
                    if (currentSection.equals(section)) {
                        inSection = true;
                        sectionFound = true;
                        continue;
                    } else {
                        inSection = false;
                    }
                }

                if (!inSection) {
                    updatedLines.add(line);
                }
            }

            if (sectionFound) {
                writeConfigFile(updatedLines);
                if (debug) {
                    System.out.printf("[DEBUG] Sektion '%s' wurde gelöscht%n", section);
                }
                return true;
            }

            if (debug) {
                System.out.printf("[DEBUG] Sektion '%s' nicht gefunden%n", section);
            }
            return false;
        } catch (IOException e) {
            handleException("Löschen der Sektion: " + section, e);
            return false;
        }
    }

    /**
     * Fügt einen Schlüssel-Wert-Paar zu einer Sektion hinzu.
     * 
     * @param key     Der Schlüssel (darf nicht null sein)
     * @param value   Der Wert (darf nicht null sein)
     * @param section Die Sektion (darf nicht null sein)
     * @return true wenn der Schlüssel erfolgreich hinzugefügt wurde,
     *         false wenn der Schlüssel bereits existiert oder ein Fehler auftrat
     * @throws NullPointerException wenn key, value oder section null ist
     */
    public boolean addKeyToSection(String key, String value, String section) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        if (debug) {
            System.out.printf("[DEBUG] Füge Key '%s' zu Sektion '%s' hinzu%n", key, section);
        }

        try {
            List<String> configLines = readConfigFile();
            List<String> updatedLines = new ArrayList<>();
            boolean sectionFound = false;
            boolean keyExists = false;
            boolean keyAdded = false;

            for (String line : configLines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    String currentSection = trimmedLine.substring(1, trimmedLine.length() - 1);
                    if (currentSection.equals(section)) {
                        sectionFound = true;
                        updatedLines.add(line);
                        continue;
                    } else {
                        sectionFound = false;
                        updatedLines.add(line);
                        continue;
                    }
                }

                if (sectionFound) {
                    if (trimmedLine.startsWith(key + "=")) {
                        keyExists = true;
                        if (debug) {
                            System.out.printf("[DEBUG] Key '%s' existiert bereits in Sektion '%s'%n", key, section);
                        }
                        updatedLines.add(line);
                        continue;
                    }
                    
                    if (!keyAdded && !keyExists && !trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                        updatedLines.add(key + "=" + value);
                        keyAdded = true;
                    }
                }
                
                updatedLines.add(line);
            }

            // Wenn Sektion nicht gefunden wurde
            if (!sectionFound) {
                if (!updatedLines.isEmpty() && !updatedLines.get(updatedLines.size() - 1).trim().isEmpty()) {
                    updatedLines.add("");
                }
                updatedLines.add("[" + section + "]");
                updatedLines.add(key + "=" + value);
                writeConfigFile(updatedLines);
                return true;
            }

            // Wenn Sektion gefunden aber Key nicht existiert
            if (sectionFound && !keyExists && !keyAdded) {
                updatedLines.add(key + "=" + value);
                writeConfigFile(updatedLines);
                return true;
            }

            // Wenn Key existiert, überschreiben
            if (sectionFound && keyExists) {
                List<String> finalLines = new ArrayList<>();
                boolean inSection = false;
                boolean keyReplaced = false;
                
                for (String line : configLines) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        inSection = trimmed.substring(1, trimmed.length() - 1).equals(section);
                        finalLines.add(line);
                        continue;
                    }
                    
                    if (inSection && trimmed.startsWith(key + "=")) {
                        finalLines.add(key + "=" + value);
                        keyReplaced = true;
                        continue;
                    }
                    
                    finalLines.add(line);
                }
                
                if (!keyReplaced && inSection) {
                    finalLines.add(key + "=" + value);
                }
                
                writeConfigFile(finalLines);
                return true;
            }

            writeConfigFile(updatedLines);
            return true;
            
        } catch (IOException e) {
            handleException("Hinzufügen von Key '" + key + "' zu Sektion '" + section + "'", e);
            return false;
        }
    }

    /**
     * Liest einen Wert aus einer Sektion.
     * 
     * @param key     Der Schlüssel (darf nicht null sein)
     * @param section Die Sektion (darf nicht null sein)
     * @return Der Wert des Schlüssels oder null, wenn der Schlüssel nicht gefunden wurde
     * @throws NullPointerException wenn key oder section null ist
     */
    public String getValueFromSection(String key, String section) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        try {
            List<String> lines = readConfigFile();
            boolean inSection = false;

            for (String line : lines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    inSection = trimmedLine.substring(1, trimmedLine.length() - 1).equals(section);
                    continue;
                }

                if (inSection && trimmedLine.startsWith(key + "=")) {
                    return trimmedLine.substring(key.length() + 1).trim();
                }
            }
        } catch (IOException e) {
            handleException("Lesen von Key '" + key + "' aus Sektion '" + section + "'", e);
        }

        return null;
    }

    /**
     * Setzt einen Wert in einer Sektion.
     * 
     * @param key     Der Schlüssel (darf nicht null sein)
     * @param value   Der Wert (darf nicht null sein)
     * @param section Die Sektion (darf nicht null sein)
     * @return true wenn erfolgreich, false bei Fehler
     * @throws NullPointerException wenn key, value oder section null ist
     */
    public boolean setValueInSection(String key, String value, String section) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        try {
            List<String> configLines = readConfigFile();
            List<String> updatedLines = new ArrayList<>();
            boolean inSection = false;
            boolean keyReplaced = false;

            for (String line : configLines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    inSection = trimmedLine.substring(1, trimmedLine.length() - 1).equals(section);
                    updatedLines.add(line);
                    continue;
                }

                if (inSection && trimmedLine.startsWith(key + "=")) {
                    updatedLines.add(key + "=" + value);
                    keyReplaced = true;
                    continue;
                }

                if (inSection && !keyReplaced && !trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    updatedLines.add(key + "=" + value);
                    keyReplaced = true;
                }

                updatedLines.add(line);
            }

            if (!inSection) {
                if (!updatedLines.isEmpty() && !updatedLines.get(updatedLines.size() - 1).trim().isEmpty()) {
                    updatedLines.add("");
                }
                updatedLines.add("[" + section + "]");
                updatedLines.add(key + "=" + value);
            } else if (!keyReplaced) {
                updatedLines.add(key + "=" + value);
            }

            writeConfigFile(updatedLines);
            return true;
        } catch (IOException e) {
            handleException("Setzen von Wert in Sektion '" + section + "' für Key '" + key + "'", e);
            return false;
        }
    }

    /**
     * Löscht einen Schlüssel aus einer Sektion.
     * 
     * @param key     Der zu löschende Schlüssel (darf nicht null sein)
     * @param section Die Sektion (darf nicht null sein)
     * @return true wenn der Schlüssel erfolgreich gelöscht wurde,
     *         false wenn der Schlüssel nicht gefunden wurde oder ein Fehler auftrat
     * @throws NullPointerException wenn key oder section null ist
     */
    public boolean removeKeyFromSection(String key, String section) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(section, "Section cannot be null");

        try {
            List<String> configLines = readConfigFile();
            List<String> updatedLines = new ArrayList<>();
            boolean inSection = false;
            boolean keyFound = false;

            for (String line : configLines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    inSection = trimmedLine.substring(1, trimmedLine.length() - 1).equals(section);
                    updatedLines.add(line);
                    continue;
                }

                if (inSection && trimmedLine.startsWith(key + "=")) {
                    keyFound = true;
                    continue;
                }

                updatedLines.add(line);
            }

            if (keyFound) {
                writeConfigFile(updatedLines);
                return true;
            }

            return false;
        } catch (IOException e) {
            handleException("Entfernen von Key '" + key + "' aus Sektion '" + section + "'", e);
            return false;
        }
    }

    /**
     * Gibt alle Schlüssel-Wert-Paare einer Sektion zurück.
     * 
     * @param section Die Sektion (darf nicht null sein)
     * @return Ein 2D-Array mit allen Schlüssel-Wert-Paaren der Sektion.
     *         Jede Zeile enthält [Schlüssel, Wert].
     *         Das Array ist leer, wenn die Sektion nicht existiert oder ein Fehler auftrat
     * @throws NullPointerException wenn section null ist
     */
    public String[][] getSectionKeys(String section) {
        Objects.requireNonNull(section, "Section cannot be null");

        List<String[]> keyValuePairs = new ArrayList<>();
        
        try {
            List<String> configLines = readConfigFile();
            boolean inSection = false;

            for (String line : configLines) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    inSection = trimmedLine.substring(1, trimmedLine.length() - 1).equals(section);
                    continue;
                }

                if (inSection && trimmedLine.contains("=") && !trimmedLine.startsWith("#")) {
                    String[] parts = trimmedLine.split("=", 2);
                    if (parts.length == 2) {
                        keyValuePairs.add(new String[]{parts[0].trim(), parts[1].trim()});
                    }
                }
            }
        } catch (IOException e) {
            handleException("Lesen der Keys aus Sektion '" + section + "'", e);
        }

        return keyValuePairs.toArray(new String[0][0]);
    }

    /**
     * Gibt alle Sektionen der Konfiguration zurück.
     * 
     * @return Ein String-Array mit allen Sektionsnamen.
     *         Das Array ist leer, wenn keine Sektionen existieren oder ein Fehler auftrat
     */
    public String[] getAllSections() {
        List<String> sections = new ArrayList<>();
        
        try {
            List<String> configLines = readConfigFile();

            for (String line : configLines) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                    sections.add(trimmedLine.substring(1, trimmedLine.length() - 1));
                }
            }
        } catch (IOException e) {
            handleException("Lesen aller Sektionen", e);
        }

        return sections.toArray(new String[0]);
    }

    /**
     * Gibt alle Sektionen sortiert zurück.
     * 
     * @return Ein sortiertes String-Array mit allen Sektionsnamen
     */
    public String[] getAllSectionsSorted() {
        String[] sections = getAllSections();
        Arrays.sort(sections);
        return sections;
    }

    /**
     * Gibt Sektionen zurück, die mit einem bestimmten Präfix beginnen.
     * 
     * @param prefix Das Präfix (darf nicht null sein)
     * @return Ein String-Array mit den gefundenen Sektionen
     * @throws NullPointerException wenn prefix null ist
     */
    public String[] getSectionsByPrefix(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        String[] sections = getAllSections();
        
        List<String> filtered = new ArrayList<>();
        for (String section : sections) {
            if (section.startsWith(prefix)) {
                filtered.add(section);
            }
        }
        
        return filtered.toArray(new String[0]);
    }

    /**
     * Gibt Sektionen zurück, die einen bestimmten Substring enthalten.
     * 
     * @param substring Der Substring (darf nicht null sein)
     * @return Ein String-Array mit den gefundenen Sektionen
     * @throws NullPointerException wenn substring null ist
     */
    public String[] getSectionsContaining(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        String[] sections = getAllSections();
        
        List<String> filtered = new ArrayList<>();
        for (String section : sections) {
            if (section.contains(substring)) {
                filtered.add(section);
            }
        }
        
        return filtered.toArray(new String[0]);
    }

    /**
     * Gibt Sektionen zurück, die einer Regex entsprechen.
     * 
     * @param regex Die Regex (darf nicht null sein)
     * @return Ein String-Array mit den gefundenen Sektionen
     * @throws NullPointerException wenn regex null ist
     */
    public String[] getSectionsByRegex(String regex) {
        Objects.requireNonNull(regex, "Regex cannot be null");
        String[] sections = getAllSections();
        
        List<String> filtered = new ArrayList<>();
        for (String section : sections) {
            if (section.matches(regex)) {
                filtered.add(section);
            }
        }
        
        return filtered.toArray(new String[0]);
    }

    /**
     * Prüft, ob eine Sektion in der Konfiguration existiert.
     * 
     * @param section Der Name der zu prüfenden Sektion (darf nicht null sein)
     * @return true wenn die Sektion existiert, false sonst oder bei Fehler
     * @throws NullPointerException wenn section null ist
     */
    public boolean sectionExists(String section) {
        Objects.requireNonNull(section, "Section cannot be null");
        String[] sections = getAllSections();
        for (String s : sections) {
            if (s.equals(section)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt die Anzahl der Sektionen zurück.
     * 
     * @return Anzahl der Sektionen
     */
    public int getSectionCount() {
        return getAllSections().length;
    }

    /**
     * Prüft ob eine Sektion leer ist (keine Keys enthält).
     * 
     * @param section Der Sektionsname (darf nicht null sein)
     * @return true wenn die Sektion leer ist oder nicht existiert
     * @throws NullPointerException wenn section null ist
     */
    public boolean isSectionEmpty(String section) {
        Objects.requireNonNull(section, "Section cannot be null");
        String[][] keys = getSectionKeys(section);
        return keys.length == 0;
    }

    /**
     * Gibt alle Keys einer Sektion als String-Array zurück.
     * 
     * @param section Der Sektionsname (darf nicht null sein)
     * @return Ein String-Array mit allen Keys der Sektion
     * @throws NullPointerException wenn section null ist
     */
    public String[] getSectionKeysAsArray(String section) {
        Objects.requireNonNull(section, "Section cannot be null");
        String[][] keyValuePairs = getSectionKeys(section);
        String[] keys = new String[keyValuePairs.length];
        for (int i = 0; i < keyValuePairs.length; i++) {
            keys[i] = keyValuePairs[i][0];
        }
        return keys;
    }

    /**
     * Gibt alle Werte einer Sektion als String-Array zurück.
     * 
     * @param section Der Sektionsname (darf nicht null sein)
     * @return Ein String-Array mit allen Werten der Sektion
     * @throws NullPointerException wenn section null ist
     */
    public String[] getSectionValuesAsArray(String section) {
        Objects.requireNonNull(section, "Section cannot be null");
        String[][] keyValuePairs = getSectionKeys(section);
        String[] values = new String[keyValuePairs.length];
        for (int i = 0; i < keyValuePairs.length; i++) {
            values[i] = keyValuePairs[i][1];
        }
        return values;
    }

    // ==================== HILFSMETHODEN ====================

    /**
     * Prüft, ob eine Zeile eine Sektionsdefinition ist.
     * 
     * @param line    Die zu prüfende Zeile
     * @param section Der Sektionsname
     * @return true wenn die Zeile die Sektion definiert, false sonst
     */
    private boolean isSectionLine(String line, String section) {
        String trimmedLine = line.trim();
        return trimmedLine.startsWith("[") && 
               trimmedLine.endsWith("]") && 
               trimmedLine.substring(1, trimmedLine.length() - 1).equals(section);
    }

    /**
     * Liest die Konfigurationsdatei.
     * 
     * @return Liste der Zeilen oder leere Liste bei Fehler
     * @throws IOException wenn die Datei nicht gelesen werden kann
     */
    private List<String> readConfigFile() throws IOException {
        if (!Files.exists(configPath)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(configPath, StandardCharsets.UTF_8);
    }

    /**
     * Schreibt die Konfigurationsdatei.
     * <p>
     * Verwendet eine temporäre Datei im gleichen Verzeichnis für atomare Schreiboperationen.
     * Falls das fehlschlägt, wird direkt geschrieben.
     * </p>
     * 
     * @param lines Die zu schreibenden Zeilen
     * @throws IOException wenn beim Schreiben ein Fehler auftritt
     */
    private void writeConfigFile(List<String> lines) throws IOException {
        // 1. Verzeichnis erstellen falls nicht vorhanden
        Path parent = configPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
            if (debug) {
                System.out.println("[DEBUG] Verzeichnis erstellt: " + parent);
            }
        }
        
        // 2. Versuche: Temporäre Datei im gleichen Verzeichnis (atomar)
        Path tempFile = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        
        try {
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            Files.move(tempFile, configPath, StandardCopyOption.REPLACE_EXISTING);
            if (debug) {
                System.out.println("[DEBUG] Datei atomar geschrieben: " + configPath);
            }
        } catch (IOException e) {
            // 3. Fallback: Direkt schreiben (wenn atomar nicht möglich)
            Files.deleteIfExists(tempFile);
            if (debug) {
                System.out.println("[DEBUG] Atomares Schreiben fehlgeschlagen, schreibe direkt...");
                System.out.println("[DEBUG] Fehler: " + e.getMessage());
            }
            Files.write(configPath, lines, StandardCharsets.UTF_8);
            if (debug) {
                System.out.println("[DEBUG] Datei direkt geschrieben: " + configPath);
            }
        }
    }

    /**
     * Zentrale Exception-Handling-Methode.
     * 
     * @param operation Die beschreibende Operation
     * @param e         Die aufgetretene Exception
     */
    private void handleException(String operation, IOException e) {
        if (debug) {
            System.err.println("[ERROR] Fehler bei " + operation + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DATEI-METHODEN ====================

    /**
     * Prüft, ob die Konfigurationsdatei existiert.
     * 
     * @return true wenn die Datei existiert, false sonst
     */
    public boolean configExists() {
        return Files.exists(configPath);
    }

    /**
     * Gibt den Pfad der Konfigurationsdatei zurück.
     * 
     * @return Der Pfad als String
     */
    public String getConfigPath() {
        return configPath.toString();
    }

    /**
     * Erstellt eine neue Konfigurationsdatei.
     * 
     * @param lines Der Inhalt der neuen Konfigurationsdatei
     * @return true wenn erfolgreich, false bei Fehler
     */
    public boolean createConfigFile(String[] lines) {
        try {
            writeConfigFile(Arrays.asList(lines));
            return true;
        } catch (IOException e) {
            handleException("Erstellen der Konfigurationsdatei", e);
            return false;
        }
    }

    /**
     * Löscht die Konfigurationsdatei.
     * 
     * @return true wenn erfolgreich gelöscht, false wenn nicht existiert oder Fehler
     */
    public boolean deleteConfigFile() {
        try {
            if (Files.exists(configPath)) {
                Files.delete(configPath);
                if (debug) {
                    System.out.println("[DEBUG] Datei gelöscht: " + configPath);
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            handleException("Löschen der Konfigurationsdatei", e);
            return false;
        }
    }

    /**
     * Gibt den Inhalt der Konfigurationsdatei als String zurück.
     * 
     * @return Der Inhalt als String oder null bei Fehler
     */
    public String getConfigContent() {
        try {
            List<String> lines = readConfigFile();
            return String.join(System.lineSeparator(), lines);
        } catch (IOException e) {
            handleException("Lesen des Dateiinhalts", e);
            return null;
        }
    }
}