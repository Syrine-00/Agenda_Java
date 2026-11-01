package projet;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/*
 * Fournit la liste des jours fériés tunisiens pour une année donnée.
 * Les fêtes fixes sont calculées automatiquement.
 * Les fêtes islamiques (variables) sont approximées pour 2024-2027.
 */
public class JoursFeries {

    public static List<evenement> getJoursFeries(int annee) {
        List<evenement> feries = new ArrayList<>();

        //FÊTES FIXES =====
        ajouterFerie(feries, LocalDate.of(annee, Month.JANUARY,  1),  "Nouvel An");
        ajouterFerie(feries, LocalDate.of(annee, Month.MARCH,   20),  "Fête de l'Indépendance");
        ajouterFerie(feries, LocalDate.of(annee, Month.APRIL,    9),  "Journée des Martyrs");
        ajouterFerie(feries, LocalDate.of(annee, Month.MAY,      1),  "Fête du Travail");
        ajouterFerie(feries, LocalDate.of(annee, Month.JULY,    25),  "Fête de la République");
        ajouterFerie(feries, LocalDate.of(annee, Month.AUGUST,  13),  "Fête de la Femme");
        ajouterFerie(feries, LocalDate.of(annee, Month.OCTOBER, 15),  "Fête de l'Évacuation");
        ajouterFerie(feries, LocalDate.of(annee, Month.NOVEMBER, 7),  "Fête Nationale");

        // FÊTES ISLAMIQUES (approximations) =====
        Map<Integer, LocalDate[]> fetesIslamiques = new HashMap<>();

        // Format : {Aïd el-Fitr début, Aïd el-Adha début, Ras el-Am el-Hijri, Mawlid}
        fetesIslamiques.put(2024, new LocalDate[]{
            LocalDate.of(2024, Month.APRIL,    10),
            LocalDate.of(2024, Month.JUNE,     16),
            LocalDate.of(2024, Month.JULY,      7),
            LocalDate.of(2024, Month.SEPTEMBER,15)
        });
        fetesIslamiques.put(2025, new LocalDate[]{
            LocalDate.of(2025, Month.MARCH,    30),
            LocalDate.of(2025, Month.JUNE,      6),
            LocalDate.of(2025, Month.JUNE,     26),
            LocalDate.of(2025, Month.SEPTEMBER, 4)
        });
        fetesIslamiques.put(2026, new LocalDate[]{
            LocalDate.of(2026, Month.MARCH,    20),
            LocalDate.of(2026, Month.MAY,      27),
            LocalDate.of(2026, Month.JUNE,     16),
            LocalDate.of(2026, Month.AUGUST,   25)
        });
        fetesIslamiques.put(2027, new LocalDate[]{
            LocalDate.of(2027, Month.MARCH,     9),
            LocalDate.of(2027, Month.MAY,       16),
            LocalDate.of(2027, Month.JUNE,       5),
            LocalDate.of(2027, Month.AUGUST,    14)
        });

        LocalDate[] islamique = fetesIslamiques.get(annee);
        if (islamique != null) {
            // Aïd el-Fitr — 3 jours
            ajouterFerieMultiJours(feries, islamique[0], islamique[0].plusDays(2), "Aïd el-Fitr 🌙");
            // Aïd el-Adha — 3 jours
            ajouterFerieMultiJours(feries, islamique[1], islamique[1].plusDays(2), "Aïd el-Adha 🐑");
            // Ras el-Am el-Hijri — 1 jour
            ajouterFerie(feries, islamique[2], "Ras el-Am el-Hijri (Nouvel An islamique)");
            // Mawlid — 1 jour
            ajouterFerie(feries, islamique[3], "Mawlid (Naissance du Prophète) ﷺ");
        }

        return feries;
    }

    private static void ajouterFerie(List<evenement> liste, LocalDate date, String nom) {
    java.sql.Date d = java.sql.Date.valueOf(date);
    liste.add(new evenement(
        nom, d, null, null, null,
        "Jour férié tunisien", "Férié", false
    ));
}

private static void ajouterFerieMultiJours(List<evenement> liste,
        LocalDate debut, LocalDate fin, String nom) {
    java.sql.Date d = java.sql.Date.valueOf(debut);
    java.sql.Date f = java.sql.Date.valueOf(fin);
    liste.add(new evenement(
        nom, d, f, null, null,
        "Jour férié tunisien", "Férié", false
    ));
}
}