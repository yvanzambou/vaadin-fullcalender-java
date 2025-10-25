package com.zambou.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.zambou.app.component.ExportButton;
import com.zambou.app.model.Event;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Die {@code CalendarService}-Klasse verwaltet die Darstellung und Interaktion
 * eines {@link FullCalendar}-Kalenders für Klausurtermine.
 *
 * <p>Sie lädt Daten aus einer CSV-basierten {@link ExamDB}, konfiguriert die Kalenderansicht,
 * erstellt visuelle Einträge und ermöglicht die Auswahl von Klausuren zur Exportmarkierung.</p>
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class CalendarService {

	/** Datenbank mit Klausurinformationen */
    private final ExamDB db;

    /** Kalenderkomponente zur Anzeige der Klausuren */
    private final FullCalendar calendar;

    /** Referenz zur Export-Schaltfläche für Aktualisierung nach Auswahl */
    private ExportButton exportButton;

    /**
     * Erstellt einen neuen {@code CalendarService} mit Daten aus der angegebenen CSV-Datei.
     *
     * @param csvPath Pfad zur CSV-Datei mit Klausurterminen
     */
    public CalendarService(String csvPath) {
    	db = new ExamDB(csvPath);
    	
        calendar = FullCalendarBuilder.create().build();
        calendar.setEntryProvider(new InMemoryEntryProvider<>(new ArrayList<>()));
        configureCalendar();
        addEntries();
    }

    /**
     * Konfiguriert die Darstellung und das Verhalten des Kalenders.
     * Dazu gehören Sprache, Zeitfenster, Drag-and-drop-Verhalten und visuelle Formatierung.
     */
    private void configureCalendar() {
        calendar.setLocale(Locale.GERMAN);
        calendar.setOption("height", "auto");
        calendar.setOption("hiddenDays", "[0]"); // Sonntag ausblenden
        calendar.setOption("allDaySlot", false); // Kein ganztätiges Event
        calendar.setOption("slotDuration", "02:30:00");
        calendar.setOption("slotMinTime", "08:00:00");
        calendar.setOption("slotMaxTime", "18:00:00");
        calendar.setOption("eventAllow", "function(dropInfo, draggedEvent) { return false; }"); // Drag-and-drop von Events deaktivieren
        
        // Uhrzeitformat konfigurieren
        JsonObject formatObj = Json.createObject();
        formatObj.put("hour", "2-digit");
        formatObj.put("minute", "2-digit");
        JsonArray slotLabelFormat = Json.createArray();
        slotLabelFormat.set(0, formatObj);
        calendar.setOption("slotLabelFormat", slotLabelFormat);
        
        // Gültiger Datumsbereich
        List<LocalDate> range = db.getSortedDatesAsLocalDate();
        calendar.setValidRange(range.getFirst(), range.getLast().plusDays(1));
        
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);
        
        // Darstellung der Einträge im Kalender
        calendar.setEntryContentCallback("""
        	    function(arg) {
				    const start = arg.event.extendedProps.customProperties.start;
				    const end = arg.event.extendedProps.customProperties.end;
				    const room = arg.event.extendedProps.customProperties.room;
				    const examiner = arg.event.extendedProps.customProperties.examiner;
				    const title = arg.event.title;
				
				    const wrapper = document.createElement("div");
				    wrapper.classList.add("hover-effekt");
				    wrapper.setAttribute("style", "white-space: normal !important; padding: 5px; width: 100%; height: 100%; overflow: hidden; color: #fff; background-color: #3788d8; border-radius: 5px; transition: background-color 0.2s ease;");
				    
				    wrapper.innerHTML = `<div>${start} - ${end}<br><strong>${title}</strong></div>
				        				 <div>${examiner}@${room}</div>`;
				    return { domNodes: [wrapper] };
				}
        	""");
        
        // Interaktive Navigation
        calendar.setNumberClickable(true);
        calendar.addDayNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_DAY);
            calendar.gotoDate(event.getDate());
        });
        
        calendar.addWeekNumberClickedListener(event -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(event.getDate());
        });
        
        // Klick auf Eintrag öffnet Dialog zum Export
        calendar.addEntryClickedListener(event -> {
        	Entry entry = event.getEntry();
            Integer id = Integer.parseInt(entry.getGroupId());

            Map<String, Object> props = entry.getCustomProperties();

            String modalTitle = entry.getTitle();
            String modalDate = entry.getStart().toString();
            String dayOfWeek = entry.getStart().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMAN);
            String modalRoom = (String) props.get("room");
            String modalExaminer = (String) props.get("examiner");

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Information");

            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.add(createModalItem("Klausur: ", modalTitle));
            dialogContent.add(createModalItem("Datum: ", dayOfWeek + ", " + getDate(modalDate)));
            dialogContent.add(createModalItem("Uhrzeit: ", getTime(modalDate) + " Uhr"));
            dialogContent.add(createModalItem("Raum: ", modalRoom));
            dialogContent.add(createModalItem("Prüfer: ", modalExaminer));

            Set<Integer> ids = UserSessionService.getUser().getIds();
            boolean selected = ids.contains(id);

            Button exportBtn = new Button();
            if (selected) {
                exportBtn.setText("Aus Export entfernen");
                exportBtn.getStyle().set("background-color", "#e53935");
            } else {
                exportBtn.setText("Zum Export bereitstellen");
                exportBtn.getStyle().set("background-color", "#4caf50");
            }
            exportBtn.getStyle().set("color", "white")
            					.set("cursor", "pointer");

            exportBtn.addClickListener(click -> {
                if (selected) {
                    UserSessionService.getUser().removeId(id);
                    System.err.println(UserSessionService.getUser().getUuid() + ": " +UserSessionService.getUser().getIds() + " - removed: " + id);
                    markedExamNotification(modalTitle, false).open();
                } else {
                    UserSessionService.getUser().addId(id);
                    System.err.println(UserSessionService.getUser().getUuid() + ": " +UserSessionService.getUser().getIds() + " - added: " + id);
                    markedExamNotification(modalTitle, true).open();
                }
                UserSessionService.storeUser();
                
                UI ui = UI.getCurrent();
                if (ui != null && exportButton != null) {
                    ui.access(() -> exportButton.update());
                }
                dialog.close();
            });

            Button closeButton = new Button("Schließen", e -> dialog.close());
            closeButton.getStyle().set("color", "red")
            					  .set("cursor", "pointer");

            dialog.getFooter().add(exportBtn, closeButton);
            dialog.add(dialogContent);
            dialog.open();
        });
    }
    
    /**
     * Fügt alle Klausurtermine aus der Datenbank (CSV-Datei) als Einträge in den Kalender ein.
     */
    private void addEntries() {
    	db.getExamsAsEvents(db.getAllExams()).forEach(ev ->
    		calendar.getEntryProvider().asInMemory().addEntries(createEntry(ev))
    	);
    }
    
    /**
     * Erstellt einen {@link Entry} für den Kalender basierend auf einem {@link Event}.
     *
     * @param ev das Event-Objekt
     * @return ein vollständiger Kalender-Eintrag
     */
    public Entry createEntry(Event ev) {
        Entry entry = new Entry();
        entry.setGroupId(String.valueOf(ev.getId()));
        entry.setTitle(ev.getTitle());
        entry.setStart(LocalDateTime.parse(ev.getStart()));
        entry.setEnd(LocalDateTime.parse(ev.getEnd()));
        
        Map<String, Object> customProps = new HashMap<>();
        customProps.put("start", getTime(ev.getStart()));
        customProps.put("end", getTime(ev.getEnd()));
        customProps.put("room", ev.getRooms());
        customProps.put("examiner", ev.getExaminer());
        
        entry.setCustomProperties(customProps);
        return entry;
    }
    
    /**
     * Extrahiert das Datum aus einem ISO-Zeitstempel.
     *
     * @param isoDateTime ISO-Zeitstempel (z. B. "2025-08-21T10:00")
     * @return formatiertes Datum (z. B. "21.08.2025")
     */
    private String getDate(String isoDateTime) {
        LocalDateTime ldt = LocalDateTime.parse(isoDateTime);
        return ldt.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    /**
     * Extrahiert die Uhrzeit aus einem ISO-Zeitstempel.
     *
     * @param isoDateTime ISO-Zeitstempel
     * @return Uhrzeit im Format "HH:mm"
     */
    private String getTime(String isoDateTime) {
        LocalDateTime ldt = LocalDateTime.parse(isoDateTime);
        return ldt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Erstellt eine Benachrichtigung zur Markierung oder Entfernung einer Klausur.
     *
     * @param modalTitle Titel der Klausur
     * @param marked     {@code true} wenn hinzugefügt, {@code false} wenn entfernt
     * @return eine {@link Notification}-Komponente
     */
    private Notification markedExamNotification(String modalTitle, boolean marked) {
    	Notification notif = new Notification();
    	notif.setPosition(Notification.Position.BOTTOM_START);
    	notif.setDuration(3000);
    	
    	Span title = new Span(modalTitle);
    	Span msg;
    	
    	if (marked) {
    		title.getStyle().set("color", "green");
    		msg = new Span(" zum Export vorgemerkt");
		} else {
			title.getStyle().set("color", "red");
    		msg = new Span(" aus Export entfernt");
		}
    	title.getStyle().set("font-weight", "bold")
    					.set("margin-right", "10px");
    	
    	FlexLayout layout = new FlexLayout(title, msg);
        layout.setFlexWrap(FlexWrap.WRAP);
        
    	notif.add(layout);
		return notif;
	}

    /**
     * Erstellt ein visuelles Element zur Anzeige eines einzelnen Informationseintrags im Dialog.
     *
     * @param text Beschriftung des Feldes (z. B. "Raum:")
     * @param item Inhalt des Feldes (z. B. "B301")
     * @return ein {@link Component}, das aus zwei gestylten {@link Span}-Elementen besteht
     */
	private Component createModalItem(String text, String item) {
    	Span s1 = new Span(text);
    	Span s2 = new Span(item);
    	
    	s1.getStyle().set("font-size", "var(--lumo-font-size-m)")
    				 .set("font-weight", "600");
		s2.getStyle().set("font-size", "var(--lumo-font-size-m)");
		
		FlexLayout box = new FlexLayout(s1, s2);
        box.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
		return box;
	}
	
	/**
	 * Gibt die verwendete {@link FullCalendar}-Instanz zurück.
	 *
	 * @return der Kalender mit allen konfigurierten Einträgen und Optionen
	 */
	public FullCalendar getCalendar() {
        return calendar;
    }
	
	/**
	 * Gibt die verwendete {@link ExamDB}-Instanz zurück.
	 *
	 * @return die Datenbank mit allen geladenen Klausurterminen
	 */
    public ExamDB getDb() {
        return db;
    }
    
    /**
     * Setzt die Referenz zur {@link ExportButton}-Instanz, damit diese bei Änderungen aktualisiert werden kann.
     *
     * @param exportButton die Export-Schaltfläche, die aktualisiert werden soll
     */
    public void setExportButton(ExportButton exportButton) {
        this.exportButton = exportButton;
    }
}