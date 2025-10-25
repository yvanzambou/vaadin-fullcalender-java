package com.zambou.app.model;

/**
 * Repräsentiert eine einzelne Klausur als Event im Kalender
 * mit relevanten Metadaten wie Titel, Zeitrahmen, Gruppen, Prüfer und Raum.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class Event {

	/** Eindeutige ID des Events */
    private int id;

    /** Titel der Event */
    private String title;

    /** Startzeitpunkt im ISO-Format */
    private String start;

    /** Endzeitpunkt im ISO-Format */
    private String end;

    /** Zugeordnete Gruppen */
    private String groups;

    /** Name des Prüfers */
    private String examiner;

    /** Raum */
    private String rooms;

    /**
     * Erstellt ein neues {@code Event}-Objekt mit allen relevanten Informationen.
     *
     * @param id        eindeutige ID des Events
     * @param title     Titel des Events
     * @param start     Startzeitpunkt im ISO-Format
     * @param end       Endzeitpunkt im ISO-Format
     * @param groups    zugeordnete Gruppen
     * @param examiner  Name des Prüfers
     * @param rooms     Raum
     */
	public Event(int id, String title, String start, String end, String groups, String examiner, String rooms) {
		this.id = id;
		this.title = title;
		this.start = start;
		this.end = end;
		this.groups = groups;
		this.examiner = examiner;
		this.rooms = rooms;
	}

	/**
     * Gibt die ID des Events zurück.
     *
     * @return ID des Events
     */
    public int getId() {
        return id;
    }

    /**
     * Gibt den Titel des Events zurück.
     *
     * @return Titel des Events
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gibt den Startzeitpunkt zurück.
     *
     * @return Startzeitpunkt im ISO-Format
     */
    public String getStart() {
        return start;
    }

    /**
     * Gibt den Endzeitpunkt zurück.
     *
     * @return Endzeitpunkt im ISO-Format
     */
    public String getEnd() {
        return end;
    }

    /**
     * Gibt die zugeordneten Gruppen zurück.
     *
     * @return Gruppenbezeichnung(en)
     */
    public String getGroups() {
        return groups;
    }

    /**
     * Gibt den Namen des Prüfers zurück.
     *
     * @return Name des Prüfers
     */
    public String getExaminer() {
        return examiner;
    }

    /**
     * Gibt die Raum zurück.
     *
     * @return Raum/Raüme
     */
    public String getRooms() {
        return rooms;
    }

    /**
     * Liefert eine kompakte textuelle Repräsentation des Events.
     *
     * @return String mit ID, Titel und Prüfer
     */
    @Override
    public String toString() {
        return "Event [id=" + id + ", title=" + title + ", examiner=" + examiner + "]";
    }
	
}