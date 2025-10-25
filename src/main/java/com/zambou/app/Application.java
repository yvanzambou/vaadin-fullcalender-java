package com.zambou.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.zambou.app.service.ICSDownloadServlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Einstiegspunkt der Klausurplan-Webanwendung.
 * <p>
 * Diese Klasse bootet die Spring Boot-Anwendung, konfiguriert das Vaadin-Theme
 * und registriert ein benutzerdefiniertes Servlet zur Bereitstellung von ICS-Dateien.
 * 
 * @author Yvan Zambou
 * @version 1.0
 */
@SpringBootApplication
@Theme("default")
public class Application implements AppShellConfigurator {

    private static final long serialVersionUID = 1L;

    /**
     * Hauptmethode zum Starten der Spring Boot-Anwendung.
     * <p>
     * Optional kann die automatische Neustartfunktion von Spring DevTools deaktiviert werden.
     *
     * @param args Kommandozeilenargumente
     */
	public static void main(String[] args) {
		// System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(Application.class, args);
    }
	
	/**
     * Registriert das {@link ICSDownloadServlet}, das ICS-Kalenderdateien für Nutzer bereitstellt.
     * <p>
     * Das Servlet wird unter dem Pfad {@code /ics-export/*} verfügbar gemacht.
     *
     * @return die Servlet-Registrierung für das ICS-Download-Servlet
     */
	@Bean
    public ServletRegistrationBean<ICSDownloadServlet> icsDownloadServlet() {
        return new ServletRegistrationBean<>(new ICSDownloadServlet(), "/ics-export/*");
    }
}