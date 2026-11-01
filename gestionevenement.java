package projet;

import java.sql.*;
import java.util.*;

public class gestionevenement {

    public boolean ajouterEvenement(evenement e) {
        String sql = "INSERT INTO EVENEMENTS (titre, date_event, date_fin, heure, heure_fin, description, type_event, important) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, new java.sql.Date(e.getDateEvent().getTime()));
            ps.setDate(3, e.getDateFin() != null ? new java.sql.Date(e.getDateFin().getTime()) : null);
            ps.setString(4, e.getHeure());
            ps.setString(5, e.getHeureFin());
            ps.setString(6, e.getDescription());
            ps.setString(7, e.getTypeEvent());
            ps.setInt(8, e.isImportant() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erreur ajout : " + ex.getMessage());
            return false;
        }
    }

    public List<evenement> getTousLesEvenements() {
        List<evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM EVENEMENTS ORDER BY date_event ASC";
        try (Connection conn = connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(extraireEvenement(rs));
        } catch (SQLException ex) {
            System.err.println("Erreur lecture : " + ex.getMessage());
        }
        return liste;
    }

    public List<evenement> getEvenementsImportants() {
        List<evenement> liste = new ArrayList<>();
        String sql = "SELECT * FROM EVENEMENTS WHERE important = 1 ORDER BY date_event ASC";
        try (Connection conn = connexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(extraireEvenement(rs));
        } catch (SQLException ex) {
            System.err.println("Erreur importants : " + ex.getMessage());
        }
        return liste;
    }

    public List<evenement> rechercherEvenements(String titre, String type, String date) {
        List<evenement> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM EVENEMENTS WHERE 1=1");
        if (titre != null && !titre.isEmpty())
            sql.append(" AND UPPER(titre) LIKE UPPER(?)");
        if (type != null && !type.isEmpty())
            sql.append(" AND UPPER(type_event) = UPPER(?)");
        if (date != null && !date.isEmpty())
            sql.append(" AND TO_CHAR(date_event, 'YYYY-MM-DD') = ?");
        sql.append(" ORDER BY date_event ASC");
        try (Connection conn = connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (titre != null && !titre.isEmpty()) ps.setString(i++, "%" + titre + "%");
            if (type  != null && !type.isEmpty())  ps.setString(i++, type);
            if (date  != null && !date.isEmpty())  ps.setString(i++, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(extraireEvenement(rs));
        } catch (SQLException ex) {
            System.err.println("Erreur recherche : " + ex.getMessage());
        }
        return liste;
    }

    public boolean modifierEvenement(evenement e) {
        String sql = "UPDATE EVENEMENTS SET titre=?, date_event=?, date_fin=?, heure=?, heure_fin=?, "
                   + "description=?, type_event=?, important=? WHERE id=?";
        try (Connection conn = connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, new java.sql.Date(e.getDateEvent().getTime()));
            ps.setDate(3, e.getDateFin() != null ? new java.sql.Date(e.getDateFin().getTime()) : null);
            ps.setString(4, e.getHeure());
            ps.setString(5, e.getHeureFin());
            ps.setString(6, e.getDescription());
            ps.setString(7, e.getTypeEvent());
            ps.setInt(8, e.isImportant() ? 1 : 0);
            ps.setInt(9, e.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erreur modification : " + ex.getMessage());
            return false;
        }
    }

    public boolean supprimerEvenement(int id) {
        String sql = "DELETE FROM EVENEMENTS WHERE id = ?";
        try (Connection conn = connexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erreur suppression : " + ex.getMessage());
            return false;
        }
    }

    private evenement extraireEvenement(ResultSet rs) throws SQLException {
        return new evenement(
            rs.getInt("id"),
            rs.getString("titre"),
            rs.getDate("date_event"),
            rs.getDate("date_fin"),
            rs.getString("heure"),
            rs.getString("heure_fin"),
            rs.getString("description"),
            rs.getString("type_event"),
            rs.getInt("important") == 1
        );
    }
}