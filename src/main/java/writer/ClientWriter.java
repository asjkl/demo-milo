package writer;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import client.SynchronousClient;
import utils.Constants;

public class ClientWriter {
    
    private static Integer namespace =2;
    private static String nodeId="HelloWorld/ScalarTypes/Int32";
    
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException  {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);
	
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	//Ricaviamo l'address space che contiene tutti i nodi del server
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	
	//Prendiamo il nodo variabile attraverso l'utilizzo di namespace e id
	UaVariableNode testNode = (UaVariableNode) addressSpace.getNode(
		    new NodeId(namespace, nodeId)
		);
	
	//Stampiamo il valore del nodo prima della scrittura
	System.out.println(testNode.readValue().getValue().getValue());
	
	//Scriviamo il valore del nodo variabile (in caso di errori viene sollevata un'eccezione)
	testNode.writeValue(new Variant(15));
	
	//Stampiamo il valore del nodo dopo la scrittura
	System.out.println(testNode.readValue().getValue().getValue());
	
	opcUaClient.disconnect();
    }

}
