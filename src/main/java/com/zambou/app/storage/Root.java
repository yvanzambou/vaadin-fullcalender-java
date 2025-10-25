package com.zambou.app.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.zambou.app.model.User;

/**
 * Die {@code Root}-Klasse stellt die Wurzelstruktur für die persistente Speicherung von {@link User}-Objekten dar.
 * <p>
 * Sie wird typischerweise als Root-Objekt in einem eingebetteten Speicher verwendet (z. B. mit {@code EmbeddedStorageManager}).
 * Die Klasse erlaubt das Abrufen, Hinzufügen und Aktualisieren von Nutzern anhand ihrer UUID.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class Root implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<User> users = new ArrayList<>();

    /**
     * Gibt die vollständige Liste aller gespeicherten Nutzer zurück.
     *
     * @return Liste aller {@link User}-Objekte
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sucht einen Nutzer anhand seiner UUID.
     *
     * @param uuid die eindeutige Kennung des Nutzers
     * @return {@link User}-Objekt mit passender UUID oder {@code null}, falls nicht gefunden oder UUID ungültig
     */
    public User getUserByUUID(UUID uuid) {
        if (uuid == null)
        	return null;
        
        return users.stream()
        		.filter(user -> uuid.equals(user.getUuid()))
        		.findFirst()
        		.orElse(null);
    }

    /**
     * Fügt einen neuen Nutzer hinzu oder aktualisiert einen bestehenden Nutzer mit derselben UUID.
     * <p>
     * Falls bereits ein Nutzer mit der gegebenen UUID existiert, wird dieser ersetzt.
     * Andernfalls wird der neue Nutzer zur Liste hinzugefügt.
     *
     * @param user das hinzuzufügende oder zu aktualisierende {@link User}-Objekt
     */
    public void addOrUpdateUser(User user) {
        if (user == null || user.getUuid() == null)
        	return;

        for (int i = 0; i < users.size(); i++) {
            if (user.getUuid().equals(users.get(i).getUuid())) {
                users.set(i, user);
                return;
            }
        }
        users.add(user);
    }
}