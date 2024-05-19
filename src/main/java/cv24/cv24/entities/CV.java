package cv24.cv24.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.List;

public class CV {
    @OneToOne(cascade = CascadeType.REMOVE)
    private Identite identite;

    @OneToOne(cascade = CascadeType.REMOVE)
    private Poste poste;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "cv")
    private List<Experience> experiences;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "cv")
    private List<Diplome> diplomes;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "cv")
    private List<Certification> certifications;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "cv")
    private List<Langue> langues;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "cv")
    private List<Autre> autres;

    public CV(Identite identite, Poste poste, List<Experience> experiences, List<Diplome> diplomes,
              List<Certification> certifications, List<Langue> langues, List<Autre> autres) {
        this.identite = identite;
        this.poste = poste;
        this.experiences = experiences;
        this.diplomes = diplomes;
        this.certifications = certifications;
        this.langues = langues;
        this.autres = autres;
    }
    public CV() {}

    public Identite getIdentite() {
        return identite;
    }

    public void setIdentite(Identite identite) {
        this.identite = identite;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public List<Diplome> getDiplomes() {
        return diplomes;
    }

    public void setDiplomes(List<Diplome> diplomes) {
        this.diplomes = diplomes;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }

    public List<Langue> getLangues() {
        return langues;
    }

    public void setLangues(List<Langue> langues) {
        this.langues = langues;
    }

    public List<Autre> getAutres() {
        return autres;
    }

    public void setAutres(List<Autre> autres) {
        this.autres = autres;
    }
}
