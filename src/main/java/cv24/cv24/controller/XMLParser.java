package cv24.cv24.controller;

import cv24.cv24.entities.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XMLParser {

    public static CV parseXML(String xmlFilePath) {
        List<Experience> experiences = new ArrayList<>();
        List<Diplome> diplomes = new ArrayList<>();
        List<Certification> certifications = new ArrayList<>();
        List<Langue> langues = new ArrayList<>();
        List<Autre> autres = new ArrayList<>();
        Identite identite = new Identite();
        Poste poste = new Poste();
        try {
            File file = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlFilePath)));
            Element root = document.getDocumentElement();
            NodeList identiteList = root.getElementsByTagName("cv24:identite");
            System.out.println("hiiiiiiii");
            if (identiteList.getLength() > 0) {
                Element identiteElement = (Element) identiteList.item(0);

                String genre = identiteElement.getElementsByTagName("cv24:genre").item(0).getTextContent();
                String nom = identiteElement.getElementsByTagName("cv24:nom").item(0).getTextContent();
                String prenom = identiteElement.getElementsByTagName("cv24:prenom").item(0).getTextContent();
                String tel = identiteElement.getElementsByTagName("cv24:tel").item(0).getTextContent();
                String email = identiteElement.getElementsByTagName("cv24:mel").item(0).getTextContent();

                 identite = new Identite(nom, prenom, email, tel, Genre.valueOf(genre.toUpperCase()));
            }
                NodeList objectifList = root.getElementsByTagName("cv24:objectif");
            System.out.println("hiiiiiiii");
                if (objectifList.getLength() > 0) {
                    Element objectifElement = (Element) objectifList.item(0);

                    String statut = objectifElement.getAttribute("statut");
                    String intitule = objectifElement.getTextContent();

                    poste.setIntiltule(intitule);
                    poste.setStatus(TypeContart.valueOf(statut.toLowerCase()));
                }
            System.out.println("hiiiiiiii");
            NodeList detailList = root.getElementsByTagName("cv24:detail");
            for (int i = 0; i < detailList.getLength(); i++) {
                Element detailElement = (Element) detailList.item(i);
                String datedebStr = detailElement.getElementsByTagName("cv24:datedeb").item(0).getTextContent();
                String datefinStr = detailElement.getElementsByTagName("cv24:datefin").item(0).getTextContent();
                String titre = detailElement.getElementsByTagName("cv24:titre").item(0).getTextContent();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date datedeb = dateFormat.parse(datedebStr);
                Date datefin = dateFormat.parse(datefinStr);
                Experience experience = new Experience();
                experience.setDatedeb(datedeb);
                experience.setDatefin(datefin);
                experience.setTitre(titre);
                experiences.add(experience);
            }
            System.out.println("hiiiiiiii");
            NodeList diplomeList = root.getElementsByTagName("cv24:diplome");

            for (int i = 0; i < diplomeList.getLength(); i++) {
                Element diplomeElement = (Element) diplomeList.item(i);

                String intitule = diplomeElement.getAttribute("intitule");
                int niveau = Integer.parseInt(diplomeElement.getAttribute("niveau"));
                String dateStr = diplomeElement.getElementsByTagName("cv24:date").item(0).getTextContent();
                String institut = diplomeElement.getElementsByTagName("cv24:institut").item(0).getTextContent();
                String titre = diplomeElement.getElementsByTagName("cv24:titreD").item(0).getTextContent();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(dateStr);
                Diplome diplome = new Diplome();
                diplome.setTitre(intitule);
                diplome.setNiveauQualification(niveau);
                diplome.setDateObtention(date);
                diplome.setInstitut(institut);
                diplome.setTitre(titre);
                diplomes.add(diplome);
            }
            System.out.println("hiiiiiiii");
            NodeList certifList = root.getElementsByTagName("cv24:certif");
            for (int i = 0; i < certifList.getLength(); i++) {
                Element certifElement = (Element) certifList.item(i);
                String dateDebutStr = certifElement.getElementsByTagName("cv24:datedeb").item(0).getTextContent();
                String dateFinStr = certifElement.getElementsByTagName("cv24:datefin").item(0).getTextContent();
                String titre = certifElement.getElementsByTagName("cv24:titre").item(0).getTextContent();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date dateDebut = dateFormat.parse(dateDebutStr);
                Date dateFin = dateFormat.parse(dateFinStr);
                Certification certification = new Certification();
                certification.setDateDebut(dateDebut);
                certification.setDateFin(dateFin);
                certification.setTitre(titre);
                certifications.add(certification);
            }

            System.out.println("hiiiiiiii23");
            NodeList lvList = root.getElementsByTagName("cv24:lv");

            for (int i = 0; i < lvList.getLength(); i++) {
                Element lvElement = (Element) lvList.item(i);

                String cert = lvElement.getAttribute("cert");
                String lang = lvElement.getAttribute("lang");
                Langue langue = new Langue();
                langue.setNom(lang);
                langue.setCert(Cert.valueOf(cert));
                System.out.println(Cert.valueOf(cert));
                if(cert.equals("CLES")){
                    System.out.println("cles");
                    String nivsStr = lvElement.getAttribute("nivs");
                    langue.setNivs(Niveaux.valueOf(nivsStr.toUpperCase()));
                }else if(cert.equals("TOEIC")) {
                    String nivi = lvElement.getAttribute("nivi");
                    langue.setNivi(Integer.parseInt(nivi));
                }



                langues.add(langue);

            }

            NodeList autreList = root.getElementsByTagName("cv24:autre");

            for (int i = 0; i < autreList.getLength(); i++) {
                Element autreElement = (Element) autreList.item(i);

                String titre = autreElement.getAttribute("titre");
                String commentaire = autreElement.getAttribute("comment");
                Autre autre = new Autre();
                autre.setTitre(titre);
                autre.setCommentaire(commentaire);
                autres.add(autre);
            }
            System.out.println("hiiiiiiiimmmm");
            CV cv=new CV(identite,poste,experiences,diplomes,certifications,langues,autres);
            return cv;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String parseDataToXML(List<CV> cvList) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.newDocument();

                Element cv24Element = doc.createElementNS("http://univ.fr/cv24", "cv24");
                doc.appendChild(cv24Element);

                for (CV cv : cvList) {
                    Element cvElement = doc.createElement("cv");
                    cv24Element.appendChild(cvElement);

                    // Ajouter l'ID
                    Element idElement = doc.createElement("id");
                    idElement.setTextContent(String.valueOf(cv.getIdentite().getId()));
                    cvElement.appendChild(idElement);

                    // Ajouter l'identité
                    Element identiteElement = doc.createElement("identite");
                    cvElement.appendChild(identiteElement);

                    Element nomElement = doc.createElement("nom");
                    nomElement.setTextContent(cv.getIdentite().getNom());
                    identiteElement.appendChild(nomElement);

                    Element prenomElement = doc.createElement("prenom");
                    prenomElement.setTextContent(cv.getIdentite().getPrenom());
                    identiteElement.appendChild(prenomElement);

                    Element genreElement = doc.createElement("genre");
                    Identite i = cv.getIdentite();
                    genreElement.setTextContent(String.valueOf(i.getGenre()));
                    identiteElement.appendChild(genreElement);

                    Element objectifElement = doc.createElement("objectif");
                    Poste p = cv.getPoste();
                    objectifElement.setAttribute("statut", String.valueOf(p.getStatus()));
                    objectifElement.setTextContent(p.getIntiltule());
                    cvElement.appendChild(objectifElement);

                    Element diplomeElement = doc.createElement("diplome");
                    Diplome highestDiplome = cv.getDiplomes().stream()
                            .max((d1, d2) -> d1.getDateObtention().compareTo(d2.getDateObtention()))
                            .orElse(null);
                    if (highestDiplome != null) {
                        diplomeElement.setTextContent(highestDiplome.getTitre());
                    }
                    cvElement.appendChild(diplomeElement);
                }

                StringWriter writer = new StringWriter();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                return writer.toString();
            } catch (ParserConfigurationException | TransformerException e) {
                e.printStackTrace();
                return "<error>Unable to generate XML</error>";
            }
    }


    public String parseDataCVToXML(CV cv) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element cv24Element = doc.createElementNS("http://univ.fr/cv24", "cv24:cv24");
            doc.appendChild(cv24Element);
            cv24Element.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://univ.fr/cv24 schema.xsd");

            // Identité
            Element identiteElement = doc.createElement("cv24:identite");
            cv24Element.appendChild(identiteElement);

            Identite i = cv.getIdentite();
            Element genreElement = doc.createElement("cv24:genre");
            String genreValue = i.getGenre().toString().toUpperCase();
            switch (genreValue) {
                case "MME":
                    genreElement.setTextContent("Mme");
                    break;
                case "M.":
                case "MR":
                    genreElement.setTextContent("M.");
                    break;
                case "MRS":
                    genreElement.setTextContent("Mrs");
                    break;
                case "MISS":
                    genreElement.setTextContent("Miss");
                    break;
                default:
                    genreElement.setTextContent("Mme"); // default or handle the error accordingly
                    break;
            }
            identiteElement.appendChild(genreElement);

            Element nomElement = doc.createElement("cv24:nom");
            nomElement.setTextContent(i.getNom());
            identiteElement.appendChild(nomElement);

            Element prenomElement = doc.createElement("cv24:prenom");
            prenomElement.setTextContent(i.getPrenom());
            identiteElement.appendChild(prenomElement);

            Element telElement = doc.createElement("cv24:tel");
            telElement.setTextContent(i.getTel());
            identiteElement.appendChild(telElement);

            Element melElement = doc.createElement("cv24:mel");
            melElement.setTextContent(i.getEmail());
            identiteElement.appendChild(melElement);

            // Objectif
            Element objectifElement = doc.createElement("cv24:objectif");
            cv24Element.appendChild(objectifElement);

            Poste p = cv.getPoste();
            objectifElement.setAttribute("statut", String.valueOf(p.getStatus()));
            objectifElement.setTextContent(p.getIntiltule());

            // Prof
            if (cv.getExperiences() != null && !cv.getExperiences().isEmpty()) {
                Element profElement = doc.createElement("cv24:prof");
                cv24Element.appendChild(profElement);

                for (Experience experience : cv.getExperiences()) {
                    Element detailElement = doc.createElement("cv24:detail");
                    profElement.appendChild(detailElement);

                    Element datedebElement = doc.createElement("cv24:datedeb");
                    datedebElement.setTextContent(formatDate((Timestamp) experience.getDatedeb()));
                    detailElement.appendChild(datedebElement);

                    Element datefinElement = doc.createElement("cv24:datefin");
                    datefinElement.setTextContent(formatDate((Timestamp) experience.getDatefin()));
                    detailElement.appendChild(datefinElement);

                    Element titreElement = doc.createElement("cv24:titre");
                    titreElement.setTextContent(experience.getTitre());
                    detailElement.appendChild(titreElement);
                }
            }

            // Compétences
            Element competencesElement = doc.createElement("cv24:competences");
            cv24Element.appendChild(competencesElement);

            for (Diplome diplome : cv.getDiplomes()) {
                Element diplomeElement = doc.createElement("cv24:diplome");
                competencesElement.appendChild(diplomeElement);

                Element dateElement = doc.createElement("cv24:date");
                dateElement.setTextContent(formatDate((Timestamp) diplome.getDateObtention()));
                diplomeElement.appendChild(dateElement);

                Element institutElement = doc.createElement("cv24:institut");
                institutElement.setTextContent(diplome.getInstitut());
                diplomeElement.appendChild(institutElement);

                Element titreDElement = doc.createElement("cv24:titreD");
                titreDElement.setTextContent(diplome.getTitre());
                diplomeElement.appendChild(titreDElement);
                diplomeElement.setAttribute("niveau", String.valueOf(diplome.getNiveauQualification()));
                diplomeElement.setAttribute("intitule", diplome.getTitre());
            }

            for (Certification certification : cv.getCertifications()) {
                Element certifElement = doc.createElement("cv24:certif");
                competencesElement.appendChild(certifElement);

                Element datedebElement = doc.createElement("cv24:datedeb");
                datedebElement.setTextContent(formatDate((Timestamp) certification.getDateDebut()));
                certifElement.appendChild(datedebElement);

                Element datefinElement = doc.createElement("cv24:datefin");
                datefinElement.setTextContent(formatDate((Timestamp) certification.getDateFin()));
                certifElement.appendChild(datefinElement);

                Element titreElement = doc.createElement("cv24:titre");
                titreElement.setTextContent(certification.getTitre());
                certifElement.appendChild(titreElement);
            }

            // Divers
            if (cv.getLangues() != null && !cv.getLangues().isEmpty()) {
                Element diversElement = doc.createElement("cv24:divers");
                cv24Element.appendChild(diversElement);

                for (Langue langue : cv.getLangues()) {
                    Element lvElement = doc.createElement("cv24:lv");

                    String certType = langue.getCert().toString();
                    lvElement.setAttribute("lang", langue.getNom());
                    lvElement.setAttribute("cert", certType);

                    if ("MATt".equals(certType)) {
                        // No nivs or nivi
                    } else if ("CLES".equals(certType)) {
                        Niveaux nivimaitrise = langue.getNivs();
                        lvElement.setAttribute("nivs", String.valueOf(nivimaitrise));
                    } else if ("TOIC".equals(certType)) {
                        int nivi = langue.getNivi(); // Ensure nivi is a valid number
                        lvElement.setAttribute("nivi", String.valueOf(nivi));
                    }

                    diversElement.appendChild(lvElement);
                }

                for (Autre autre : cv.getAutres()) {
                    Element autreElement = doc.createElement("cv24:autre");
                    autreElement.setAttribute("titre", autre.getTitre());
                    autreElement.setAttribute("comment", autre.getCommentaire());
                    diversElement.appendChild(autreElement);
                }
            }

            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return "<error>Unable to generate XML</error>";
        }
    }

    private String formatDate(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate().toString();
    }


    public String generateErrorXML(String errorMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element errorElement = doc.createElement("error");
            doc.appendChild(errorElement);

            Element messageElement = doc.createElement("message");
            messageElement.setTextContent(errorMessage);
            errorElement.appendChild(messageElement);

            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return "<error><message>Erreur interne lors de la génération du message d'erreur XML.</message></error>";
        }

    }

    public String genereateHTMLWithXSLT(String fluxXML) {
        StringWriter htmlWriter = new StringWriter();
        try {
            String xsltchemin = "/xml/parser.xslt";

            InputStream xsltStream = getClass().getClassLoader().getResourceAsStream(xsltchemin);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(xsltStream);

            // Créer le transformateur
            Transformer transformer = transformerFactory.newTransformer(xslt);

            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            Source xml = new StreamSource(new StringReader(fluxXML));

            Result output = new StreamResult(htmlWriter);
            transformer.transform(xml, output);


            System.out.println("Le code HTML  généré avec succès : ");
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return htmlWriter.toString();
    }















}
