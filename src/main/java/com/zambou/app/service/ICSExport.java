package com.zambou.app.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.vaadin.flow.server.StreamResource;
import com.zambou.app.model.Exam;

/**
 * Dienstklasse zur Erzeugung von ICS-Kalenderdateien aus einer Liste von {@link Exam}-Objekten.
 * <p>
 * Die erzeugte Datei entspricht dem iCalendar-Standard (RFC 5545) und kann in gängige Kalenderanwendungen
 * wie Outlook, Apple Kalender oder Google Kalender importiert werden.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class ICSExport {

	/**
     * Exportiert die übergebenen Prüfungen als ICS-Datei im iCalendar-Format.
     * <p>
     * Jede Prüfung wird als {@code VEVENT} mit Start- und Endzeit, Prüfer, Gruppe und Raum dargestellt.
     * Die Dauer der Prüfung wird pauschal auf 150 Minuten gesetzt. Die erzeugte Datei wird als
     * {@link StreamResource} zurückgegeben und ist direkt für den Download oder die Anzeige geeignet.
     *
     * @param exams Liste der Prüfungen, die exportiert werden sollen
     * @return {@link StreamResource} mit dem Inhalt der ICS-Datei
     */
    public static StreamResource exportToIcs(List<Exam> exams) {
    	
    	DateTimeFormatter utcStamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        int currentYear = LocalDate.now().getYear();
    	
        StringBuilder sb = new StringBuilder();
        
        sb.append("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:hs-emden-leer.de\n");

        for (Exam exam : exams) {
        	
        	String dateTime = exam.getDate().split(",")[1].trim() + currentYear + " " + exam.getTime();
            LocalDateTime start = LocalDateTime.parse(dateTime, inputFormat);
            LocalDateTime end = start.plusMinutes(150);
            
            sb.append("BEGIN:VEVENT\n");
            sb.append("UID:exam-").append(exam.getId()).append("\n");
            sb.append("DTSTAMP:").append(utcStamp.format(Instant.now())).append("\n");
            sb.append("DTSTART:").append(start.format(outputFormat)).append("\n");
            sb.append("DTEND:").append(end.format(outputFormat)).append("\n");
            sb.append("SUMMARY:").append(exam.getName()).append("\n");
            sb.append("DESCRIPTION:").append("Prüfer: " + exam.getExaminer() + " - Gruppe: " + exam.getGroups()).append("\n");
            sb.append("LOCATION:").append(exam.getRooms()).append("\n");
            sb.append("END:VEVENT\n");
        }

        sb.append("END:VCALENDAR");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        StreamResource resource =  new StreamResource("", () -> new ByteArrayInputStream(bytes));
        resource.setContentType("text/calendar; charset=utf-8");  // Wichtig für iOS und Android
        
        return resource;
    }
}