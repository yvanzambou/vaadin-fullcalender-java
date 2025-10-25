package com.zambou.app.model;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Repräsentiert einen Nutzer im System. Jeder Nutzer besitzt eine UUID und eine Menge
 * von ausgewählten Klausur-IDs, die z. B. für Export oder Markierung genutzt werden können.
 *
 * Diese Klasse ist serialisierbar, um sie z. B. in einer Embedded-Datenbank (SQLite) oder Session (Vaadin) zu speichern.
 *
 * @author Yvan Zambou
 * @version 1.0
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private UUID uuid;
	private Set<Integer> ids;
	
	/**
	 * Erstellt einen neuen Benutzer mit der angegebenen UUID und einer Menge von IDs.
	 * 
	 * @param uuid die eindeutige Nutzer-ID
	 * @param ids die Menge der Klausur-IDs
	 */
	public User(UUID uuid, Set<Integer> ids) {
		this.uuid = uuid;
		this.ids = ids;
	}

	/**
	 * Gibt die UUID des Nutzers zurück.
	 * @return UUID des Nutzers
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Setzt die UUID des Nutzers.
	 * @param uuid neue UUID
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gibt die gesetzten Klausur-IDs des Nutzers zurück.
	 * @return Set von Integer-IDs
	 */
	public Set<Integer> getIds() {
		return ids;
	}

	/**
	 * Setzt die Menge der Klausur-IDs.
	 * @param ids neue ID-Menge
	 */
	public void setIds(Set<Integer> ids) {
		this.ids = ids;
	}
	
	/**
	 * Gibt die Anzahl der ausgewählten Klausur-IDs zurück.
	 * @return Anzahl der IDs
	 */
	public int getIdCount() {
		return ids.size();
	}
	
	/**
	 * Fügt eine Klausur-ID hinzu.
	 * @param id ID der Klausur
	 */
	public void addId(Integer id) {
		ids.add(id);
	}

	/**
	 * Entfernt eine Klausur-ID.
	 * @param id zu entfernende ID
	 */
	public void removeId(Integer id) {
		ids.remove(id);
	}
	
	/**
	 * Entfernt alle Klausur-IDs.
	 */
	public void removeAllId() {
		ids.clear();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    User other = (User) obj;
	    return uuid.equals(other.uuid);
	}

	@Override
	public int hashCode() {
	    return uuid.hashCode();
	}
}