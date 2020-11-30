package reader;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

import utils.Constants;

public class ClientReader {
    
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);
	
	ClientReader reader = new ClientReader();
	
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	String spazio="";
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	UaNode serverNode = addressSpace.getNode(Identifiers.RootFolder);
	
	System.out.format("%-60s %-15s %-15s %-15s %-15s%n", "NodeName", "NodeType", "NameSpaceIndex", "Value", "NodeId");
	System.out.println("-----------------------------------------------------------------------------------------------------------------");
	reader.broswe(serverNode, addressSpace, spazio);
	
	opcUaClient.disconnect();
	

    }
    
    public void broswe(UaNode nodo, AddressSpace addressSpace, String spazio) throws UaException {
	    List<UaNode> nodes = (List<UaNode>) addressSpace.browseNodes(nodo);
	    
	    for (UaNode uaNode : nodes) {
		stampa(uaNode, spazio);
		broswe(uaNode, addressSpace, spazio+" ");
	    }
    }
    
    public void stampa(UaNode node, String spazio) throws UaException {
	String nodeName = node.getBrowseName().getName().toString();
	String nodeType = node.getNodeClass().toString();
	//String nodeWritable = node.getWriteMask().toString();
	String nameSpaceIndex = node.getBrowseName().getNamespaceIndex().toString();
	String nodeId = node.getNodeId().getIdentifier().toString();
	Object value = null;
	
	if(node.getNodeClass().equals(NodeClass.Variable)) {
    	    value = ((UaVariableNode)node).readValue().getValue().getValue();
    	    System.out.format("%-60s %-15s %-15s %-15s %-15s%n", 
    		    spazio+nodeName,
    		    nodeType,
    		    /*nodeWritable,*/
    		    nameSpaceIndex, 
    		    value,
    		    nodeId);
    	    
         }else {
    	    System.out.format("%-60s %-15s %-15s %-15s %-15s%n", 
    		    spazio+nodeName,
    		    nodeType,
    		    /*nodeWritable,*/ 
    		    nameSpaceIndex,
    		    "-",
    		    nodeId);
    	}
    }
}
