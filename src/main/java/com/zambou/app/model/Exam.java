package com.zambou.app.model;

import java.util.Arrays;
import java.util.List;

import com.opencsv.bean.CsvBindByName;

/**
 * Repräsentiert eine Klausur, die aus einer CSV-Datei geladen wird.
 * Die Felder sind an Spaltennamen gebunden und werden mit OpenCSV eingelesen.
 *
 * @author Yvan Zambou
 * @version 1.0
 */
public class Exam {

	/** Eindeutige ID der Klausur */
	@CsvBindByName(column = "ID")
	private int id;
	
	/** Datum der Klausur (z. B. "Do., 26.06.") */
	@CsvBindByName(column = "DATUM")
	private String date;
	
	/** Uhrzeit der Klausur im Format "HH:mm" (z. B. "10:30") */
	@CsvBindByName(column = "ZEIT")
	private String time;
	
	/** Kommagetrennte Liste von Gruppenbezeichnungen (z. B. "I4,IP4,M6-MI") */
	@CsvBindByName(column = "GRUPPEN")
	private String groups;
	
	/** Name bzw. Titel der Klausur */
	@CsvBindByName(column = "NAME")
	private String name;
	
	 /** Name des Prüfers oder der Prüferin */
	@CsvBindByName(column = "PRUEFER")
	private String examiner;
	
	/** Kommagetrennte Liste von Raumbezeichnungen (z. B. "Dynexite,Coram") */
	@CsvBindByName(column = "RAEUME")
	private String rooms;

	/**
     * Gibt die eindeutige ID der Klausur zurück.
     * @return die Prüfungs-ID
     */
	public int getId() {
		return id;
	}

	/**
	 * Gibt das Datum der Klausur zurück.
	 * @return das Datum als String
	 */
	public String getDate() {
		return date;
	}

	/**
     * Gibt die Uhrzeit der Klausur zurück.
     * @return die Uhrzeit als String
     */
	public String getTime() {
		return time;
	}

	/**
     * Gibt die Gruppenbezeichnungen als kommagetrennten String zurück.
     * @return die Gruppen als String
     */
	public String getGroups() {
		return groups;
	}
	
	/**
     * Gibt den Namen der Klausur zurück.
     * @return der Prüfungsname
     */
	public String getName() {
		return name;
	}

	/**
     * Gibt den Namen des Prüfers/der Prüferin zurück.
     * @return der Prüfername
     */
	public String getExaminer() {
		return examiner;
	}

	/**
     * Gibt die Raumbezeichnungen als kommagetrennten String zurück.
     * @return die Räume als String
     */
	public String getRooms() {
		return rooms;
	}
	
	/**
     * Gibt alle Gruppen als Liste einzelner Strings zurück.
     * @return Liste der Gruppen
     */
	public List<String> getAllGroupsAsList() {
		return Arrays.stream(groups.split(",")).map(String::trim).toList();
	}

	 /**
     * Gibt alle Räume als Liste einzelner Strings zurück.
     * @return Liste der Räume
     */
	public List<String> getAllRoomsAsList() {
		return Arrays.stream(rooms.split(",")).map(String::trim).toList();
	}
}