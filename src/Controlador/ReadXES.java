package Controlador;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadXES {

    public static String XESTOTXT(String path, String newFileName) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Preparación de xpath
        XPath xpath = XPathFactory.newInstance().newXPath();
        //archivo de salida
        String newPath = "";
        if(path.contains("/")){
            newPath = path.substring(0, path.lastIndexOf("/")+1) + newFileName + ".txt";
        }else if(path.contains( ((char) 92) + "" )){
             newPath = path.substring(0, path.lastIndexOf( ((char)92) +"" )+1) + newFileName + ".txt";
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(newPath));
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);
            String sCurrentLine;
            int bandera = 0;
            int bandera2 = 0;
            String asd = "";
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains("<trace>")) {
                    bandera = 1;
                } else if (sCurrentLine.contains("<event>")) {
                    asd = "";
                    bandera2 = 1;
                    asd += "\n" + sCurrentLine + "\n";
                } else if (sCurrentLine.contains("</event>")) {
                    bandera2 = 0;
                    asd += "\n" + sCurrentLine + "\n";
                    Document docc = builder.parse(new ByteArrayInputStream(asd.getBytes()));//"D:\\jacie\\Documents\\Doctorado UAT\\Dataset proceso inter-organizacionales\\e.xml");
                    NodeList nodoss1 = (NodeList) xpath.evaluate("//string", docc, XPathConstants.NODESET);
                    for (int i = 0; i < nodoss1.getLength(); i++) {
                        Node asd1 = nodoss1.item(i);
                        Element esl = (Element) asd1;
                        //System.out.println(esl.getAttribute("key") + " " + esl.getAttribute("value"));
                        if (esl.getAttribute("key").equals("concept:name")) {
                            writer.write(esl.getAttribute("value") + "; ");
                        }
                    }
                } else if (sCurrentLine.contains("</trace>")) {
                    writer.write("\n\n");
                    bandera = 0;
                } else {
                    if (bandera == 1 && bandera2 == 1) {
                        asd += sCurrentLine;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        writer.close();
        return newPath;
    }
}
