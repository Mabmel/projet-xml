package cv24.cv24.controller;
import cv24.cv24.entities.*;
import cv24.cv24.repository.*;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class CvController {

    //Ajout desx log pour la gestion
    private static final Logger logger = LoggerFactory.getLogger(CvController.class);


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

    @PostMapping("/cv24/insert")
    public ResponseEntity<String> validateXML(@RequestBody String xmlString) {
        Boolean etat = false;
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

            etat = true;
        } catch (SAXParseException e) {
            // Capturer les détails de l'erreur SAXParseException
            int lineNumber = e.getLineNumber();
            int columnNumber = e.getColumnNumber();
            String errorMessage = e.getMessage();


            // Journaliser l'erreur
            logger.error("Erreur de validation XML : Ligne {}, Colonne {} - {}", lineNumber, columnNumber, errorMessage);
            // Construire la réponse d'erreur avec les détails
            String response = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Erreur de validation XML : Ligne %d, Colonne %d - %s</status>", lineNumber, columnNumber, errorMessage);
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(response);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Journaliser l'erreur
            logger.error("Une erreur est survenue lors de la validation XML : {}", e.getMessage());

            etat = false;
        }

        if (etat) {
            XMLParser xp = new XMLParser();
            CV cv = xp.parseXML(xmlString);

            if (existeIdentiteDupliquee(cv)) {
                // Le CV existe déjà, retourner un message d'erreur
                logger.warn("Le CV existe déjà dans la base de données");
                String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Erreur : CV déjà existant</status>";
                return ResponseEntity.badRequest()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            } else {
                // Enregistrement du CV dans la base de données
                Long id = saveCV(cv);

                // Construire la réponse XML avec l'en-tête
                logger.info("CV inséré avec succès dans la base de données");
                String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cv id=\"" + id + "\" status=\"INSERTED\"/>";
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            }
        } else {
            // La validation a échoué, retourner un message d'erreur
            logger.warn("Validation XML a échoué");
            String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Erreur de validation XML : Le fichier XML est invalide</status>";
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(response);


        }
    }


    // Méthode pour enregistrer un nouveau CV dans la base de données
    private Long saveCV(CV cv) {

        Identite savedIdentite = identiteRepository.save(cv.getIdentite());
        Long identiteId = savedIdentite.getId();
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

        // Retourner l'identifiant principal, par exemple, l'identifiant de l'identité
        return identiteId;

    }

    private boolean existeIdentiteDupliquee(CV cv) {
        // Récupérer toutes les données d'identité de la base de données
        List<Identite> identites = identiteRepository.findAll();

        // Parcourir les données d'identité de la base de données pour comparer avec le CV
        for (Identite identite : identites) {
            String telAsString = identite.getTel().toString();
            String sString =cv.getIdentite().getTel().toString();

            // Comparer nom, prénom et téléphone en ignorant la casse
            if (identite.getNom().equalsIgnoreCase(cv.getIdentite().getNom()) &&
                    identite.getPrenom().equalsIgnoreCase(cv.getIdentite().getPrenom()))
                    {
                // Si des correspondances sont trouvées, retourner une indication d'erreur
                return true;
            }
            if(telAsString.equals(sString)){
                return true;
            }
        }
        // Aucune identité dupliquée trouvée
        return false;
    }




    @GetMapping(value = "/cv24/resume/xml", produces = "application/xml")
    @ResponseBody
    public String getAllCVsForXML() {
        XMLParser xp = new XMLParser(); // Initialisation de xp ici

        try {
            List<Identite> identites = identiteRepository.findAll();
            List<CV> cvs = new ArrayList<>();

            for (Identite identite : identites) {
                CV cv = new CV();
                cv.setIdentite(identite);
                cv.setPoste(posteRepository.findByIdentiteId(identite.getId()).orElse(null));
                cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
                cv.setDiplomes(diplomeRepository.findByIdentiteId(identite.getId()));
                cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
                cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
                cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));
                cvs.add(cv);
            }

            return xp.parseDataToXML(cvs); // Utilisation de xp ici
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la génération du XML pour les CVs : {}", e.getMessage());
            return xp.generateErrorXML("Une erreur est survenue lors de la génération du XML pour les CVs.");
        }
    }



    @GetMapping(value = "/cv24/xml", produces = "application/xml")
    @ResponseBody
    public String getCVDetailInXML(@RequestParam("id") Long id) throws ParserConfigurationException, IOException, SAXException {
        Identite identite = identiteRepository.findById(id).orElse(null);
        XMLParser xp = new XMLParser();
        if (identite == null) {
            return xp.generateErrorXML("Identité non trouvée pour l'ID: " + id);
        }

        CV cv = new CV();
        cv.setIdentite(identite);
        cv.setPoste(posteRepository.findByIdentiteId(identite.getId()).orElse(null));
        cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
        cv.setDiplomes(diplomeRepository.findByIdentiteId(identite.getId()));
        cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
        cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
        cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));

        String fxml = xp.parseDataCVToXML(cv);
        String xsdFichierPath = "classpath:xml/shema.xsd";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(fxml)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));
            Validator validator = schema.newValidator();
            logger.info("génération du XML du CV");
            validator.validate(new DOMSource(document));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Une erreur est survenue lors de la génération du XML d'detail d'un CV: {}", e.getMessage());
            return xp.generateErrorXML("Erreur de validation du XML par rapport au schéma XSD: " + e.getMessage());
        }
        return fxml;

    }

    @DeleteMapping(value = "/cv24/delete/{id}", produces = "application/xml")
    @Transactional
    public ResponseEntity<String> deleteCV(@PathVariable Long id) {
        logger.info("Requête reçue pour supprimer le CV avec l'id : {}", id);
        try {
            Optional<Identite> identiteOptional = identiteRepository.findById(id);
            if (identiteOptional.isPresent()) {
                Identite identite = identiteOptional.get();

                // Supprimer les certifications
                certificationRepository.deleteByIdentiteId(identite.getId());

                // Supprimer les autres données liées à cette identité
                posteRepository.deleteByIdentiteId(identite.getId());
                experienceRepository.deleteByIdentiteId(identite.getId());
                diplomeRepository.deleteByIdentiteId(identite.getId());
                langueRepository.deleteByIdentiteId(identite.getId());
                autreRepository.deleteByIdentiteId(identite.getId());

                // Supprimer l'identité
                identiteRepository.deleteById(id);

                // Construire la réponse XML avec l'en-tête
                StringWriter stringWriter = new StringWriter();
                stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                stringWriter.write("<cv id=\"" + id + "\" status=\"DELETED\"/>");
                String response = stringWriter.toString();

                logger.info("CV avec l'id : {} supprimé avec succès", id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            } else {
                logger.warn("CV avec l'id : {} non trouvé", id);
                StringWriter stringWriter = new StringWriter();
                stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                stringWriter.write("<status>Erreur</status>");
                String response = stringWriter.toString();

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                        .body(response);
            }
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la suppression du CV avec l'id : {}", id, e);
            StringWriter stringWriter = new StringWriter();
            stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            stringWriter.write("<status>Erreur : " + e.getMessage() + "</status>");
            String response = stringWriter.toString();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(response);
        }
    }
    @GetMapping(value = "/cv24/html")
    public String getCVDetailHTML(@RequestParam("id") Long id, Model model) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        Identite identite = identiteRepository.findById(id).orElse(null);
        XMLParser xp = new XMLParser();
        if (identite == null) {
            return xp.generateErrorXML("Identité non trouvée pour l'ID: " + id);
        }

        CV cv = new CV();
        cv.setIdentite(identite);
        cv.setPoste(posteRepository.findByIdentiteId(identite.getId()).orElse(null));
        cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
        cv.setDiplomes(diplomeRepository.findByIdentiteId(identite.getId()));
        cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
        cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
        cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));

        String fxml = xp.parseDataCVToXML(cv);
        String xsdFichierPath = "classpath:xml/shema.xsd";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(fxml)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream(xsdFichierPath)));
            Validator validator = schema.newValidator();;
            validator.validate(new DOMSource(document));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return xp.generateErrorXML("Erreur de validation du XML par rapport au schéma XSD: " + e.getMessage());
        }
        XMLParser xmlParser = new XMLParser();


        return xp.genereateHTMLWithXSLT(fxml);
    }



    //fonctionalite recherche

    @GetMapping(value = "/cv24/search", produces = "application/xml")
    @ResponseBody
    public String searchCVsByObjectiveAndDate(
            @RequestParam(value = "objectif", required = false) String objectif,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {

        try {
            // Vérifier si l'objectif est fourni
            if (objectif == null || objectif.isEmpty()) {
                logger.error("Objectif non fourni.");
                return "<status>ERROR</status>";
            }

            List<Identite> identites = identiteRepository.findAll();
            List<CV> cvs = new ArrayList<>();

            // Parcourir les identités pour filtrer par objectif
            for (Identite identite : identites) {
                Optional<Poste> posteOptional = posteRepository.findByIdentiteId(identite.getId());
                posteOptional.ifPresent(poste -> {
                    boolean objectifMatch = poste.getIntiltule() != null &&
                            Arrays.stream(objectif.split(" "))
                                    .anyMatch(keyword -> poste.getIntiltule().contains(keyword));

                    // Vérifier si la date d'obtention du diplôme est valide
                    List<Diplome> diplomes = diplomeRepository.findByIdentiteId(identite.getId());
                    boolean dateMatch = diplomes.stream()
                            .anyMatch(diplome -> diplome.getDateObtention() != null &&
                                    (date == null || !diplome.getDateObtention().before(date)));

                    if (objectifMatch && dateMatch) {
                        CV cv = new CV();
                        cv.setIdentite(identite);
                        cv.setPoste(poste);
                        cv.setExperiences(experienceRepository.findByIdentiteId(identite.getId()));
                        cv.setDiplomes(diplomes);
                        cv.setCertifications(certificationRepository.findByIdentiteId(identite.getId()));
                        cv.setLangues(langueRepository.findByIdentiteId(identite.getId()));
                        cv.setAutres(autreRepository.findByIdentiteId(identite.getId()));
                        cvs.add(cv);
                    }
                });
            }

            // Si aucun CV ne correspond aux critères
            if (cvs.isEmpty()) {
                logger.info("Aucun CV trouvé pour l'objectif : {}", objectif);
                return "<status>NONE</status>";
            }

            // Convertir la liste de CVs en XML
            XMLParser xp = new XMLParser();
            return xp.parseDataToXML(cvs);
        } catch (Exception e) {
            logger.error("Une erreur est survenue lors de la recherche des CVs : {}", e.getMessage());
            return "<status>ERROR</status>";
        }
    }



}