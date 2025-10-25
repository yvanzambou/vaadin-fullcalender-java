package com.zambou.app.component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.zambou.app.model.Exam;
import com.zambou.app.service.ExamDB;
import com.zambou.app.service.ICSExport;
import com.zambou.app.service.UserSessionService;

/**
 * Die {@code ExportButton}-Klasse stellt eine Benutzeroberflächenkomponente dar,
 * die es ermöglicht, vorgemerkte Klausuren aus einer Datenbank als ICS-Datei zu exportieren.
 * 
 * <p>Sie bietet eine Schaltfläche zur Anzeige der Anzahl vorgemerkter Klausuren und öffnet
 * bei Klick einen Dialog zur Auswahl und zum Herunterladen der Termine. Zusätzlich kann
 * ein ICS-Link kopiert werden.</p>
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class ExportButton extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    /** Export-Schaltfläche zur Anzeige und Auslösung des Dialogs */
    private final Button export;

    /** ICS-Link für den Kalenderexport */
    private final String icsLink;

    /**
     * Erstellt eine neue {@code ExportButton}-Instanz.
     *
     * @param db      die Datenbank mit Klausurinformationen
     * @param icsLink der ICS-Link für den Kalenderexport
     */
    public ExportButton(ExamDB db, String icsLink) {
    	this.icsLink = icsLink;
    	this.export = new Button();
        export.getStyle().set("background-color", "#4caf50")
        				 .set("color", "white")
        				 .set("cursor", "pointer");
        export.addClickListener(e -> openExportDialog(db));
        
        FlexLayout exportLayout = new FlexLayout(export);
        exportLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        exportLayout.setWidthFull();
        setWidthFull();
        
        add(exportLayout);
        update();
    }

    /**
     * Aktualisiert den Text der Export-Schaltfläche basierend auf der Anzahl vorgemerkter Klausuren.
     */
    public void update() {
        int count = UserSessionService.getUser().getIdCount();
        export.setText("Alle vorgemerkte Klausuren anzeigen (" + count + ")");
        export.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "600");
    }

    /**
     * Öffnet einen Dialog zur Auswahl und zum Export vorgemerkter Klausuren.
     *
     * @param db die Datenbank mit Klausurinformationen
     */
    private void openExportDialog(ExamDB db) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Zum Export bereit");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);

        List<Exam> selectedExams = getSelectedExams(db);
        
        Map<Checkbox, Exam> checkboxMap = new LinkedHashMap<>();
        if (selectedExams.isEmpty()) {
            dialogContent.add(new Span("Es wurden keine Klausuren vorgemerkt."));
        } else {
            for (Exam exam : selectedExams) {
                Checkbox checkbox = new Checkbox(exam.getName(), true);
                Integer examId = exam.getId();

                checkbox.addValueChangeListener(evt -> {
                    if (evt.getValue()) {
                        UserSessionService.getUser().addId(examId);
                    } else {
                        UserSessionService.getUser().removeId(examId);
                    }
                    UserSessionService.storeUser();
                    
                    update();
                });

                checkboxMap.put(checkbox, exam);
                dialogContent.add(checkbox);
            }
        }

        Button downloadBtn = new Button("Herunterladen");
        designedButton(downloadBtn);
        downloadBtn.setEnabled(!selectedExams.isEmpty());

        Anchor disableableButton = new Anchor();		// Damit der Button später deaktiviert werden kann, falls es keine vorgemerkte Klausuren gibt
        disableableButton.add(downloadBtn);

        if (selectedExams.isEmpty()) {			// Button sieht deaktiviert aus
            disableableButton.setEnabled(false);
            disableableButton.getElement().getStyle().set("opacity", "0.5");
        }

        String icsFilename = UserSessionService.getUser().getUuid().toString() + ".ics";
        
        downloadBtn.addClickListener(e -> {
            List<Exam> toExport = checkboxMap.entrySet().stream()
                .filter(entry -> entry.getKey().getValue())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

            if (toExport.isEmpty()) {
            	Notification notif = new Notification("Bitte mindestens eine Klausur auswählen", 3000, Notification.Position.BOTTOM_START);
                notif.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notif.open();
                return;
            }

            StreamResource resource = ICSExport.exportToIcs(toExport);
            resource.setContentType("text/calendar; charset=utf-8");

            StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
            String resourceUrl = registration.getResourceUri().toString();
            
            UI ui = export.getUI().orElseThrow();

            ui.getPage().executeJs(
                """
                fetch($0)
                  .then(resp => resp.blob())
                  .then(blob => {
                    const a = document.createElement('a');
                    a.style.display = 'none';
                    a.href = URL.createObjectURL(blob);
                    a.download = $1;
                    document.body.appendChild(a);
                    a.click();
                    URL.revokeObjectURL(a.href);
                    a.remove();
                  });
                """,
                resourceUrl, icsFilename
            ).then(response -> {
                uploadSuccessNotification(icsFilename).open();
                dialog.close();
            });
        });
        
        Button copyICSLink = new Button("URL kopieren");
        copyICSLink.setTooltipText(icsLink);
        designedButton(copyICSLink);
        copyICSLink.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                """
                const tempInput = document.createElement('input');
                tempInput.style.position = 'absolute';
                tempInput.style.opacity = '0';
                tempInput.value = $0;
                document.body.appendChild(tempInput);
                tempInput.select();
                document.execCommand('copy');
                document.body.removeChild(tempInput);
                """,
                icsLink
            ).then(response -> {
            	Notification notif = new Notification("Link kopiert", 3000, Notification.Position.TOP_END);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notif.open();
                dialog.close();
            });
        });

        Button closeBtn = new Button("Schließen", event -> dialog.close());
        closeBtn.getStyle().set("margin", "4px 10px")
        				   .set("color", "red")
        				   .set("cursor", "pointer");

        FlexLayout footer = new FlexLayout(disableableButton, copyICSLink, closeBtn);
        footer.setFlexWrap(FlexWrap.WRAP);
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        dialog.getFooter().add(footer);

        dialog.add(dialogContent);
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setResizable(false);
        dialog.open();
    }

    /**
     * Stellt einheitliches Styling für Buttons bereit.
     *
     * @param btn der zu stylende Button
     */
    private void designedButton(Button btn) {
    	btn.getStyle().set("margin", "4px 10px")
    				  .set("background-color", "#4CAF50")
    				  .set("color", "white")
    				  .set("cursor", "pointer");
    }

    /**
     * Gibt die Liste der aktuell vorgemerkten Klausuren zurück.
     *
     * @param db die Datenbank mit Klausurinformationen
     * @return Liste der ausgewählten {@link Exam}-Objekte
     */
    private List<Exam> getSelectedExams(ExamDB db) {
        Set<Integer> ids = UserSessionService.getUser().getIds();
        return ids.stream()
                  .map(db::getExamById)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    /**
     * Erstellt eine Erfolgsmeldung nach erfolgreichem Export.
     *
     * @param name Dateiname der exportierten ICS-Datei
     * @return {@link Notification}-Komponente mit Erfolgshinweis
     */
    public static Notification uploadSuccessNotification(String name) {
        Notification notif = new Notification();
        notif.setDuration(5000);

        Icon icon = VaadinIcon.CHECK_CIRCLE.create();
        icon.setColor("var(--lumo-success-color)");

        Div success = new Div(new Text("Erfolgreich exportiert"));
        success.getStyle().set("font-weight", "600")
        						   .setColor("var(--lumo-success-text-color)");

        Span fileName = new Span(name);
        fileName.getStyle().set("font-size", "var(--lumo-font-size-s)")
        				   .set("font-weight", "600");

        Div info = new Div(
        		success,
        		new Div(new Text("Datei "), fileName, new Text(" heruntergeladen"))
        );
        info.getStyle().set("font-size", "var(--lumo-font-size-s)")
        			   .setColor("var(--lumo-secondary-text-color)");

        HorizontalLayout layout = new HorizontalLayout(icon, info);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        notif.add(layout);
        return notif;
    }
}