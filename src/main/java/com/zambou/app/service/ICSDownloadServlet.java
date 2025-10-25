package com.zambou.app.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.eclipse.store.afs.sql.types.SqlConnector;
import org.eclipse.store.afs.sql.types.SqlFileSystem;
import org.eclipse.store.afs.sql.types.SqlProviderSqlite;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.sqlite.SQLiteDataSource;

import com.zambou.app.model.Exam;
import com.zambou.app.model.User;
import com.zambou.app.storage.Root;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AppConfig;

/**
 * Servlet zur Bereitstellung von ICS-Kalenderdateien für einzelne Nutzer.
 * <p>
 * Die ICS-Datei enthält alle Prüfungen eines Nutzers, identifiziert über eine UUID im Pfad.
 * Beispielhafte Anfrage: {@code GET /ics/ee9abb52-56f9-46a2-88e4-d955fb89181e.ics}
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class ICSDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
     * Verarbeitet eine eingehende HTTP-Anfrage zur Generierung und Auslieferung einer ICS-Datei.
     * <p>
     * Die Methode prüft, ob der Pfad korrekt ist und eine gültige UUID enthält. Anschließend wird
     * die zugehörige ICS-Datei generiert und als Antwort zurückgegeben.
     *
     * @param request  die HTTP-Anfrage mit Pfadinformationen
     * @param response die HTTP-Antwort, in die die ICS-Datei geschrieben wird
     * @throws IOException bei Fehlern beim Schreiben oder Laden der Daten
     */
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String pathInfo = request.getPathInfo();	// z.B.  "/ee9abb52-56f9-46a2-88e4-d955fb89181e.ics"
		
        if (pathInfo == null || !pathInfo.endsWith(".ics")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pfad ungültig.");
            return;
        }
        
        String uuid = pathInfo.substring(pathInfo.lastIndexOf("/") + 1, pathInfo.length() - 4);
        
        if (isValidUUID(uuid)) {
        	byte[] icsData = generateIcsFor(UUID.fromString(uuid));

            if (icsData == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nutzer nicht gefunden.");
                return;
            }

            response.setContentType("text/calendar; charset=UTF-8");
            response.setHeader("Content-Disposition", "inline; filename=\""+ uuid +".ics\"");
            response.getOutputStream().write(icsData);
        	
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige UUID.");
            return;
		}
	}

	/**
     * Generiert eine ICS-Datei für den Nutzer mit der angegebenen UUID.
     * <p>
     * Die Methode lädt die Nutzer- und Prüfungsdaten, wandelt die relevanten Prüfungen
     * in ICS-kompatible VEVENT-Einträge um und gibt die Datei als Byte-Array zurück.
     *
     * @param uuid die eindeutige Nutzerkennung
     * @return Byte-Array der ICS-Datei oder {@code null}, falls kein Nutzer gefunden wurde
     * @throws IOException bei Fehlern beim Laden der CSV oder Datenbank
     */
	private byte[] generateIcsFor(UUID uuid) throws IOException {
		
		DateTimeFormatter utcStamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        int currentYear = LocalDate.now().getYear();
        
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(AppConfig.SQLITE_DB_URL);

        SqlFileSystem sqlFileSystem = SqlFileSystem.New(SqlConnector.Caching(SqlProviderSqlite.New(dataSource)));

        EmbeddedStorageManager storageManager = EmbeddedStorage.start(sqlFileSystem.ensureDirectoryPath(AppConfig.VIRTUAL_DIRECTORY));

        Root root = (Root) storageManager.root();
        if (root == null) {
            root = new Root();
            storageManager.setRoot(root);
            storageManager.storeRoot();
        }

        List<User> storedUsers = root.getUsers();
        ExamDB db = new ExamDB(CSVLoader.getTempFilePath("csv/klausuren.csv"));

        storageManager.shutdown();
    	
        StringBuilder calendar = new StringBuilder();
        calendar.append("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:hs-emden-leer.de\n");
        
        boolean userWasFound = false;
        
        for (User user : storedUsers) {
			if (user.getUuid().equals(uuid)) {
				userWasFound = true;
				
				for (Integer examId : user.getIds()) {
					Exam exam = db.getExamById(examId);
					
					String dateTime = exam.getDate().split(",")[1].trim() + currentYear + " " + exam.getTime();
		            LocalDateTime start = LocalDateTime.parse(dateTime, inputFormat);
		            LocalDateTime end = start.plusMinutes(150);
		            
		            calendar.append("BEGIN:VEVENT\n");
		            calendar.append("UID:exam-").append(exam.getId()).append("\n");
		            calendar.append("DTSTAMP:").append(utcStamp.format(Instant.now())).append("\n");
		            calendar.append("DTSTART:").append(start.format(outputFormat)).append("\n");
		            calendar.append("DTEND:").append(end.format(outputFormat)).append("\n");
		            calendar.append("SUMMARY:").append(exam.getName()).append("\n");
		            calendar.append("DESCRIPTION:Prüfer: ")
		                    .append(exam.getExaminer())
		                    .append(" - Gruppe: ")
		                    .append(exam.getGroups())
		                    .append("\n");
		            calendar.append("LOCATION:").append(exam.getRooms()).append("\n");
		            calendar.append("END:VEVENT\n");
				}
				break;
			}
		}
        
        calendar.append("END:VCALENDAR");
        
        if (userWasFound) {
        	return calendar.toString().getBytes(StandardCharsets.UTF_8);
		}
        
        return null;
	}
	
	/**
     * Prüft, ob ein gegebener String eine gültige UUID darstellt.
     *
     * @param str der zu prüfende String
     * @return {@code true}, wenn der String eine gültige UUID ist, sonst {@code false}
     */
	private boolean isValidUUID(String str) {
        return str != null && str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}