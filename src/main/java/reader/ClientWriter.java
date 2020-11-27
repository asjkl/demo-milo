package reader;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import utils.Constants;

public class ClientWriter {

    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException  {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);	
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	UaVariableNode testNode = (UaVariableNode) addressSpace.getNode(
		    new NodeId(1, "VendorServerInfo/OsName")
		);

		// Write the Value attribute; throws UaException if the write fails
		testNode.writeValue(new Variant("Windows 10"));
	
	opcUaClient.disconnect();
    }

}
