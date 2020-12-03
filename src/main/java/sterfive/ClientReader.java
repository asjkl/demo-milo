package sterfive;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

import client.SynchronousClient;
import utils.Constants;

public class ClientReader {
    private static Integer namespace =1;
    private static String nodeId="HelloWorld";
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOSTSTERFIVE, Constants.PORTSTERFIVE, Constants.PATHSTERFIVE);
	
	ClientReader reader = new ClientReader();
	
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	String spazio="";
	
	//Ricaviamo l'address space che contiene tutti i nodi del server
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	
	//Dal address space prendiamo il nodo da cui vogliamo partire (per esempio nodo radice)
	UaNode serverNode = addressSpace.getNode(Identifiers.Server);
	
	System.out.format("%-60s %-15s %-15s %-15s %-15s %-15s%n", "NodeName", "NodeType","NodeWritable", "NameSpaceIndex", "NodeId", "Value");
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
	Boolean nodeWritable = false;
	String nameSpaceIndex = node.getBrowseName().getNamespaceIndex().toString();
	String nodeId = node.getNodeId().getIdentifier().toString();
	Object value = null;
	
	//Se il nodo è di tipo variabile possiamo recuperare il valore
	if(node.getNodeClass().equals(NodeClass.Variable)) {
	   
	    //Lettura del valore di una variabile
    	    value = ((UaVariableNode)node).readValue().getValue().getValue();
    	   
    	    //Controllo se è possibile scrivere la variabile, è possibile fare lo stesso per la lettura
    	    Set<AccessLevel> accessLevel = AccessLevel.fromValue(((UaVariableNode)node).getAccessLevel());
    	    nodeWritable=accessLevel.contains(AccessLevel.CurrentWrite);
    	   
    	    System.out.format("%-60s %-15s %-15s %-15s %-15s %-15s%n", 
    		    spazio+nodeName,
    		    nodeType,
    		    nodeWritable,
    		    nameSpaceIndex, 
    		    nodeId,
    		    value);
    	    
         }else {
    	    System.out.format("%-60s %-15s %-15s %-15s %-15s %-15s%n", 
    		    spazio+nodeName,
    		    nodeType,
    		    nodeWritable, 
    		    nameSpaceIndex,
    		    nodeId,
    		    "-");
    	}
    }
}
