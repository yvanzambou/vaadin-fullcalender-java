package com.zambou.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Dienstklasse zum Laden von CSV-Ressourcen aus dem Klassenpfad und
 * zum Erstellen temporärer Dateien für die Weiterverarbeitung.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class CSVLoader {

	/**
     * Lädt eine CSV-Datei aus dem Klassenpfad und speichert sie als temporäre Datei.
     * <p>
     * Diese Methode sucht die angegebene Ressource im Klassenpfad, kopiert deren Inhalt
     * in eine temporäre Datei und gibt den absoluten Pfad dieser Datei zurück.
     * Die temporäre Datei wird mit dem Präfix {@code klausuren-} und der Endung {@code .csv} erstellt.
     *
     * @param resourcePath Pfad zur CSV-Ressource im Klassenpfad (z. B. {@code "csv/klausuren.csv"})
     * @return absoluter Pfad zur temporär erstellten CSV-Datei
     * @throws IOException wenn die Ressource nicht gefunden wird oder ein Fehler beim Schreiben auftritt
     */
    public static String getTempFilePath(String resourcePath) throws IOException {
    	
    	try (InputStream input = CSVLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
            	throw new IOException("CSV-Datei nicht gefunden: " + resourcePath);
            }
            Path temp = Files.createTempFile("klausuren-", ".csv");
            Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp.toAbsolutePath().toString();
        }
    }
}