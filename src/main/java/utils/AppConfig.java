package utils;

/**
 * Die {@code AppConfig}-Klasse enthält zentrale Konfigurationswerte für die Anwendung.
 * <p>
 * Diese Klasse ist nicht instanziierbar und dient ausschließlich als statischer Container
 * für Konstanten wie Datenbankpfade oder Verzeichnisse.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public final class AppConfig {
	
	/**
     * Privater Konstruktor verhindert die Instanziierung dieser Utility-Klasse.
     */
    private AppConfig() {}

    /**
     * JDBC-URL zur SQLite-Datenbank, die für die persistente Speicherung von Nutzerdaten verwendet wird.
     */
    public static final String SQLITE_DB_URL = "jdbc:sqlite:users-storage.db";

    /**
     * Virtuelles Verzeichnis im eingebetteten Dateisystem zur Ablage von Speicherstrukturen.
     */
    public static final String VIRTUAL_DIRECTORY = "storage";
}