package cv24.cv24.controller;
import cv24.cv24.entities.*;
import cv24.cv24.repository.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.IOException;
import java.io.StringReader;

@RestController
public class CvController {
    private final IdentiteRepository identiteRepository;
    private final PosteRepository posteRepository;
    private final ExperienceRepository experienceRepository;
    private final DiplomeRepository diplomeRepository;
    private final CertificationRepository certificationRepository;
    private final LangueRepository langueRepository;
    private final AutreRepository autreRepository;

    public CvController(IdentiteRepository identiteRepository, PosteRepository posteRepository, ExperienceRepository experienceRepository, DiplomeRepository diplomeRepository, CertificationRepository certificationRepository, LangueRepository langueRepository, AutreRepository autreRepository) {
        this.identiteRepository = identiteRepository;
        this.posteRepository = posteRepository;
        this.experienceRepository = experienceRepository;
        this.diplomeRepository = diplomeRepository;
        this.certificationRepository = certificationRepository;
        this.langueRepository = langueRepository;
        this.autreRepository = autreRepository;
    }

    @PostMapping("/validate-xml")
    public Boolean validateXML(@RequestBody String xmlString) {
        Boolean etat =false;
        String xsdFichierPath = "classpath:xml/shema.xsd";


        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));

            Validator validator = schema.newValidator();

            validator.validate(new DOMSource(document));

            etat=true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            etat=false;
        }
    if(etat){

        XMLParser xp = new XMLParser();

        CV cv = xp.parseXML(xmlString);

        identiteRepository.save(cv.getIdentite());
        Poste poste = cv.getPoste();
        poste.setIdentite(cv.getIdentite());
        // Enregistrer Poste
        posteRepository.save(poste);

        // Enregistrer Experiences
        for (Experience experience : cv.getExperiences()) {
            experience.setIdentite(cv.getIdentite());
            experienceRepository.save(experience);
        }

        // Enregistrer Diplomes
        for (Diplome diplome : cv.getDiplomes()) {
            diplome.setIdentite(cv.getIdentite());
            diplomeRepository.save(diplome);
        }

        // Enregistrer Certifications
        for (Certification certification : cv.getCertifications()) {
            certification.setIdentite(cv.getIdentite());
            certificationRepository.save(certification);
        }

        // Enregistrer Langues
        for (Langue langue : cv.getLangues()) {
            langue.setIdentite(cv.getIdentite());
            langueRepository.save(langue);
        }

        // Enregistrer Autres
        for (Autre autre : cv.getAutres()) {
            autre.setIdentite(cv.getIdentite());
            autreRepository.save(autre);
        }



        return true;
    }else {
        return false;
    }
    }
}
