# 🧭 Webapp zur Klausurplanung an einer Hochschule

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Vaadin](https://img.shields.io/badge/Vaadin-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)

Diese Anwendung ermöglicht Studierenden, Dozierenden und Hochschulangehörigen, **Klausuren und Prüfungen einzusehen** und **persönlich zu planen**.  
Über eine intuitive Weboberfläche können Prüfungen gefiltert, vorgemerkt und für externe Kalenderdienste exportiert werden.

---

## 🎯 Funktionen

- **Klausuren filtern** nach Studiengang, Semester oder Modul  
- **Vormerken gewünschter Prüfungen** für den späteren Export  
- **Export als .ICS (iCalendar)** zur Integration in externe Kalender (z. B. Google Calendar)  
- **Drei Kalenderansichten**: Monat, Woche, Tag  
- **Persönlicher User-Link**, um vorgemerkte Klausuren jederzeit wieder aufzurufen  
- **Dauerhafter ICS-Link** für automatische Synchronisation mit externen Kalendern  

---

## 🧱 Technologien

| Komponente | Beschreibung |
|-------------|--------------|
| **Java** | Programmiersprache |
| **Spring Boot** | Anwendungskern & Dependency Injection |
| **Vaadin** | UI-Framework für Weboberflächen |
| **FullCalendar** | Kalenderdarstellung und Navigation |
| **SQLite** | Leichtgewichtige Datenbank |
| **EclipseStore** | Persistenzschicht für Benutzer- und Filterdaten |

---

## ⚙️ Installation

1. **Repository klonen**
   ```bash
   git clone https://github.com/yvanzambou/vaadin-fullcalender-java.git
   cd vaadin-fullcalender-java
   ```

2. **Apache Maven installieren**
   
   [Binary-Datei](https://maven.apache.org/download.cgi) herunterladen und Pfad zum `/bin`-Ordner in die Umgebungsvariable hinzufügen (PATH).

3. **Build ausführen**
   ```bash
   mvn clean package
   ```

4. **Anwendung starten**
   ```bash
   mvn spring-boot:run
   ```

5. **Aufruf im Browser**
   
   http://localhost:8080/klausurplan

---

## 💾 Datenhaltung

- Alle Daten (Benutzer- und KlausurenIDs ) werden mit EclipseStore persistiert.
- Als Speicherziel dient eine SQLite-Datenbankdatei (`users-storage.db`) im Projektverzeichnis, die erst beim Start der Anwendung angelegt wird.
- Jeder Benutzer erhält eine eindeutige UUID und kann seine persönlichen Filter- und Exportdaten über einen individuellen Link wieder aufrufen.

---

## 📄 Hinweis  

Dieses Projekt entstand im Rahmen meiner Bachelorarbeit zum Thema „Webanwendung zur Klausurplanung an einer Hochschule“.
Aus Datenschutzgründen wurden alle Dozentennamen und personenbezogenen Daten im enthaltenen Beispieldatensatz durch prominente Namen ersetzt, die in keinerlei Verbindung zur Hochschule oder zur tatsächlichen Anwendung stehen.
Alle Namen wurden zufällig gewählt und haben keinen realen Bezug.
Die Anwendung dient ausschließlich Demonstrations- und Evaluationszwecken.

---

## 👨‍💻 Autor  
**Yvan Zambou**  

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Profil-blue?logo=linkedin)](https://linkedin.com/in/yvan-zambou-29aba9261)  
[![GitHub](https://img.shields.io/badge/GitHub-Projekte-black?logo=github)](https://github.com/yvanzambou)  

---