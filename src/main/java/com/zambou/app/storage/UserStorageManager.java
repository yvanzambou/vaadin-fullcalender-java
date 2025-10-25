package com.zambou.app.storage;

import java.util.List;
import java.util.UUID;

import org.eclipse.store.afs.sql.types.SqlConnector;
import org.eclipse.store.afs.sql.types.SqlFileSystem;
import org.eclipse.store.afs.sql.types.SqlProviderSqlite;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.sqlite.SQLiteDataSource;

import com.zambou.app.model.User;

import utils.AppConfig;

/**
 * Die {@code UserStorageManager}-Klasse verwaltet die persistente Speicherung von {@link User}-Objekten
 * mithilfe eines eingebetteten Speichermechanismus auf Basis von SQLite.
 * <p>
 * Sie initialisiert den Speicher, lädt die Root-Struktur, erlaubt das Speichern und Abrufen von Nutzern
 * und stellt sicher, dass Ressourcen korrekt freigegeben werden.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class UserStorageManager implements AutoCloseable{

    private final EmbeddedStorageManager storageManager;
    private final Root root;

    /**
     * Erstellt eine neue Instanz des {@code UserStorageManager} und initialisiert den eingebetteten Speicher.
     * <p>
     * Falls keine Root-Struktur vorhanden ist, wird eine neue {@link Root}-Instanz erstellt und gespeichert.
     *
     * @throws RuntimeException falls ein Fehler bei der Initialisierung auftritt
     */
    public UserStorageManager() {
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(AppConfig.SQLITE_DB_URL);

            SqlFileSystem sqlFileSystem = SqlFileSystem.New(SqlConnector.Caching(SqlProviderSqlite.New(dataSource)));
            this.storageManager = EmbeddedStorage.start(sqlFileSystem.ensureDirectoryPath(AppConfig.VIRTUAL_DIRECTORY));

            Root existingRoot = (Root) storageManager.root();
            if (existingRoot == null) {
            	existingRoot = new Root();
                storageManager.setRoot(existingRoot);
                storageManager.storeRoot();
            }
            this.root = existingRoot;
            
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Initialisieren von UserStorageManager", e);
        }
    }

    /**
     * Speichert den übergebenen Nutzer im eingebetteten Speicher.
     * <p>
     * Falls der Nutzer bereits existiert (basierend auf UUID), wird er aktualisiert.
     *
     * @param user der zu speichernde {@link User}
     */
    public void save(User user) {
        root.addOrUpdateUser(user);
        storageManager.store(root.getUsers());
    }

    /**
     * Gibt alle gespeicherten Nutzer zurück.
     *
     * @return Liste aller {@link User}-Objekte
     */
    public List<User> getAllUsers() {
        return root.getUsers();
    }
    
    /**
     * Sucht einen Nutzer anhand seiner UUID.
     *
     * @param uuid eindeutige Kennung des Nutzers
     * @return {@link User}-Objekt oder {@code null}, falls nicht gefunden
     */
    public User getUserById(UUID uuid) {
        return root.getUserByUUID(uuid);
    }

    /**
     * Schließt den Speicher und gibt alle Ressourcen frei.
     *
     * @throws Exception falls beim Herunterfahren ein Fehler auftritt
     */
	@Override
	public void close() throws Exception {
		storageManager.shutdown();
	}
}