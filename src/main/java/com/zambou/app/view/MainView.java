package com.zambou.app.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.zambou.app.component.CalendarNavBar;
import com.zambou.app.component.ExportButton;
import com.zambou.app.component.FilterForm;
import com.zambou.app.component.UserLinkField;
import com.zambou.app.model.User;
import com.zambou.app.service.CSVLoader;
import com.zambou.app.service.CalendarService;
import com.zambou.app.service.ExamDB;
import com.zambou.app.service.UserSessionService;
import com.zambou.app.storage.UserStorageManager;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Hauptansicht für die Klausurplan-Anwendung.
 * <p>
 * Diese View wird unter dem Pfad {@code /klausurplan} geladen und initialisiert beim ersten Aufruf
 * die Benutzerinstanz, lädt die Prüfungsdaten aus einer CSV-Datei und baut die Benutzeroberfläche
 * mit Filterformular, Exportfunktion und Kalenderdarstellung dynamisch auf.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
@Route("klausurplan")
@PageTitle("Klausurplan HS Emden/Leer")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    /**
     * Konstruktor der View. Die Initialisierung erfolgt im {@link #beforeEnter(BeforeEnterEvent)}-Callback.
     */
    public MainView() {
        // Aufbau in beforeEnter()
    }

    /**
     * Wird vor dem Betreten der View aufgerufen.
     * <p>
     * Initialisiert den Nutzer (neu oder aus Session), lädt die Prüfungsdaten,
     * erstellt die UI-Komponenten und fügt sie zur Ansicht hinzu.
     *
     * @param event das Navigationsevent mit Pfad- und Parameterinformationen
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
    	
    	try (UserStorageManager storageManager = new UserStorageManager()) {
            UUID uuid = loadOrCreateUser(event, storageManager);

            String userLink = getUserLink(event, uuid);
            String icsLink = getICSLink(uuid.toString());

            UI.getCurrent().getPage().getHistory().replaceState(null, "klausurplan");

            try {
            	String csvPath = CSVLoader.getTempFilePath("csv/klausuren.csv");
                CalendarService service = new CalendarService(csvPath);
                // CalendarService2 service2 = new CalendarService2(csvPath);
                ExamDB db = service.getDb();
                FullCalendar calendar = service.getCalendar();
                ExportButton exportButton = new ExportButton(db, icsLink);
                UserLinkField uLinkField = new UserLinkField(userLink);
                FilterForm filterForm = new FilterForm(db, calendar, service);
                CalendarNavBar calendarNavBar = new CalendarNavBar(calendar, csvPath);

                service.setExportButton(exportButton);
                add(filterForm, uLinkField, exportButton, calendarNavBar, calendar);
                
            } catch (IOException e) {
                log.error("Fehler beim Laden des Kalenders", e);
            }
        } catch (Exception e) {
            log.error("Fehler beim Zugriff auf den User-Speicher", e);
        }
    }
    
    /**
     * Lädt einen bestehenden Nutzer anhand der URL-Parameter oder erstellt einen neuen.
     * <p>
     * Der Nutzer wird anschließend in der {@link VaadinSession} gespeichert.
     *
     * @param event           das Navigationsevent mit URL-Parametern
     * @param storageManager  Zugriff auf die persistente Nutzerverwaltung
     * @return die UUID des geladenen oder neu erstellten Nutzers
     */
    private UUID loadOrCreateUser(BeforeEnterEvent event, UserStorageManager storageManager) {
        User sessionUser = UserSessionService.getUser();
        String userId = getQueryParam(event, "userId");
        UUID uuid = isValidUUID(userId) ? UUID.fromString(userId) : (sessionUser != null ? sessionUser.getUuid() : UUID.randomUUID());
        User storedUser = storageManager.getUserById(uuid);

        if (storedUser != null) {
            VaadinSession.getCurrent().setAttribute(User.class, storedUser);
        } else if (sessionUser != null && sessionUser.getUuid().equals(uuid)) {
            storageManager.save(sessionUser);
            VaadinSession.getCurrent().setAttribute(User.class, sessionUser);
        } else {
            User newUser = new User(uuid, new HashSet<>());
            storageManager.save(newUser);
            VaadinSession.getCurrent().setAttribute(User.class, newUser);
        }

        VaadinSession.getCurrent().getSession().setMaxInactiveInterval(-1);
        return uuid;
    }
    
    /**
     * Prüft, ob ein gegebener String eine gültige UUID darstellt.
     *
     * @param str der zu prüfende String
     * @return {@code true}, wenn gültige UUID, sonst {@code false}
     */
    private boolean isValidUUID(String str) {
        return str != null && str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }

    /**
     * Extrahiert einen URL-Parameter aus dem Navigationsevent.
     *
     * @param event das Navigationsevent
     * @param param Name des Parameters
     * @return Wert des Parameters oder {@code null}, falls nicht vorhanden
     */
    private String getQueryParam(BeforeEnterEvent event, String param) {
        return event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault(param, List.of())
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Entfernt alle Query-Parameter aus einer URL.
     *
     * @param url die vollständige URL
     * @return bereinigte URL ohne Parameter
     */
    private String removeNoValidQuery(String url) {
        int idx = url.indexOf('?');
        return (idx != -1) ? url.substring(0, idx) : url;
    }
    
    /**
     * Ermittelt die Basis-URL der aktuellen Anfrage.
     *
     * @return vollständige Basis-URL (z. B. {@code https://example.com/klausurplan})
     */
    private String getBaseUrl() {
        HttpServletRequest request = (HttpServletRequest) VaadinService.getCurrentRequest();
        return request.getRequestURL().toString();
    }
    
    /**
     * Erstellt einen personalisierten Link zur aktuellen Ansicht mit eingebetteter Nutzer-ID.
     *
     * @param event das Navigationsevent
     * @param uuid  die UUID des Nutzers
     * @return vollständiger Link zur Ansicht mit {@code ?userId=...}
     */
    private String getUserLink(BeforeEnterEvent event, UUID uuid) {
        String cleaned = removeNoValidQuery(event.getLocation().getPathWithQueryParameters());
        return getBaseUrl() + cleaned + "?userId=" + uuid;
    }

    /**
     * Erstellt den Download-Link zur ICS-Datei für den angegebenen Nutzer.
     *
     * @param uuid die UUID des Nutzers
     * @return vollständiger Link zur ICS-Datei
     */
    private String getICSLink(String uuid) {
        return getBaseUrl() + "ics-export/" + uuid + ".ics";
    }
}