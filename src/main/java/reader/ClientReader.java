package reader;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

import utils.Constants;

public class ClientReader {
    
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);
	
	ClientReader reader = new ClientReader();
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	String spazio="";
	//Ricaviamo l'address space che contiene tutti i nodi del server
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	//Dal address space prendiamo il nodo da cui vogliamo partire (per esempio nodo radice)
	UaNode serverNode = addressSpace.getNode(Identifiers.RootFolder);
	
	System.out.format("%-60s %-15s %-15s %-15s %-15s%n", "NodeName", "NodeType", "NameSpaceIndex", "Value", "NodeId");
	System.out.println("-----------------------------------------------------------------------------------------------------------------");
	//Esploriamo l'address space a partire dal nodo scelto
	reader.broswe(serverNode, addressSpace, spazio);
	
	opcUaClient.disconnect();
	

    }
    
    //Ricorsivamente si vanno ad esplorare tutti i nodi figli partendo dal nodo padre
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
	//Se il nodo è di tipo variabile possiamo recuperare il valore
	if(node.getNodeClass().equals(NodeClass.Variable)) {
	    //Lettura del valore di una variabile
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
