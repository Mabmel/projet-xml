package cv24.cv24.entities;

import jakarta.persistence.*;
@Entity
@Table(name = "poste")
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identite_id")
    private Identite identite;

    @Column(name = "intitule")
    private String intiltule;

    @Column(name = "status")
    private TypeContart status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntiltule() {
        return intiltule;
    }

    public void setIntiltule(String intiltule) {
        this.intiltule = intiltule;
    }

    public TypeContart getStatus() {
        return status;
    }

    public void setStatus(TypeContart status) {
        this.status = status;
    }
    public Identite getIdentite() {
        return identite;
    }

    public void setIdentite(Identite identite) {
        this.identite = identite;
    }
}
