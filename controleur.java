package projet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class controleur implements Initializable {

    //Composants FXML: calendrier principal
    @FXML private Label lblCurrentMonth;
    @FXML private AnchorPane colDay1, colDay2, colDay3, colDay4, colDay5, colDay6, colDay7;
    @FXML private Label lblDay1, lblDay2, lblDay3, lblDay4, lblDay5, lblDay6, lblDay7;
    @FXML private HBox dayHeaderBar;
    @FXML private HBox calendarGrid;
    @FXML private ScrollPane calendarScrollPane;
    @FXML private VBox calendarVBox;

    //Composants FXML: sidebar
    @FXML private Label lblMiniMonth;
    @FXML private GridPane miniCalendarGrid;
    @FXML private ComboBox<String> searchCriteria;
    @FXML private TextField sideSearchField;
    @FXML private ListView<String> upcomingEventsList;
    @FXML private CheckBox chkTravail, chkPersonnel, chkUrgent, chkReunion, chkAutre;
    @FXML private CheckBox chkConge, chkVoyage, chkAnniversaire, chkFerie;
    @FXML private VBox sidebarVBox;

    //Composants FXML: header
    @FXML private HBox headerHBox;
    @FXML private Button btnPrev, btnNext, btnToday, btnMiniPrev, btnMiniNext;

    // DAO et état de navigation
    private gestionevenement dao = new gestionevenement();
    private LocalDate currentWeekStart;
    private LocalDate miniCalendarDate;
    private boolean darkMode = false;
    private Scene mainScene;

    // Couleurs du thème sombre
    private static final String BG_DARK       = "#202124";
    private static final String BG_DARK_CARD  = "#2d2e31";
    private static final String BG_DARK_COL   = "#28292c"; 
    private static final String BG_DARK_WE    = "#232427"; 
    private static final String BORDER_DARK   = "#3c4043"; 
    private static final String TEXT_DARK     = "#e8eaed"; 
    private static final String TEXT_DARK_SUB = "#9aa0a6";

    // Types d'événements disponibles dans le formulaire
    private static final String[] TYPES = {
        "Travail", "Personnel", "Urgent", "Réunion", "Congé", "Voyage", "Anniversaire", "Autre"
    };

    // Couleur associée à chaque type d'événement
    private static final Map<String, String> TYPE_COLORS = new HashMap<>();
    static {
        TYPE_COLORS.put("Travail",      "#1a73e8");
        TYPE_COLORS.put("Personnel",    "#33b679");
        TYPE_COLORS.put("Urgent",       "#f4511e");
        TYPE_COLORS.put("Réunion",      "#8e24aa");
        TYPE_COLORS.put("Congé",        "#1b1c63");
        TYPE_COLORS.put("Voyage",       "#f6bf26");
        TYPE_COLORS.put("Anniversaire", "#e67c73");
        TYPE_COLORS.put("Férié",        "#1b5e20");
        TYPE_COLORS.put("Autre",        "#a79b8e");
    }

    // Convertit java.util.Date ou java.sql.Date en LocalDate sans risque de ClassCastException
    private static LocalDate toLocalDate(java.util.Date date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date)
            return ((java.sql.Date) date).toLocalDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Point d'entrée JavaFX : initialise l'état et affiche la semaine courante
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        miniCalendarDate = LocalDate.now();
        mettreAJourEnTetes();
        rafraichirCalendrier();
        rafraichirMiniCalendrier();
        rafraichirEvenementsImportants();
        configurerClicsColonnes();
        connecterBoutonsNavigation();
        // Configurer les checkboxes de filtrage
        configurerCheckboxes();
    }

    // Recoit la scène principale pour permettre les lookups CSS en dark mode
    public void setScene(Scene scene) {
        this.mainScene = scene;
    }

    // Connecte les boutons de navigation au cas où onAction n'est pas défini dans le FXML
    private void connecterBoutonsNavigation() {
        if (btnPrev     != null) btnPrev.setOnAction(e -> handlePrev());
        if (btnNext     != null) btnNext.setOnAction(e -> handleNext());
        if (btnToday    != null) btnToday.setOnAction(e -> handleToday());
        if (btnMiniPrev != null) btnMiniPrev.setOnAction(e -> handleMiniPrev());
        if (btnMiniNext != null) btnMiniNext.setOnAction(e -> handleMiniNext());
    }

    // Permet de cliquer sur un jour pour ouvrir le formulaire avec la date et l'heure pré-remplies.
    // L'heure est calculée à partir de la position verticale du clic (6h en haut, 1px = 1 min).
    private void configurerClicsColonnes() {
        AnchorPane[] colonnes = {colDay1, colDay2, colDay3, colDay4, colDay5, colDay6, colDay7};
        for (int i = 0; i < 7; i++) {
            final int index = i;
            if (colonnes[i] != null) {
                colonnes[i].setOnMouseClicked(e -> {
                    LocalDate jourClique = currentWeekStart.plusDays(index);
                    double y = e.getY();
                    int heure   = (int) ((y - 30) / 60) + 6;
                    int minutes = (int) (((y - 30) % 60) / 60.0 * 60);
                    minutes = minutes < 30 ? 0 : 30;
                    if (heure < 0)  heure = 0;
                    if (heure > 23) heure = 23;
                    ouvrirFormulaire(null, jourClique, String.format("%02d:%02d", heure, minutes));
                });
            }
        }
    }

    //Navigation

    @FXML private void handlePrev()  { currentWeekStart = currentWeekStart.minusWeeks(1); mettreAJourEnTetes(); rafraichirCalendrier(); }
    @FXML private void handleNext()  { currentWeekStart = currentWeekStart.plusWeeks(1);  mettreAJourEnTetes(); rafraichirCalendrier(); }
    @FXML private void handleToday() { currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY); mettreAJourEnTetes(); rafraichirCalendrier(); }

    @FXML private void handleMiniPrev() { miniCalendarDate = miniCalendarDate.minusMonths(1); rafraichirMiniCalendrier(); }
    @FXML private void handleMiniNext() { miniCalendarDate = miniCalendarDate.plusMonths(1);  rafraichirMiniCalendrier(); }

    // Met à jour le titre du mois et les numéros de jours dans l'en-tête du calendrier
    // Le jour d'aujourd'hui est mis en surbrillance avec un cercle bleu
    private void mettreAJourEnTetes() {
        String mois = currentWeekStart.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        mois = mois.substring(0, 1).toUpperCase() + mois.substring(1);
        lblCurrentMonth.setText(mois + " " + currentWeekStart.getYear());

        Label[] labels = {lblDay1, lblDay2, lblDay3, lblDay4, lblDay5, lblDay6, lblDay7};
        String textColor = darkMode ? TEXT_DARK : "#3c4043";
        for (int i = 0; i < 7; i++) {
            LocalDate jour = currentWeekStart.plusDays(i);
            if (labels[i] == null) continue;
            labels[i].setText(String.valueOf(jour.getDayOfMonth()));
            if (jour.equals(LocalDate.now())) {
                labels[i].setStyle(
                    "-fx-font-size: 20px; -fx-text-fill: white;" +
                    "-fx-background-color: #1a73e8; -fx-background-radius: 50;" +
                    "-fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center;");
            } else {
                labels[i].setStyle("-fx-font-size: 20px; -fx-text-fill: " + textColor + ";");
            }
        }
    }

    // Affiche le mini calendrier de la sidebar avec les jours correctement alignés
    // selon le premier jour du mois (Lundi = colonne 0).
    private void rafraichirMiniCalendrier() {
        miniCalendarGrid.getChildren().clear();

        String mois = miniCalendarDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        mois = mois.substring(0, 1).toUpperCase() + mois.substring(1);
        lblMiniMonth.setText(mois + " " + miniCalendarDate.getYear());

        LocalDate premier  = miniCalendarDate.withDayOfMonth(1);
        int debutCol       = premier.getDayOfWeek().getValue() - 1;
        int totalJours     = miniCalendarDate.lengthOfMonth();

        String btnStyle = darkMode
            ? "-fx-background-color: transparent; -fx-font-size: 11px; -fx-cursor: hand;" +
              "-fx-min-width: 24; -fx-min-height: 24; -fx-text-fill: " + TEXT_DARK + ";"
            : "-fx-background-color: transparent; -fx-font-size: 11px; -fx-cursor: hand;" +
              "-fx-min-width: 24; -fx-min-height: 24;";

        String btnTodayStyle =
            "-fx-background-color: #1a73e8; -fx-text-fill: white;" +
            "-fx-font-size: 11px; -fx-background-radius: 50;" +
            "-fx-min-width: 24; -fx-min-height: 24;";

        int col = debutCol, row = 0;
        for (int jour = 1; jour <= totalJours; jour++) {
            final LocalDate date = miniCalendarDate.withDayOfMonth(jour);
            Button btn = new Button(String.valueOf(jour));
            btn.setStyle(date.equals(LocalDate.now()) ? btnTodayStyle : btnStyle);
            
            btn.setPadding(new Insets(0)); // Supprime l'espace interne qui pousse le texte
            btn.setEllipsisString("");    // Supprime les "..." si le texte dépasse
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER);
    
            btn.setOnAction(e -> {
                currentWeekStart = date.with(DayOfWeek.MONDAY);
                mettreAJourEnTetes();
                rafraichirCalendrier();
            });
            miniCalendarGrid.add(btn, col, row);
            if (++col == 7) { col = 0; row++; }
        }
    }

    // Efface et redessine tous les événements de la semaine visible
    // Fusionne les événements de la base de données avec les jours fériés tunisiens
    private void rafraichirCalendrier() {
    AnchorPane[] colonnes = {colDay1, colDay2, colDay3, colDay4, colDay5, colDay6, colDay7};
    for (AnchorPane col : colonnes)
        if (col != null) col.getChildren().removeIf(n -> n instanceof VBox);

    List<evenement> tous = dao.getTousLesEvenements();
    List<evenement> feries = JoursFeries.getJoursFeries(currentWeekStart.getYear());
    List<evenement> complet = new ArrayList<>(tous);
    complet.addAll(feries);

    for (evenement ev : complet) {
        if (ev.getDateEvent() == null) continue;

        // Filtrer selon les checkboxes
        if (!typeEstVisible(ev.getTypeEvent())) continue;

        LocalDate dateEv  = ((java.sql.Date) ev.getDateEvent()).toLocalDate();
        LocalDate dateFin = ev.getDateFin() != null
            ? ((java.sql.Date) ev.getDateFin()).toLocalDate()
            : dateEv;

        for (int i = 0; i < 7; i++) {
            LocalDate jourCol = currentWeekStart.plusDays(i);
            if (!jourCol.isBefore(dateEv) && !jourCol.isAfter(dateFin))
                if (colonnes[i] != null)
                    afficherEvenementDansColonne(ev, colonnes[i]);
        }
    }
}

