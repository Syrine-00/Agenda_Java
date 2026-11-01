package projet;
import java.util.Date;

public class evenement {
    private int id;
    private String titre;
    private Date dateEvent;
    private Date dateFin;
    private String heure;
    private String heureFin;
    private String description;
    private String typeEvent;
    private boolean important;

    public evenement(int id, String titre, Date dateEvent, Date dateFin, String heure, String heureFin,
                     String description, String typeEvent, boolean important) {
        this.id = id; this.titre = titre; this.dateEvent = dateEvent;
        this.dateFin = dateFin; this.heure = heure; this.heureFin = heureFin;
        this.description = description; this.typeEvent = typeEvent; this.important = important;
    }

    public evenement(String titre, Date dateEvent, Date dateFin, String heure, String heureFin,
                     String description, String typeEvent, boolean important) {
        this.titre = titre; this.dateEvent = dateEvent; this.dateFin = dateFin;
        this.heure = heure; this.heureFin = heureFin; this.description = description;
        this.typeEvent = typeEvent; this.important = important;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Date getDateEvent() { return dateEvent; }
    public void setDateEvent(Date dateEvent) { this.dateEvent = dateEvent; }
    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }
    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTypeEvent() { return typeEvent; }
    public void setTypeEvent(String typeEvent) { this.typeEvent = typeEvent; }
    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }
}