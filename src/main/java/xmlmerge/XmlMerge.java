package xmlmerge;

import java.io.FileInputStream;
import java.io.InputStream;
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
            InputStream updateDataStream = new FileInputStream(args[0] + ".merge.json");){

            ObjectMapper mapper = new ObjectMapper();
            MergeData data = mapper.readValue(updateDataStream, MergeData.class);

            Document source = PositionalXMLReader.readXML(sourceStream);
            Document target = PositionalXMLReader.readXML(targetStream);

            XPath xpath = XPathFactory.newInstance().newXPath();

            for (MergeData.CreateData cd : data.getCreateList()){
                Node sourceNode = getFirstMatch(xpath, cd.getSource(), source);
                Node targetNode = getFirstMatch(xpath, cd.getTarget(), target);

                System.out.println(
                		"CREATE_ELEM_START=`" +
                		"cat -n " + args[0] + 
                		" | sed -n -e 1," + sourceNode.getUserData(PositionalXMLReader.START_LINE_NUMBER_KEY) + "p" +
                		" | sed -n -e \"/<" + sourceNode.getNodeName() + " /=\"" +
                		" | tail -n 1`");
                System.out.println(
                		"CREATE_ELEM_END=" +
                		sourceNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY));
                System.out.println("CREATE_ELEM=`sed -n -e \"${CREATE_ELEM_START},$((CREATE_ELEM_END-1))s/$/\\\\ /g;${CREATE_ELEM_START},${CREATE_ELEM_END}p\" " + args[0] + "`");

                if(cd.isInsertBefore()){
                    System.out.println(
                    		"TARGET_ELEM_START=`" +
                    		"cat -n " + args[0] + 
                    		" | sed -n -e 1," + targetNode.getUserData(PositionalXMLReader.START_LINE_NUMBER_KEY) + "p" +
                    		" | sed -n -e \"/<" + targetNode.getNodeName() + " /=\"" +
                    		" | tail -n 1`");
                    System.out.println("echo -n $((TARGET_ELEM_START-1))i >> hoge.sed");
                    System.out.println("echo \"$CREATE_ELEM\" | sed -e \"s/ /\\\\\\\\ /g;s/\\t/\\\\\\\\ \\\\\\\\ \\\\\\\\ \\\\\\\\ /g\" >> hoge.sed");
                }else{
                	int insertPos = Integer.parseInt((String)targetNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY))-1;
                    System.out.println("echo -n " + insertPos + "i >> hoge.sed");
                    System.out.println("echo \"$CREATE_ELEM\" | sed -e \"s/ /\\\\\\\\ /g;s/\\t/\\\\\\\\ \\\\\\\\ \\\\\\\\ \\\\\\\\ /g\" >> hoge.sed");
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
