package com.zambou.app.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Die {@code UserLinkField}-Klasse stellt eine Benutzeroberflächenkomponente dar,
 * die dem Nutzer einen personalisierten Link anzeigt, mit dem er später auf seine
 * vorgemerkten Klausuren zugreifen kann.
 *
 * <p>Der Link wird in einem nicht editierbaren Textfeld angezeigt und kann per Klick
 * auf eine Schaltfläche in die Zwischenablage kopiert werden.</p>
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
public class UserLinkField extends HorizontalLayout {
	
	private static final long serialVersionUID = 1L;

	/**
     * Erstellt ein neues {@code UserLinkField}-Element mit dem übergebenen Link.
     *
     * @param uLink der personalisierte Link für den Nutzer
     */
	public UserLinkField(String uLink) {
		FlexLayout urlBox = createUrlBox(uLink);
		add(urlBox);
	}
	
	/**
     * Erstellt die visuelle Darstellung des Links inklusive Beschreibung und Kopierfunktion.
     *
     * @param url der darzustellende Link
     * @return ein {@link FlexLayout} mit Label, Textfeld und Kopierbutton
     */
	private FlexLayout createUrlBox(String url) {
		// Hinweistext für den Nutzer
        Span label = new Span("** Bitte folgenden Link für den nächsten Besuch aufbewahren, falls Sie Klausuren vorgemerkt haben **");
        label.getStyle().set("font-size", "12px").set("font-style", "italic");

        // Textfeld mit dem Link (nicht editierbar)
        TextField urlField = new TextField();
        urlField.setId("copyField");
        urlField.setWidth(url.length()+ 5 + "ch");
        urlField.setValue(url);
        urlField.setReadOnly(true);
        urlField.getStyle().set("font-family", "consolas");

        // Button zum Kopieren des Links in die Zwischenablage
        Button copyButton = new Button("Link kopieren");
        copyButton.addClickListener(e -> {
        	UI.getCurrent().getPage().executeJs(
        	        """
        	        const input = document.querySelector('#copyField input');
        	        if (input) {
        	            input.select();
        	            document.execCommand('copy');
        	        }
        	        """
        	    );
        	Notification notif = new Notification("Link kopiert", 3000, Notification.Position.TOP_END);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notif.open();
        });
        
        // Layout für Textfeld und Button
        FlexLayout linkBox = new FlexLayout(urlField, copyButton);
        linkBox.setFlexWrap(FlexWrap.NOWRAP);
        linkBox.setJustifyContentMode(JustifyContentMode.CENTER);
        linkBox.setAlignItems(Alignment.CENTER);
        linkBox.setWidthFull();
        linkBox.getStyle().set("gap", "10px");

        // Gesamtlayout mit Hinweistext und Linkbereich
        FlexLayout box = new FlexLayout(label, linkBox);
        box.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        box.setAlignItems(FlexLayout.Alignment.CENTER);
        box.setJustifyContentMode(JustifyContentMode.CENTER);
        box.setWidthFull();
        setWidthFull();

        return box;
    }
}