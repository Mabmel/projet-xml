package cv24.cv24.controller;

import cv24.cv24.entities.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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




}
