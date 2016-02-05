package xmlmerge;

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
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XmlMerge{

    public static void main(String[] args){
        try(InputStream sourceStream = new FileInputStream(args[0]);
            InputStream targetStream = new FileInputStream(args[1]);
            InputStream updateDataStream = new FileInputStream(args[0] + ".json");){

            ObjectMapper mapper = new ObjectMapper();
            MergeData data = mapper.readValue(updateDataStream, MergeData.class);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document source = builder.parse(sourceStream);
            Document target = builder.parse(targetStream);

            XPath xpath = XPathFactory.newInstance().newXPath();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            for (MergeData.CreateData cd : data.getCreateList()){
                Node sourceNode = getFirstMatch(xpath, cd.getSource(), source);
                Node targetNode = getFirstMatch(xpath, cd.getTarget(), target);

                Node imported = target.importNode(sourceNode, true);
                if(cd.isInsertBefore()){
                    targetNode.getParentNode().insertBefore(imported, targetNode);
                }else{
                    targetNode.appendChild(imported);
                }
            }

            for (MergeData.UpdateData ud : data.getUpdateList()){
                Node sourceNode = getFirstMatch(xpath, ud.getPath(), source);
                Node targetNode = getFirstMatch(xpath, ud.getPath(), target);

                Node imported = target.importNode(sourceNode, false);

                if(!ud.isRecursive()){
                    NodeList targetChildren = targetNode.getChildNodes();
                    for(int i=0;i<targetChildren.getLength();i++){
                        imported.appendChild(targetChildren.item(i).cloneNode(true));
                    }
                }
                targetNode.getParentNode().replaceChild(imported, targetNode);
            }

            for (String path : data.getDeleteList()){
                Node targetNode = getFirstMatch(xpath, path, target);
                targetNode.getParentNode().removeChild(targetNode);
            }

            DOMSource out = new DOMSource(target);
            StreamResult console = new StreamResult(System.out);
            transformer.transform(out, console);

        } catch (Exception ex){
            System.err.println(ex);
        }
    }

    private static Node getFirstMatch(XPath xpath, String path, Node node) throws XPathExpressionException{
        NodeList resultList = (NodeList)xpath.evaluate(path, node, XPathConstants.NODESET);
        if(resultList == null || resultList.getLength() == 0){
            throw new RuntimeException("Node not found for the path " + path);
        }else{
            return resultList.item(0);
        }
    }

}
