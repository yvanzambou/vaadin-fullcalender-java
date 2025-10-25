package com.zambou.app.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.zambou.app.model.Event;
import com.zambou.app.service.CalendarService;
import com.zambou.app.service.ExamDB;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Die {@code FilterForm}-Klasse stellt ein Formular zur Verfügung, mit dem
 * Klausurtermine im {@link FullCalendar} nach verschiedenen Kriterien gefiltert werden können.
 *
 * <p>Filterbar sind Studierendengruppen, Dozenten, Räume und Klausurnamen.
 * Die Trefferanzahl wird dynamisch angezeigt und der Kalender aktualisiert.</p>
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class FilterForm extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    /** Anzeige der Anzahl gefilterter Klausuren */
    private final Span resultCount;

    /** Auswahlfeld für Studierendengruppen */
    private final ComboBox<String> groupBox;

    /** Auswahlfeld für Dozenten */
    private final ComboBox<String> examinerBox;

    /** Auswahlfeld für Räume */
    private final ComboBox<String> roomBox;

    /** Auswahlfeld für Klausurnamen */
    private final ComboBox<String> examNameBox;

    /** Service zur Umwandlung von {@link Event}-Objekten in Kalender-Einträge */
    private final CalendarService service;

    
    /**
     * Erstellt eine neue FilterForm-Komponente.
     *
     * @param db       die Datenbank mit Klausurinformationen
     * @param calendar der Kalender, der aktualisiert werden soll
     * @param service  der Service zur Erstellung von Kalender-Einträgen
     */
    public FilterForm(ExamDB db, FullCalendar calendar, CalendarService service) {
    	
        setAlignItems(Alignment.CENTER);
        setPadding(false);
        setSpacing(false);
        
		this.resultCount = new Span();
        this.service = service;
        
        groupBox = createComboBox("Studierendenset:", db.getAllGroups());
        examinerBox = createComboBox("Dozent:", db.getAllExaminers());
        roomBox = createComboBox("Raum:", db.getAllRooms());
        examNameBox = createComboBox("Klausur:", db.getAllNames());
        
        int maxLength = db.getAllNames().stream().mapToInt(String::length).max().orElse(10);
        examNameBox.setWidth((maxLength - 20) + "ch");

        FlexLayout layout = new FlexLayout(groupBox, examinerBox, roomBox, examNameBox);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        
        resultCount.setText("Treffer: " + db.getAllExams().size());
        resultCount.getStyle().set("font-weight", "bold");
        
        add(layout);
        add(resultCount);
        setupListeners(db, calendar);
    }
    
    /**
     * Aktualisiert die Anzeige der Trefferanzahl.
     *
     * @param count Anzahl der gefilterten Klausuren
     */
    public void updateResultCount(int count) {
        resultCount.setText("Treffer: " + count);
    }

    /**
     * Erstellt ein {@link ComboBox}-Element mit gegebenem Label und Einträgen.
     *
     * @param label Beschriftung des Auswahlfeldes
     * @param items Menge der auswählbaren Einträge
     * @return konfigurierte {@link ComboBox}
     */
    private ComboBox<String> createComboBox(String label, Set<String> items) {
        ComboBox<String> box = new ComboBox<>(label);
        box.setItems(items);
        box.setPlaceholder("Bitte auswählen");
        box.setClearButtonVisible(true);
        box.setWidth("200px");
        box.getStyle().set("margin", "0 10px");
        return box;
    }

    /**
     * Registriert Listener für alle Filterfelder, um bei Änderungen den Kalender zu aktualisieren.
     *
     * @param db       die Datenbank mit Klausurinformationen
     * @param calendar der Kalender, der aktualisiert werden soll
     */
    private void setupListeners(ExamDB db, FullCalendar calendar) {
        Stream.of(groupBox, examinerBox, roomBox, examNameBox)
            .forEach(box -> box.addValueChangeListener(e -> updateCalendar(db, calendar)));
    }

    /**
     * Aktualisiert den Kalender basierend auf den aktuellen Filterwerten.
     *
     * @param db       die Datenbank mit Klausurinformationen
     * @param calendar der Kalender, der aktualisiert werden soll
     */
    private void updateCalendar(ExamDB db, FullCalendar calendar) {
        String group = groupBox.getValue();
        String examiner = examinerBox.getValue();
        String room = roomBox.getValue();
        String examName = examNameBox.getValue();

        InMemoryEntryProvider<Entry> provider = calendar.getEntryProvider().asInMemory();
        provider.removeAllEntries();

        List<Event> filteredEvents = db.getExamsAsEvents(db.getFilteredExams(group, examiner, room, examName));
        updateResultCount(filteredEvents.size());
        
        if (!filteredEvents.isEmpty()) {
            LocalDate jumpDate = LocalDateTime.parse(filteredEvents.get(0).getStart()).toLocalDate();
            calendar.gotoDate(jumpDate);
        }
        
        filteredEvents.forEach(ev -> provider.addEntries(service.createEntry(ev)));
        provider.refreshAll();
    }
}