package cv24.cv24.controller;
import cv24.cv24.entities.*;
import cv24.cv24.repository.*;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
@Controller
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

    @GetMapping(value = "/resume",produces = "application/html")
    @ResponseBody
    public String getAllCVsForHTML(Model model) {
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

        model.addAttribute("cvs", cvs);
        return "resume";
    }

    @GetMapping(value = "/resume/xml", produces = "application/xml")
    @ResponseBody
    public String getAllCVsForXML() {
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
        XMLParser xp = new XMLParser();
        return xp.parseDataToXML(cvs);
    }

    @GetMapping(value = "/cv24/xml",produces = "application/xml")
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
            Validator validator = schema.newValidator();;
            validator.validate(new DOMSource(document));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return xp.generateErrorXML("Erreur de validation du XML par rapport au schéma XSD: " + e.getMessage());
        }
            return fxml;

    }

    @GetMapping(value = "/cv24/html")
    public String getCVDetailHTML(@RequestParam("id") Long id,Model model) throws ParserConfigurationException, IOException, SAXException, TransformerException {

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


        String xsltFilePath = "classpath:xml/parser.xslt";
        //String outputchemin = "src/main/resources/Resultatv3.html";// Chemin vers votre fichier XSLT
        //String outputchemin = "src/main/resources/Resultat.html";//
        String outputchemin = "resources/Resultat.html";

        //String outputchemin = "Resultat.html"; // Crée le fichier Resultat.html dans le répertoire src/main/resources


        System.out.println("tima");
        InputStream xsltStream = getClass().getClassLoader().getResourceAsStream(xsltFilePath);
        System.out.println("coucou");
        Source xslt = new StreamSource(xsltStream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslt);
        System.out.println("ghilas");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");


       Source xmlSource = new StreamSource(new StringReader(fxml));
        System.out.println("hiiii");
        File outputFile = new File(outputchemin);
        System.out.println("helloooo");
        OutputStream htmlStream = new FileOutputStream(outputFile);
        System.out.println("bonjouuuuur");
        Result output = new StreamResult(htmlStream);
        System.out.println("kkkkkkkkkkkk");

        // Transformation
        transformer.transform(xmlSource, output);

        // Fermeture des flux
        xsltStream.close();
        htmlStream.close();
        transformer.transform(xmlSource, output);



        return "DetailCV";



    }

   }
