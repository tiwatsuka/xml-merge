package xmlmerge;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;

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
            InputStream updateDataStream = new FileInputStream(args[0] + ".merge.json");
        	Writer writer = new FileWriter(args[0] + ".merge.sh");){
        	
            ObjectMapper mapper = new ObjectMapper();
            MergeData data = mapper.readValue(updateDataStream, MergeData.class);

            Document source = PositionalXMLReader.readXML(sourceStream);
            Document target = PositionalXMLReader.readXML(targetStream);
            source.normalize();
            target.normalize();
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            StringBuilder sb = new StringBuilder();
            String sedFile = args[0] + ".merge.sed";
            
            sb.append("#!/bin/bash\n");
            sb.append("rm -f ").append(sedFile).append("\n");
            
            for (MergeData.CreateData cd : data.getCreateList()){
                Node sourceNode = getFirstMatch(xpath, cd.getSource(), source);
                Node targetNode = getFirstMatch(xpath, cd.getTarget(), target);

                addStartTagBeginQuery(sb, "CREATE_ELEM_START", args[0], sourceNode);
                sb.append("CREATE_ELEM_END=")
                	.append(sourceNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY))
                	.append("\n");
                sb.append("CREATE_ELEM=$(")
                	.append("sed -n -e \"")
                	.append("s/$/\\\\\\\\/g;${CREATE_ELEM_START},${CREATE_ELEM_END}p\" ")
                	.append(args[0])
                	.append(" | sed -e \"\\$s/.$//g\")\n");
                
                if(cd.isInsertBefore()){
                	addStartTagBeginQuery(sb, "TARGET_ELEM_START", args[1], targetNode);
                	sb.append("echo $((TARGET_ELEM_START-1))i\\\\ >> ").append(sedFile).append("\n")
                	.append("echo \"$CREATE_ELEM\"\\\\ >> ").append(sedFile).append("\n")
                	.append("echo >> ").append(sedFile).append("\n");
                }else{
                	sb.append("echo ")
                		.append((String)targetNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY))
                		.append("i\\\\ >> ").append(sedFile).append("\n")
                		.append("echo \\\\ >> ").append(sedFile).append("\n")
                		.append("echo \"$CREATE_ELEM\" >> ").append(sedFile).append("\n");
                }
            }

            for (MergeData.UpdateData ud : data.getUpdateList()){
                Node sourceNode = getFirstMatch(xpath, ud.getPath(), source);
                Node targetNode = getFirstMatch(xpath, ud.getPath(), target);

                addStartTagBeginQuery(sb,"CREATE_ELEM_START", args[0], sourceNode);
                addStartTagBeginQuery(sb,"DELETE_ELEM_START", args[1], targetNode);

                if(ud.isRecursive()){
                	sb.append("CREATE_ELEM_END=").append(sourceNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY)).append("\n");
                	sb.append("DELETE_ELEM_END=").append(targetNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY)).append("\n");
                }else{
                	sb.append("CREATE_ELEM_END=").append(sourceNode.getUserData(PositionalXMLReader.START_LINE_NUMBER_KEY)).append("\n");
                	sb.append("DELETE_ELEM_END=").append(targetNode.getUserData(PositionalXMLReader.START_LINE_NUMBER_KEY)).append("\n");
                }
                sb.append("CREATE_ELEM=$(")
                	.append("sed -n -e \"")
                	.append("s/$/\\\\\\\\/g;${CREATE_ELEM_START},${CREATE_ELEM_END}p\" ")
                	.append(args[0])
                	.append(" | sed -e \"\\$s/.$//g\")\n");

                sb.append("echo ${DELETE_ELEM_START}i\\\\ >> ").append(sedFile).append("\n")
                		.append("echo \"$CREATE_ELEM\" >> ").append(sedFile).append("\n");
                sb.append("echo ${DELETE_ELEM_START},${DELETE_ELEM_END}d >> ").append(sedFile).append("\n");
            }

            for (String path : data.getDeleteList()){
                Node targetNode = getFirstMatch(xpath, path, target);
                addStartTagBeginQuery(sb, "DELETE_ELEM_START", args[1], targetNode);
                sb.append("DELETE_ELEM_END=").append(targetNode.getUserData(PositionalXMLReader.END_LINE_NUMBER_KEY)).append("\n");
                sb.append("echo ${DELETE_ELEM_START},${DELETE_ELEM_END}d >> ").append(sedFile).append("\n");
            }

            sb.append("if test -e ").append(sedFile).append("; then\n")
            	.append("sed -i -f ").append(sedFile).append(" ").append(args[1]).append("\n")
            	.append("fi\n");
            writer.append(sb);
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

    private static void addStartTagBeginQuery(StringBuilder sb, String variable, String filename, Node node){
    	sb.append(variable).append("=$(")
    		.append("cat -n ").append(filename)
    		.append(" | sed -n -e 1,").append(node.getUserData(PositionalXMLReader.START_LINE_NUMBER_KEY)).append("p")
    		.append(" | sed -n -e \"/<").append(node.getNodeName()).append("\\( \\|\\n\\|\\/\\|>\\)/=\"")
    		.append(" | tail -n 1)\n");
    }
}
