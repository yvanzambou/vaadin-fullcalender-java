package com.zambou.app.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.zambou.app.service.ExamDB;

import org.vaadin.stefan.fullcalendar.CalendarViewImpl;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Die {@code CalendarNavBar}-Klasse stellt eine Navigationsleiste für einen {@link FullCalendar}
 * dar und ermöglicht die Auswahl verschiedener Kalenderansichten (Monat, Woche, Tag),
 * sowie Navigation zwischen Zeitintervallen.
 *
 * <p>Sie zeigt außerdem den aktuellen Zeitraum an und deaktiviert Navigationsbuttons,
 * wenn das Datum außerhalb eines gültigen Bereichs liegt.</p>
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class CalendarNavBar extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	
	/** Datenbankinstanz zur Ermittlung gültiger Datumsbereiche */
    private final ExamDB db;

    /** Label zur Anzeige des aktuellen Zeitraums */
    private final Span titleLabel = new Span();

    /** Aktuell ausgewählte Kalenderansicht */
    private CalendarViewImpl currentView = CalendarViewImpl.DAY_GRID_MONTH;

    /** Button für Monatsansicht */
    private Button monthView;

    /** Button für Wochenansicht */
    private Button weekView;

    /** Button für Tagesansicht */
    private Button dayView;

    /**
     * Erstellt eine neue Navigationsleiste für den übergebenen Kalender.
     *
     * @param calendar die {@link FullCalendar}-Instanz, die gesteuert werden soll
     * @param csvPath Pfad zur CSV-Datei mit den Prüfungsterminen
     */
    public CalendarNavBar(FullCalendar calendar, String csvPath) {
    	db = new ExamDB(csvPath);
    	
    	// Automatische Ansichtsauswahl basierend auf Bildschirmbreite
        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            int screenWidth = details.getScreenWidth();

            if (screenWidth < 768) {
                currentView = CalendarViewImpl.TIME_GRID_DAY;
            }
            
            calendar.changeView(currentView);
            updateViewButtonStyles(monthView, weekView, dayView);
        });
    	
        // Initialisierung der Ansicht-Buttons
        monthView = new Button(buttonText("MONAT"), e -> {
            currentView = CalendarViewImpl.DAY_GRID_MONTH;
            calendar.changeView(currentView);
            updateViewButtonStyles(monthView, weekView, dayView);
        });
        monthView.getStyle().set("cursor", "pointer");

        weekView = new Button(buttonText("WOCHE"), e -> {
            currentView = CalendarViewImpl.TIME_GRID_WEEK;
            calendar.changeView(currentView);
            updateViewButtonStyles(monthView, weekView, dayView);
        });
        weekView.getStyle().set("margin", "4px 5px");
        weekView.getStyle().set("cursor", "pointer");

        dayView = new Button(buttonText("TAG"), e -> {
            currentView = CalendarViewImpl.TIME_GRID_DAY;
            calendar.changeView(currentView);
            updateViewButtonStyles(monthView, weekView, dayView);
        });
        dayView.getStyle().set("cursor", "pointer");

        // Navigationsbuttons
        Icon leftArrow = VaadinIcon.ARROW_LEFT.create();
        Icon rightArrow = VaadinIcon.ARROW_RIGHT.create();
        
        Button next = new Button(rightArrow, e -> calendar.next());
        Button prev = new Button(leftArrow, e -> calendar.previous());
        Button today = new Button(buttonText("HEUTE"), e -> calendar.today());
        today.getStyle().set("margin", "4px 5px");
        
        next.getStyle().set("cursor", "pointer");
        prev.getStyle().set("cursor", "pointer");
        today.getStyle().set("cursor", "pointer");

        titleLabel.getStyle().set("font-weight", "bold").set("font-size", "1.4em");

        FlexLayout left = new FlexLayout(monthView, weekView, dayView);
        left.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        left.setJustifyContentMode(JustifyContentMode.CENTER);
        
        FlexLayout right = new FlexLayout(prev, today, next);
        right.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        right.setJustifyContentMode(JustifyContentMode.CENTER);
        
        setWidthFull();
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(left, titleLabel, right);
        
        // Gültiger Datumsbereich aus CSV-Datei
        List<LocalDate> validRange = db.getSortedDatesAsLocalDate();
        
        // Listener zur Aktualisierung der Titelanzeige und Button-Zustände
        calendar.addDatesRenderedListener(event -> {
            titleLabel.setText(getFormatter(currentView).format(event.getIntervalStart()));
            
            LocalDate startDate = validRange.getFirst();
            LocalDate endDate = validRange.getLast();
            LocalDate todayDate = LocalDate.now();
            LocalDate intervalStart = event.getIntervalStart();
            LocalDate intervalEnd = event.getIntervalEnd();
            
            boolean isTodayInRange = !todayDate.isBefore(startDate) && !todayDate.isAfter(endDate);
            today.setEnabled(isTodayInRange);
            
            prev.setEnabled(intervalStart.isAfter(startDate));
            next.setEnabled(intervalEnd.isBefore(endDate));
        });
    }
    
    /**
     * Erstellt ein {@link Span}-Element mit gestyltem Buttontext.
     *
     * @param text der anzuzeigende Text
     * @return gestylter {@link Span}
     */
    private Span buttonText(String text) {
    	Span span = new Span(text);
    	span.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "600");
		return span;
	}

    /**
     * Gibt ein passendes {@link DateTimeFormatter}-Objekt für die aktuelle Ansicht zurück.
     *
     * @param view die aktuelle Kalenderansicht
     * @return entsprechender {@link DateTimeFormatter}
     */
	private DateTimeFormatter getFormatter(CalendarViewImpl view) {
        return switch (view) {
            case DAY_GRID_MONTH -> DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);
            case TIME_GRID_WEEK -> DateTimeFormatter.ofPattern("'KW' ww - MMMM yyyy", Locale.GERMAN);
            case TIME_GRID_DAY -> DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
            default -> DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMAN);
        };
    }
	
	/**
     * Aktualisiert die Button-Stile, um die aktive Ansicht visuell hervorzuheben.
     *
     * @param monthView Button für Monatsansicht
     * @param weekView  Button für Wochenansicht
     * @param dayView   Button für Tagesansicht
     */
	private void updateViewButtonStyles(Button monthView, Button weekView, Button dayView) {
	    Stream.of(monthView, weekView, dayView).forEach(btn -> {
	        btn.getStyle().remove("background-color");
	        btn.getStyle().remove("color");
	    });
	    
	    switch (currentView) {
	        case DAY_GRID_MONTH -> {
	            monthView.getStyle().set("background-color", "#1976d2");
	            monthView.getStyle().set("color", "white");
	        }
	        case TIME_GRID_WEEK -> {
	            weekView.getStyle().set("background-color", "#1976d2");
	            weekView.getStyle().set("color", "white");
	        }
	        case TIME_GRID_DAY -> {
	            dayView.getStyle().set("background-color", "#1976d2");
	            dayView.getStyle().set("color", "white");
	        }
	    }
	}
}