// Vérifie si le type est coché dans la sidebar
private boolean typeEstVisible(String type) {
    if (type == null) return true;
    switch (type) {
        case "Travail":      return chkTravail     == null || chkTravail.isSelected();
        case "Personnel":    return chkPersonnel   == null || chkPersonnel.isSelected();
        case "Urgent":       return chkUrgent      == null || chkUrgent.isSelected();
        case "Réunion":      return chkReunion     == null || chkReunion.isSelected();
        case "Congé":        return chkConge       == null || chkConge.isSelected();
        case "Voyage":       return chkVoyage      == null || chkVoyage.isSelected();
        case "Anniversaire": return chkAnniversaire== null || chkAnniversaire.isSelected();
        case "Férié":        return chkFerie       == null || chkFerie.isSelected();
        default:             return chkAutre       == null || chkAutre.isSelected();
    }
}

    // Crée et positionne le bloc visuel d'un événement dans une colonne de jour.
    // La position verticale est calculée à partir de l'heure (6h = haut, 1h = 60px).
    private void afficherEvenementDansColonne(evenement ev, AnchorPane colonne) {
        double topY = 30;
        if (ev.getHeure() != null && ev.getHeure().contains(":")) {
            try {
                String[] parts = ev.getHeure().split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                topY = Math.max(0, (h - 6) * 60 + m);
            } catch (Exception ignored) {}
        }

        String couleur = TYPE_COLORS.getOrDefault(ev.getTypeEvent(), "#be84cf");

        VBox bloc = new VBox(2);
        bloc.setStyle(
            "-fx-background-color: " + couleur + "22;" +
            "-fx-border-color: " + couleur + ";" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-padding: 2 4; -fx-cursor: hand;");
        bloc.setPrefWidth(colonne.getPrefWidth() - 4);
        bloc.setPrefHeight(50);

        Label titre = new Label(ev.isImportant() ? "⭐ " + ev.getTitre() : ev.getTitre());
        titre.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
        titre.setWrapText(true);
        bloc.getChildren().add(titre);

        // Affiche la plage horaire "08:00 → 09:30" si une heure est définie
        if (ev.getHeure() != null && !ev.getHeure().isEmpty()) {
            String plage = ev.getHeure();
            if (ev.getHeureFin() != null && !ev.getHeureFin().isEmpty())
                plage += " → " + ev.getHeureFin();
            Label heure = new Label(plage);
            heure.setStyle("-fx-font-size: 10px; -fx-text-fill: " + couleur + ";");
            bloc.getChildren().add(heure);
        }

        // Affiche la date de fin si l'événement s'étend sur plusieurs jours
        if (ev.getDateFin() != null) {
            LocalDate fin = toLocalDate(ev.getDateFin());
            if (!fin.equals(toLocalDate(ev.getDateEvent()))) {
                Label lblDateFin = new Label("→ " + fin);
                lblDateFin.setStyle("-fx-font-size: 10px; -fx-text-fill: " + couleur + "; -fx-font-style: italic;");
                bloc.getChildren().add(lblDateFin);
            }
        }

        AnchorPane.setTopAnchor(bloc, topY);
        AnchorPane.setLeftAnchor(bloc, 2.0);
        AnchorPane.setRightAnchor(bloc, 2.0);
        bloc.setOnMouseClicked(e -> { e.consume(); ouvrirFormulaire(ev, null, null); });
        colonne.getChildren().add(bloc);
    }

    // Affiche la liste des événements marqués importants dans la sidebar,
    // triés par date croissante. Un clic navigue vers la semaine de l'événement.
    private void rafraichirEvenementsImportants() {
        if (upcomingEventsList == null) return;
        ObservableList<String> items = FXCollections.observableArrayList();

        dao.getTousLesEvenements().stream()
            .filter(ev -> ev.isImportant() && ev.getDateEvent() != null)
            .sorted(Comparator.comparing(ev -> toLocalDate(ev.getDateEvent())))
            .forEach(ev -> items.add("⭐ " + toLocalDate(ev.getDateEvent()) + " — " + ev.getTitre()));

        upcomingEventsList.setItems(items);

        final String bgCell = darkMode ? BG_DARK_CARD : "transparent";
        final String fgCell = darkMode ? TEXT_DARK    : "#3c4043";
        upcomingEventsList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle("-fx-background-color: " + bgCell + ";"); }
                else { setText(item); setStyle("-fx-background-color: " + bgCell + "; -fx-text-fill: " + fgCell + "; -fx-font-size: 11px;"); }
            }
        });

        upcomingEventsList.setOnMouseClicked(e -> {
            int idx = upcomingEventsList.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            List<evenement> importants = new ArrayList<>();
            dao.getTousLesEvenements().stream()
                .filter(ev -> ev.isImportant() && ev.getDateEvent() != null)
                .sorted(Comparator.comparing(ev -> toLocalDate(ev.getDateEvent())))
                .forEach(importants::add);
            if (idx < importants.size()) {
                currentWeekStart = toLocalDate(importants.get(idx).getDateEvent()).with(DayOfWeek.MONDAY);
                mettreAJourEnTetes();
                rafraichirCalendrier();
            }
        });
    }

    @FXML
    private void handleAddEvent() {
        ouvrirFormulaire(null, LocalDate.now(), "");
    }

    // Cherche les événements selon le critère sélectionné (Titre, Catégorie ou Date)
    // et navigue automatiquement vers la semaine du premier résultat.
    @FXML
    private void handleSearch() {
        String critere = searchCriteria.getValue();
        String valeur  = sideSearchField.getText();
        if (critere == null || valeur.isEmpty()) { rafraichirCalendrier(); return; }

        List<evenement> resultats;
        switch (critere) {
            case "Titre":     resultats = dao.rechercherEvenements(valeur, null, null); break;
            case "Catégorie": resultats = dao.rechercherEvenements(null, valeur, null); break;
            case "Date":      resultats = dao.rechercherEvenements(null, null, valeur); break;
            default:          resultats = dao.getTousLesEvenements();
        }

        if (!resultats.isEmpty() && resultats.get(0).getDateEvent() != null) {
            currentWeekStart = toLocalDate(resultats.get(0).getDateEvent()).with(DayOfWeek.MONDAY);
            mettreAJourEnTetes();
        }

        AnchorPane[] colonnes = {colDay1, colDay2, colDay3, colDay4, colDay5, colDay6, colDay7};
        for (AnchorPane col : colonnes)
            if (col != null) col.getChildren().removeIf(n -> n instanceof VBox);

        for (evenement ev : resultats) {
            if (ev.getDateEvent() == null) continue;
            LocalDate dateEv  = toLocalDate(ev.getDateEvent());
            LocalDate dateFin = ev.getDateFin() != null ? toLocalDate(ev.getDateFin()) : dateEv;
            for (int i = 0; i < 7; i++) {
                LocalDate jourCol = currentWeekStart.plusDays(i);
                if (!jourCol.isBefore(dateEv) && !jourCol.isAfter(dateFin) && colonnes[i] != null)
                    afficherEvenementDansColonne(ev, colonnes[i]);
            }
        }
    }
    
    private void configurerCheckboxes() {
    // Ajouter les listeners sur chaque checkbox
    if (chkTravail    != null) chkTravail.setOnAction(e -> rafraichirCalendrier());
    if (chkPersonnel  != null) chkPersonnel.setOnAction(e -> rafraichirCalendrier());
    if (chkUrgent     != null) chkUrgent.setOnAction(e -> rafraichirCalendrier());
    if (chkReunion    != null) chkReunion.setOnAction(e -> rafraichirCalendrier());
    if (chkAutre      != null) chkAutre.setOnAction(e -> rafraichirCalendrier());
    if (chkConge      != null) chkConge.setOnAction(e -> rafraichirCalendrier());
    if (chkVoyage     != null) chkVoyage.setOnAction(e -> rafraichirCalendrier());
    if (chkAnniversaire != null) chkAnniversaire.setOnAction(e -> rafraichirCalendrier());
    if (chkFerie      != null) chkFerie.setOnAction(e -> rafraichirCalendrier());
}

    @FXML
    private void basculerTheme() {
        darkMode = !darkMode;
        appliquerTheme();
    }

    // Applique le thème clair ou sombre à tous les composants de l'interface
    // Les composants inline (popup, blocs d'événements) utilisent les constantes BG_DARK_*.
    private void appliquerTheme() {
        String bgPrincipal = darkMode ? BG_DARK      : "#ffffff";
        String bgSidebar   = darkMode ? BG_DARK_CARD : "#ffffff";
        String bgHeader    = darkMode ? BG_DARK_CARD : "#ffffff";
        String borderColor = darkMode ? BORDER_DARK  : "#e0e0e0";
        String textColor   = darkMode ? TEXT_DARK    : "#3c4043";
        String colSemaine  = darkMode ? BG_DARK_COL  : "#ffffff";
        String colWE       = darkMode ? BG_DARK_WE   : "#fafafa";
        String scrollBg    = darkMode ? BG_DARK      : "#ffffff";

        if (mainScene != null)
            mainScene.getRoot().setStyle("-fx-background-color: " + bgPrincipal + ";");

        if (headerHBox != null)
            headerHBox.setStyle("-fx-background-color: " + bgHeader + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0; -fx-padding: 8 16 8 16;");

        if (sidebarVBox != null)
            sidebarVBox.setStyle("-fx-background-color: " + bgSidebar + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 1 0 0;");

        if (dayHeaderBar != null)
            dayHeaderBar.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0; -fx-background-color: " + bgHeader + ";");

        if (calendarScrollPane != null) {
            calendarScrollPane.setStyle("-fx-background-color: " + scrollBg + "; -fx-border-color: transparent;");
            if (calendarScrollPane.getContent() != null)
                calendarScrollPane.getContent().setStyle("-fx-background-color: " + scrollBg + ";");
        }

        // Colonnes semaine vs week-end (légèrement différenciées)
        AnchorPane[] colonnes = {colDay1, colDay2, colDay3, colDay4, colDay5, colDay6, colDay7};
        for (int i = 0; i < 7; i++) {
            if (colonnes[i] == null) continue;
            colonnes[i].setStyle(
                "-fx-background-color: " + ((i >= 5) ? colWE : colSemaine) + ";" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: " + (i < 6 ? "0 1 0 0" : "0") + ";");
        }

        if (lblCurrentMonth != null) lblCurrentMonth.setStyle("-fx-font-size: 20px; -fx-text-fill: " + textColor + ";");
        if (lblMiniMonth    != null) lblMiniMonth.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + ";");

        mettreAJourEnTetes();
        rafraichirMiniCalendrier();

        if (mainScene != null) {
            Button btnTheme = (Button) mainScene.lookup("#btnTheme");
            if (btnTheme != null) {
                btnTheme.setText(darkMode ? "☀ Clair" : "🌙 Sombre");
                btnTheme.setStyle(darkMode
            ? "-fx-background-color: transparent; -fx-border-color: #5f6368; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px; -fx-padding: 6 16; -fx-cursor: hand; -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-border-color: #dadce0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px; -fx-padding: 6 16; -fx-cursor: hand; -fx-text-fill: #3c4043;");
            } 
            
        }

        // Bouton "Aujourd'hui"
        if (btnToday != null)
            btnToday.setStyle(darkMode
                ? "-fx-background-color: transparent; -fx-border-color: #5f6368; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px; -fx-padding: 6 16; -fx-cursor: hand; -fx-text-fill: " + TEXT_DARK + ";"
                : "-fx-background-color: transparent; -fx-border-color: #dadce0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 13px; -fx-padding: 6 16; -fx-cursor: hand; -fx-text-fill: #3c4043;");

        // Boutons < > navigation semaine
        String navStyle = darkMode
            ? "-fx-background-color: transparent; -fx-border-color: #5f6368; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 18px; -fx-text-fill: " + TEXT_DARK + "; -fx-padding: 2 10; -fx-cursor: hand;"
            : "-fx-background-color: transparent; -fx-border-color: #dadce0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 18px; -fx-text-fill: #5f6368; -fx-padding: 2 10; -fx-cursor: hand;";
        if (btnPrev != null) btnPrev.setStyle(navStyle);
        if (btnNext != null) btnNext.setStyle(navStyle);

        // Checkboxes des catégories
        String chkStyle = darkMode
            ? "-fx-font-size: 12px; -fx-text-fill: " + TEXT_DARK + ";"
            : "-fx-font-size: 12px; -fx-text-fill: #3c4043;";
        if (chkTravail   != null) chkTravail.setStyle(chkStyle);
        if (chkPersonnel != null) chkPersonnel.setStyle(chkStyle);
        if (chkUrgent    != null) chkUrgent.setStyle(chkStyle);
        if (chkReunion   != null) chkReunion.setStyle(chkStyle);
        if (chkConge   != null) chkConge.setStyle(chkStyle);
        if (chkVoyage   != null) chkVoyage.setStyle(chkStyle);
        if (chkAnniversaire   != null) chkAnniversaire.setStyle(chkStyle);
        if (chkFerie   != null) chkFerie.setStyle(chkStyle);
        if (chkAutre     != null) chkAutre.setStyle(chkStyle);

        // Liste des événements importants
        if (upcomingEventsList != null) {
            if (darkMode) {
                upcomingEventsList.setStyle("-fx-background-color: " + BG_DARK_CARD + "; -fx-border-color: transparent; -fx-control-inner-background: " + BG_DARK_CARD + "; -fx-text-fill: " + TEXT_DARK + ";");
                upcomingEventsList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle("-fx-background-color: " + BG_DARK_CARD + ";"); }
                        else { setText(item); setStyle("-fx-background-color: " + BG_DARK_CARD + "; -fx-text-fill: " + TEXT_DARK + "; -fx-font-size: 11px;"); }
                    }
                });
            } else {
                upcomingEventsList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                upcomingEventsList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); }
                        else { setText(item); setStyle("-fx-font-size: 11px; -fx-text-fill: #3c4043;"); }
                    }
                });
            }
        }

        // Labels statiques de la sidebar (Rechercher par, Catégories, Événements importants)
        if (mainScene != null) {
            String labelSideStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (darkMode ? TEXT_DARK : "#5f6368") + ";";
            mainScene.getRoot().lookupAll(".label").forEach(node -> {
                if (node instanceof Label) {
                    Label lbl = (Label) node;
                    String txt = lbl.getText();
                    if ("Rechercher par".equals(txt) || "Catégories".equals(txt) || txt.startsWith("Événements importants"))
                        lbl.setStyle(labelSideStyle);
                }
            });
        }
    }

    // Ouvre le formulaire de création ou de modification d'un événement.
    // Si evExistant est null, le formulaire est en mode création avec date/heure pré-remplies.
    // Si evExistant est fourni, tous les champs sont pré-remplis pour modification.
    private void ouvrirFormulaire(evenement evExistant, LocalDate datePreselect, String heurePreselect) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(evExistant == null ? "Nouvel événement" : "Modifier l'événement");

        String popupBg    = darkMode ? BG_DARK_CARD : "#ffffff";
        String labelColor = darkMode ? "#cccccc"    : "#5f6368";
        String fieldBg    = darkMode ? "#2d2d2d"    : "#ffffff";
        String fieldFg    = darkMode ? TEXT_DARK    : "#000000";
        String fieldStyle = "-fx-background-color: " + fieldBg + "; -fx-text-fill: " + fieldFg + "; -fx-prompt-text-fill: #9aa0a6;";

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + popupBg + ";");

        TextField champTitre    = new TextField();  champTitre.setPromptText("Titre *");                               champTitre.setStyle(fieldStyle);
        DatePicker champDate    = new DatePicker(); champDate.setPromptText("Date de début *");                        champDate.setStyle(fieldStyle);
        DatePicker champDateFin = new DatePicker(); champDateFin.setPromptText("Date de fin (optionnel)");             champDateFin.setStyle(fieldStyle);
        TextField champHeure    = new TextField();  champHeure.setPromptText("Heure (optionnel) ex: 14:30");           champHeure.setStyle(fieldStyle);
        TextField champHeureFin = new TextField();  champHeureFin.setPromptText("Heure de fin (optionnel) ex: 15:30"); champHeureFin.setStyle(fieldStyle);
        TextArea  champDesc     = new TextArea();   champDesc.setPromptText("Description (optionnel)");                champDesc.setStyle(fieldStyle); champDesc.setPrefHeight(70);

        ComboBox<String> champType = new ComboBox<>();
        champType.setItems(FXCollections.observableArrayList(TYPES));
        champType.setPromptText("Type d'événement");
        champType.setPrefWidth(300);
        champType.setStyle(fieldStyle);

        CheckBox champImportant = new CheckBox("Marquer comme important ⭐");
        champImportant.setStyle("-fx-text-fill: " + labelColor + ";");

        ComboBox<String> champRepetition = new ComboBox<>();
        champRepetition.setItems(FXCollections.observableArrayList("Aucune", "Chaque semaine", "Chaque mois"));
        champRepetition.setValue("Aucune");
        champRepetition.setPrefWidth(300);
        champRepetition.setStyle(fieldStyle);

        // Pré-remplissage en mode modification
        if (evExistant != null) {
            champTitre.setText(evExistant.getTitre());
            if (evExistant.getDateEvent() != null)  champDate.setValue(toLocalDate(evExistant.getDateEvent()));
            if (evExistant.getDateFin()   != null)  champDateFin.setValue(toLocalDate(evExistant.getDateFin()));
            champHeure.setText(evExistant.getHeure()     != null ? evExistant.getHeure()     : "");
            champHeureFin.setText(evExistant.getHeureFin() != null ? evExistant.getHeureFin() : "");
            champType.setValue(evExistant.getTypeEvent());
            champDesc.setText(evExistant.getDescription() != null ? evExistant.getDescription() : "");
            champImportant.setSelected(evExistant.isImportant());
        } else {
            if (datePreselect  != null) champDate.setValue(datePreselect);
            if (heurePreselect != null && !heurePreselect.isEmpty()) champHeure.setText(heurePreselect);
        }

        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        Button btnSauvegarder = new Button(evExistant == null ? "Créer" : "Modifier");
        btnSauvegarder.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 20;");

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: transparent; -fx-border-color: " + (darkMode ? "#5f6368" : "#dadce0") + "; -fx-text-fill: " + labelColor + "; -fx-background-radius: 4; -fx-padding: 8 20;");
        btnAnnuler.setOnAction(e -> popup.close());

        // Bouton Supprimer visible uniquement en mode modification
        if (evExistant != null) {
            Button btnSupprimer = new Button("Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #f4511e; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 20;");
            btnSupprimer.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer : " + evExistant.getTitre() + " ?");
                confirm.showAndWait().ifPresent(rep -> {
                    if (rep == ButtonType.OK) {
                        dao.supprimerEvenement(evExistant.getId());
                        rafraichirCalendrier();
                        rafraichirEvenementsImportants();
                        popup.close();
                    }
                });
            });
            boutons.getChildren().add(btnSupprimer);
        }

        btnSauvegarder.setOnAction(e -> {
            if (champTitre.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Le titre est obligatoire.").showAndWait(); return;
            }
            if (champDate.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "La date de début est obligatoire.").showAndWait(); return;
            }
            if (champDateFin.getValue() != null && champDateFin.getValue().isBefore(champDate.getValue())) {
                new Alert(Alert.AlertType.ERROR, "La date de fin doit être après la date de début.").showAndWait(); return;
            }

            Date dateDebut = Date.from(champDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateFin   = champDateFin.getValue() != null
                ? Date.from(champDateFin.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

            if (evExistant == null) {
                dao.ajouterEvenement(new evenement(
                    champTitre.getText(), dateDebut, dateFin,
                    champHeure.getText(), champHeureFin.getText(),
                    champDesc.getText(), champType.getValue(), champImportant.isSelected()
                ));
                creerOccurrencesRepetition(
                    champTitre.getText(), dateDebut, dateFin,
                    champHeure.getText(), champHeureFin.getText(),
                    champDesc.getText(), champType.getValue(), champImportant.isSelected(),
                    champRepetition.getValue(), champDate.getValue()
                );
            } else {
                evExistant.setTitre(champTitre.getText());
                evExistant.setDateEvent(dateDebut);
                evExistant.setDateFin(dateFin);
                evExistant.setHeure(champHeure.getText());
                evExistant.setHeureFin(champHeureFin.getText());
                evExistant.setDescription(champDesc.getText());
                evExistant.setTypeEvent(champType.getValue());
                evExistant.setImportant(champImportant.isSelected());
                dao.modifierEvenement(evExistant);
            }

            rafraichirCalendrier();
            rafraichirEvenementsImportants();
            popup.close();
        });

        boutons.getChildren().addAll(btnAnnuler, btnSauvegarder);

        String ls = "-fx-text-fill: " + labelColor + ";";
        Label lTitre    = new Label("Titre *");                  lTitre.setStyle(ls);
        Label lDate     = new Label("Date de début *");          lDate.setStyle(ls);
        Label lDateFin  = new Label("Date de fin (optionnel)");  lDateFin.setStyle(ls);
        Label lHeure    = new Label("Heure (optionnel)");        lHeure.setStyle(ls);
        Label lHeureFin = new Label("Heure de fin");             lHeureFin.setStyle(ls);
        Label lType     = new Label("Type");                     lType.setStyle(ls);
        Label lDesc     = new Label("Description");              lDesc.setStyle(ls);
        Label lRep      = new Label("Répétition");               lRep.setStyle(ls);

        root.getChildren().addAll(
            lTitre, champTitre, lDate, champDate, lDateFin, champDateFin,
            lHeure, champHeure, lHeureFin, champHeureFin,
            lType, champType, lDesc, champDesc,
            champImportant, lRep, champRepetition, boutons
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + popupBg + "; -fx-background-color: " + popupBg + ";");

        popup.setScene(new Scene(scroll, 400, 600));
        popup.show();
    }

    // Génère les occurrences futures d'un événement répété (12 occurrences max).
    // La date de fin est décalée du même nombre de jours que l'événement original.
    private void creerOccurrencesRepetition(
            String titre, Date dateDebutOriginal, Date dateFinOriginal,
            String heure, String heurefin, String description, String type,
            boolean important, String repetition, LocalDate dateDebut) {

        if (repetition == null || repetition.equals("Aucune")) return;

        for (int i = 1; i <= 12; i++) {
            LocalDate nouvelleDate = repetition.equals("Chaque semaine")
                ? dateDebut.plusWeeks(i) : dateDebut.plusMonths(i);

            Date dateOcc    = Date.from(nouvelleDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateFinOcc = null;

            if (dateFinOriginal != null) {
                long joursEcart = java.time.temporal.ChronoUnit.DAYS.between(
                    dateDebut, toLocalDate(dateFinOriginal));
                dateFinOcc = Date.from(
                    nouvelleDate.plusDays(joursEcart).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            dao.ajouterEvenement(new evenement(
                titre, dateOcc, dateFinOcc, heure, heurefin, description, type, important
            ));
        }
    }
}
