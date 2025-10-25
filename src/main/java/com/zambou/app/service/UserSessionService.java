package com.zambou.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinSession;
import com.zambou.app.model.User;
import com.zambou.app.storage.UserStorageManager;

/**
 * Dienstklasse zur Verwaltung der aktuellen Benutzersitzung in einer Vaadin-Anwendung.
 * <p>
 * Diese Klasse ermöglicht den Zugriff auf den eingeloggten {@link User} aus der {@link VaadinSession}
 * sowie das Speichern dieses Nutzers über einen {@link UserStorageManager}.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class UserSessionService {
	
	private static final Logger log = LoggerFactory.getLogger(UserSessionService.class);

	/**
     * Gibt den aktuell in der {@link VaadinSession} gespeicherten Nutzer zurück.
     * <p>
     * Der Nutzer muss zuvor in der Session abgelegt worden sein, z. B. nach dem Login.
     *
     * @return aktueller {@link User} oder {@code null}, wenn kein Nutzer vorhanden ist
     */
	public static User getUser() {
		VaadinSession session = VaadinSession.getCurrent();
		return session.getAttribute(User.class);
	}
	
	/**
     * Persistiert den aktuell eingeloggten Nutzer mithilfe des {@link UserStorageManager}.
     * <p>
     * Falls kein Nutzer in der Session vorhanden ist, wird eine Warnung im Log ausgegeben.
     * Bei einem Fehler während des Speicherns wird eine {@link RuntimeException} geworfen.
     */
	public static void storeUser() {
        User user = getUser();
        if (user != null) {
	        try (UserStorageManager storageManager = new UserStorageManager()) {
	            storageManager.save(user);
	        } catch (Exception e) {
	            throw new RuntimeException("Fehler beim Speichern des Nutzers", e);
	        }
	    } else {
	    	log.warn("Kein Benutzer in der Session gespeichert!");
	    }
    }
}