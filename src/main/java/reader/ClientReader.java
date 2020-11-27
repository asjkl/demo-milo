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
	
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	String spazio=" ";
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	UaNode serverNode = addressSpace.getNode(Identifiers.Server);
	broswe(serverNode, addressSpace, spazio);
	

    }
    
    public static void broswe(UaNode nodo, AddressSpace addressSpace, String spazio) throws UaException {
	    List<UaNode> nodes = (List<UaNode>) addressSpace.browseNodes(nodo);
	    
	    for (UaNode uaNode : nodes) {
		stampa(uaNode, spazio);
		broswe(uaNode, addressSpace, spazio+" ");
	    }
    }
    
    public static void stampa(UaNode node, String spazio) throws UaException {
	if(node.getNodeClass().equals(NodeClass.Variable)) {
	    System.out.format("%-60s %-15s %s %s%n", spazio+node.getBrowseName().getName(),node.getNodeClass().toString(), node.getNodeId().getIdentifier().toString(),((UaVariableNode)node).readValue().getValue().getValue());
	    
	}else {
	    System.out.format("%-60s %-15s %s%n", spazio+node.getBrowseName().getName(),node.getNodeClass().toString(), node.getNodeId().getIdentifier().toString()," "," ");
	}
    }
}
