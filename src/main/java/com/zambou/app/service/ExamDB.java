package com.zambou.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.zambou.app.model.Event;
import com.zambou.app.model.Exam;

/**
 * Die {@code ExamDB}-Klasse verwaltet eine Sammlung von {@link Exam}-Objekten,
 * die aus einer CSV-Datei geladen werden. Sie bietet Funktionen zur Filterung,
 * Analyse und Konvertierung der Daten in Kalenderereignisse.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class ExamDB {
	
	private static final Logger log = LoggerFactory.getLogger(ExamDB.class);
	private List<Exam> exams;
	
	 /**
     * Erstellt eine neue Instanz der {@code ExamDB} und lädt die Prüfungsdaten
     * aus der angegebenen CSV-Datei.
     *
     * @param csvPath Pfad zur CSV-Datei mit den Prüfungsdaten
     */
	public ExamDB(String csvPath) {
		loadExamsFromCsvFile(csvPath);
	}

	/**
     * Liest die CSV-Datei ein und wandelt die Daten in {@link Exam}-Objekte um.
     *
     * @param csvPath Pfad zur CSV-Datei
     */
	private void loadExamsFromCsvFile(String csvPath) {
		exams = new ArrayList<>();
		try (Reader reader = new FileReader(csvPath)) {
			CsvToBean<Exam> bean = new CsvToBeanBuilder<Exam>(reader)
					.withType(Exam.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			exams = bean.parse();
		} catch (Exception e) {
			log.error("Fehler beim Einlesen der CSV-Datei: {}", e.getMessage(), e);
		}
	}
	
	/**
     * Gibt alle geladenen Prüfungen zurück.
     *
     * @return Liste aller {@link Exam}-Objekte
     */
	public List<Exam> getAllExams() {
		return exams;
	}
	
	/**
     * Filtert die Prüfungen anhand der angegebenen Kriterien.
     *
     * @param group     Name der Gruppe (optional)
     * @param examiner  Name des Prüfers (optional)
     * @param room      Raumbezeichnung (optional)
     * @param examName  Name der Prüfung (optional)
     * @return Liste der passenden {@link Exam}-Objekte
     */
	public List<Exam> getFilteredExams(String group, String examiner, String room, String examName) {
		return exams.stream()
				.filter(exam ->
							(group == null || group.isEmpty() || exam.getAllGroupsAsList().contains(group)) &&
							(examiner == null || examiner.isEmpty() || exam.getExaminer().contains(examiner)) &&
							(room == null || room.isEmpty() || exam.getAllRoomsAsList().contains(room)) &&
							(examName == null || examName.isEmpty() || exam.getName().contains(examName))
						)
				.collect(Collectors.toList());
	}
	
	/**
     * Extrahiert eindeutige Werte aus den Prüfungsdaten mithilfe eines Mappers.
     *
     * @param mapper Funktion, die eine Liste von Strings aus einem {@link Exam} extrahiert
     * @return sortierte Menge eindeutiger Werte
     */
	private Set<String> extractUniqueValues(Function<Exam, List<String>> mapper) {
		return exams.stream()
				.flatMap(e -> mapper.apply(e).stream())
				.map(String::trim)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
     * Gibt alle vorkommenden Gruppen zurück.
     *
     * @return sortierte Menge aller Gruppen
     */
	public Set<String> getAllGroups() {
		return extractUniqueValues(Exam::getAllGroupsAsList);
	}

	/**
     * Gibt alle vorkommenden Räume zurück.
     *
     * @return sortierte Menge aller Räume
     */
	public Set<String> getAllRooms() {
		return extractUniqueValues(Exam::getAllRoomsAsList);
	}
	
	/**
     * Gibt alle vorkommenden Prüfungsnamen zurück.
     *
     * @return sortierte Menge aller Prüfungsnamen
     */
	public Set<String> getAllNames() {
	    return exams.stream()
	                .map(Exam::getName)
	                .map(String::trim)
	                .collect(Collectors.toCollection(TreeSet::new));
	}

	/**
     * Gibt alle vorkommenden Prüfer zurück.
     *
     * @return sortierte Menge aller Prüfer
     */
	public Set<String> getAllExaminers() {
	    return exams.stream()
	                .map(Exam::getExaminer)
	                .map(String::trim)
	                .collect(Collectors.toCollection(TreeSet::new));
	}
	
	 /**
     * Wandelt eine Liste von Prüfungen in Kalenderereignisse um.
     *
     * @param exams Liste von {@link Exam}-Objekten
     * @return Liste von {@link Event}-Objekten für die Kalenderdarstellung
     */
	public List<Event> getExamsAsEvents(List<Exam> exams) {
		List<Event> events = new ArrayList<>();
		
		DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		int currentYear = LocalDate.now().getYear();
		
		for(Exam ex: exams) {
			String cleanedDate = ex.getDate().split(",")[1].trim() + currentYear; // von "Do., 26.06" wird nur "26.06" weiter betrachtet
			String dateTime = cleanedDate + " " + ex.getTime();
			try {
				LocalDateTime start = LocalDateTime.parse(dateTime, inputFormat);
				LocalDateTime end = start.plusMinutes(150);			// Startzeit + 150 Minuten (gesamte Klausurdauer)
				
				events.add(new Event(
						ex.getId(),
						ex.getName(),
						start.format(outputFormat),
						end.format(outputFormat),
						ex.getGroups(),
						ex.getExaminer(),
						ex.getRooms()
				));
			} catch (DateTimeParseException e) {
				log.error("Ungültiges Datum: {}", dateTime);
			}
		}
		return events;
	}

	/**
     * Gibt alle Prüfungstermine als sortierte {@link LocalDate}-Liste zurück.
     *
     * @return Liste aller Prüfungstage in aufsteigender Reihenfolge
     */
	public List<LocalDate> getSortedDatesAsLocalDate() {
	    return getExamsAsEvents(exams).stream()
	        .map(ev -> LocalDateTime.parse(ev.getStart()))
	        .sorted()
	        .map(LocalDateTime::toLocalDate)
	        .toList();
	}
	
	/**
     * Sucht eine Prüfung anhand ihrer ID.
     *
     * @param id eindeutige Prüfungs-ID
     * @return {@link Exam}-Objekt oder {@code null}, falls nicht gefunden
     */
	public Exam getExamById(Integer id) {
	    return exams.stream()
	                .filter(ex -> ex.getId() == id)
	                .findFirst()
	                .orElse(null);
	}
	
	/**
     * Sucht ein Kalenderereignis anhand der Prüfungs-ID.
     *
     * @param id eindeutige Prüfungs-ID
     * @return {@link Event}-Objekt oder {@code null}, falls nicht gefunden oder fehlerhaftes Datum
     */
	public Event getEventById(Integer id) {
	    DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	    DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	    int currentYear = LocalDate.now().getYear();

	    for (Exam ex : exams) {
	        if (ex.getId() == id) {
	            String cleanedDate = ex.getDate().split(",")[1].trim() + currentYear;
	            String dateTime = cleanedDate + " " + ex.getTime();
	            try {
	                LocalDateTime start = LocalDateTime.parse(dateTime, inputFormat);
	                LocalDateTime end = start.plusMinutes(150);

	                return new Event(
	                        ex.getId(),
	                        ex.getName(),
	                        start.format(outputFormat),
	                        end.format(outputFormat),
	                        ex.getGroups(),
	                        ex.getExaminer(),
	                        ex.getRooms()
	                );
	            } catch (DateTimeParseException e) {
	                log.error("Ungültiges Datum: {}", dateTime);
	                return null;
	            }
	        }
	    }

	    return null;
	}

}