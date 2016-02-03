package xmlupdate;

import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlUpdate{

    public static void main(String[] args){
        try(InputStream sourceStream = new FileInputStream(args[0]);
            InputStream targetStream = new FileInputStream(args[1]);
            InputStream updateDataStream = new FileInputStream(args[0] + ".json");){

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document source = builder.parse(sourceStream);
            Document target = builder.parse(targetStream);

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList)xpath.evaluate("/beans/bean", source, XPathConstants.NODESET);
            source.setXmlStandalone(true);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource out = new DOMSource(source);
            StreamResult console = new StreamResult(System.out);
            transformer.transform(out, console);
        } catch (Exception ex){
            System.err.println(ex);
        }
    }

}
