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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XmlUpdate{

    public static void main(String[] args){
        try(InputStream sourceStream = new FileInputStream(args[0]);
            InputStream targetStream = new FileInputStream(args[1]);
            InputStream updateDataStream = new FileInputStream(args[0] + ".json");){

            ObjectMapper mapper = new ObjectMapper();
            UpdateData data = mapper.readValue(updateDataStream, UpdateData.class);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document source = builder.parse(sourceStream);
            Document target = builder.parse(targetStream);

            XPath xpath = XPathFactory.newInstance().newXPath();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            for (UpdateData.PositionData pos : data.getCreateList()){
                NodeList sourceNodes = (NodeList)xpath.evaluate(pos.getSource(), source, XPathConstants.NODESET);
                Node targetNode = ((NodeList)xpath.evaluate(pos.getTarget(), target, XPathConstants.NODESET)).item(0);
                for(int i=0; i<sourceNodes.getLength(); i++){
                    Node imported = target.importNode(sourceNodes.item(i), true);
                    targetNode.appendChild(imported);
                }
            }
            DOMSource out = new DOMSource(target);
            StreamResult console = new StreamResult(System.out);
            transformer.transform(out, console);

        } catch (Exception ex){
            System.err.println(ex);
        }
    }

}
