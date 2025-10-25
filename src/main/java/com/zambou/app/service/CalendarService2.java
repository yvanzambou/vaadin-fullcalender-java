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

public class CalendarService2 {

    private final ExamDB db;
    private final FullCalendar calendar;
    private ExportButton exportButton;

    public CalendarService2(String csvPath) {
    	db = new ExamDB(csvPath);
    	
        calendar = FullCalendarBuilder.create().build();
        calendar.setEntryProvider(new InMemoryEntryProvider<>(new ArrayList<>()));
        configureCalendar();
        addEntries();
    }

    private void configureCalendar() {
    	Set<Integer> ids = UserSessionService.getUser().getIds();
    	
    	// ========== Markierung mit Neuladen der Seite ==========
        /* String idsAsString = ids.stream().map(String::valueOf).collect(Collectors.joining("-"));
           calendar.setOption("examIDs", idsAsString); */
    	
        calendar.setLocale(Locale.GERMAN);
        calendar.setOption("height", "auto");
        calendar.setOption("hiddenDays", "[0]"); // Sonntag ausblenden
        calendar.setOption("allDaySlot", false); // Kein ganztätiges Event
        calendar.setOption("slotDuration", "02:30:00");
        calendar.setOption("slotMinTime", "08:00:00");
        calendar.setOption("slotMaxTime", "18:00:00");
        calendar.setOption("eventAllow", "function(dropInfo, draggedEvent) { return false; }"); // Drag-and-drop von Events deaktivieren
        
        // (optional) Uhrzeiten auf HH:mm (z.B. 12:00 statt 12 Uhr)
        JsonArray slotLabelFormat = Json.createArray();
        JsonObject formatObj = Json.createObject();
        formatObj.put("hour", "2-digit");
        formatObj.put("minute", "2-digit");
        slotLabelFormat.set(0, formatObj);
        calendar.setOption("slotLabelFormat", slotLabelFormat);
        
        List<LocalDate> range = db.getSortedDatesAsLocalDate();
        calendar.setValidRange(range.getFirst(), range.getLast().plusDays(1));
        
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);
        
        // ========== Markierung mit Neuladen der Seite ==========
        /* calendar.setEntryContentCallback("""
        	    function(arg) {
        		    const id = arg.event.groupId;
				    const start = arg.event.extendedProps.customProperties.start;
				    const end = arg.event.extendedProps.customProperties.end;
				    const room = arg.event.extendedProps.customProperties.room;
				    const examiner = arg.event.extendedProps.customProperties.examiner;
				    const title = arg.event.title;
				    
				    const userIdsAsString = arg.view.calendar.getOption("examIDs") || "";
        		    const examIds = userIdsAsString.split("-").map(Number);
        		    
        		    let bgColor = "#3788d8";
					if (examIds.includes(Number(id))) {
					    bgColor = "#4caf50";
					}
				
				    const wrapper = document.createElement("div");
				    wrapper.classList.add("hover-effekt");
				    wrapper.setAttribute("style", `white-space: normal !important; padding: 5px; width: 100%; height: 100%; overflow: hidden; color: #fff; background-color: ${bgColor}; border-radius: 5px; transition: background-color 0.2s ease;`);
				    
				    wrapper.innerHTML = `
				        <div>${start} - ${end}<br><strong>${title}</strong></div>
				        <div>${examiner}@${room}</div>
				    `;
				
				    return { domNodes: [wrapper] };
				}
        	""");
        	*/
        
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
                	ui.access(() -> {
                		
                		// ========== Markierung mit standart-Event (Nur Uhrzeit und Titel) ==========
                        InMemoryEntryProvider<Entry> provider = calendar.getEntryProvider().asInMemory();
                        provider.getEntries().stream()
                        .filter(e -> e.getGroupId().equals(entry.getGroupId()))
                        .findFirst()
                        .ifPresent(provider::removeEntry);
                        provider.addEntries(createEntry(db.getEventById(id)));
                        calendar.getEntryProvider().refreshAll();
                        
                        // ========== Markierung mit Neuladen der Seite ==========
                        /* String updatedUserIDs = SessionUtils.getUser().getIds().stream().map(String::valueOf).collect(Collectors.joining("-"));
                        calendar.setOption("examIDs", updatedUserIDs); */
                        
                        exportButton.update();
                    });
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
    
    private void addEntries() {
    	db.getExamsAsEvents(db.getAllExams()).forEach(ev ->
    		calendar.getEntryProvider().asInMemory().addEntries(createEntry(ev))
    	);
    }
    
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
        
        // ========== Markierung mit standart-Event (Nur Uhrzeit und Titel)
        boolean isSelected = UserSessionService.getUser().getIds().contains(ev.getId());
        entry.setBackgroundColor(isSelected ? "#4caf50" : "#3788d8");
        
        return entry;
    }
    
    private String getDate(String isoDateTime) {
        LocalDateTime ldt = LocalDateTime.parse(isoDateTime);
        return ldt.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String getTime(String isoDateTime) {
        LocalDateTime ldt = LocalDateTime.parse(isoDateTime);
        return ldt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

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
	
	public FullCalendar getCalendar() {
        return calendar;
    }

    public ExamDB getDb() {
        return db;
    }
    
    public void setExportButton(ExportButton exportButton) {
        this.exportButton = exportButton;
    }
}