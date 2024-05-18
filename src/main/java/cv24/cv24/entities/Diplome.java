package cv24.cv24.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "diplome")
public class Diplome {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identite_id")
    private Identite identite;

    @Column(name = "niveau_qualification")
    private int niveauQualification;

    @Column(name = "date_obtention")
    private Date dateObtention;

    @Column(name = "institut")
    private String institut;

    @Column(name = "titre")
    private String titre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Identite getIdentite() {
        return identite;
    }

    public void setIdentite(Identite identite) {
        this.identite = identite;
    }

    public int getNiveauQualification() {
        return niveauQualification;
    }

    public void setNiveauQualification(int niveauQualification) {
        if (niveauQualification < 1 || niveauQualification > 8) {
            throw new IllegalArgumentException("Le niveau de qualification doit être compris entre 1 et 8.");
        }
        this.niveauQualification = niveauQualification;
    }

    public Date getDateObtention() {
        return dateObtention;
    }

    public void setDateObtention(Date dateObtention) {
        this.dateObtention = dateObtention;
    }

    public String getInstitut() {
        return institut;
    }

    public void setInstitut(String institut) {
        this.institut = institut;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
}
