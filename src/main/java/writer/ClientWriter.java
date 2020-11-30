package writer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import com.google.common.collect.ImmutableList;

import reader.ClientReader;
import reader.SynchronousClient;
import utils.Constants;

public class ClientWriter {

    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException  {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);

	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	AddressSpace addressSpace = opcUaClient.getAddressSpace();
	
	UaVariableNode testNode = (UaVariableNode) addressSpace.getNode(
		    new NodeId(2, "HelloWorld/ScalarTypes/Int32")
		);
	
	System.out.println(testNode.readValue().getValue().getValue());
	
	// Write the Value attribute; throws UaException if the write fails
	testNode.writeValue(new Variant(-1));
	
	System.out.println(testNode.readValue().getValue().getValue());
	
	opcUaClient.disconnect();
    }

}
