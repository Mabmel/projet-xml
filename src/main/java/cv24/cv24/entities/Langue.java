package cv24.cv24.entities;

import jakarta.persistence.*;
import org.hibernate.boot.beanvalidation.IntegrationException;

@Entity
@Table(name = "langue")
public class Langue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identite_id")
    private Identite identite;

    @Column(name = "nom")
    private String nom;
    @Enumerated(EnumType.STRING)
    @Column(name = "cert")
    private Cert cert;
    @Enumerated(EnumType.STRING)
    @Column(name = "nivs")
    private Niveaux nivs;
    @Column(name = "nivi")
    private int nivi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String name) {
        this.nom = name;
    }

    public Cert getCert() {
        return cert;
    }

    public void setCert(Cert cert) {
        this.cert = cert;
    }
    public Identite getIdentite() {
        return identite;
    }

    public void setIdentite(Identite identite) {
        this.identite = identite;
    }
    public Niveaux getNivs() {
        return nivs;
    }

    public void setNivs(Niveaux nivs) {
        this.nivs = nivs;
    }

    public int getNivi() {
        return nivi;
    }

    public void setNivi(int nivi) {
        this.nivi = nivi;
    }
}
