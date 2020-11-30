package subscribe;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedEventItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.ContentFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.SimpleAttributeOperand;

import reader.SynchronousClient;
import utils.Constants;

public class ClientSubscriber implements Runnable {
    
    private Boolean exit = false;
    private ManagedSubscription subscription;
    private OpcUaClient opcUaClient;
    
    public ClientSubscriber(OpcUaClient opcUaClient) throws UaException {
	this.opcUaClient=opcUaClient;
	createConfiguration(opcUaClient);
    } 
    
    private void createConfiguration(OpcUaClient opcUaClient) throws UaException {
	subscription = ManagedSubscription.create(opcUaClient, 2000);
	ManagedDataItem dataItem = subscription.createDataItem(new NodeId(2, "HelloWorld/ScalarTypes/Int32"));
	
	subscription.addChangeListener(new ChangeListener() {
    	        @Override
    	        public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
    	            System.out.println("->"+dataValues.get(0).getValue().getValue());           
    	            if(dataValues.get(0).getValue().getValue().equals(-1)) {
    	        	exit = true;
    	            }
    	        }
    	        
    	        @Override
    		public void onEventReceived(List<ManagedEventItem> eventItems, List<Variant[]> eventFields) {
    	            System.out.println("2");          
    		}
    	    });
	
	/*EventFilter eventFilter = new EventFilter(
	    new SimpleAttributeOperand[]{
	        new SimpleAttributeOperand(
	            Identifiers.BaseEventType,
	            new QualifiedName[]{new QualifiedName(0, "EventId")},
	            AttributeId.Value.uid(),
	            null),
	        new SimpleAttributeOperand(
	            Identifiers.BaseEventType,
	            new QualifiedName[]{new QualifiedName(0, "Time")},
	            AttributeId.Value.uid(),
	            null),
	        new SimpleAttributeOperand(
	            Identifiers.BaseEventType,
	            new QualifiedName[]{new QualifiedName(0, "Message")},
	            AttributeId.Value.uid(),
	            null)
	    },
	    new ContentFilter(null)
	);

        ManagedEventItem eventItem = subscription.createEventItem(new NodeId(2, "HelloWorld/ScalarTypes/Int32"), eventFilter);
        eventItem.addEventValueListener(new ManagedEventItem.EventValueListener() {
        @Override
        public void onEventValueReceived(ManagedEventItem item, Variant[] eventValues) {
        	System.out.println("3");          
        	
        }
        });*/
    }


    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);

	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	ClientSubscriber subscriptionThread =new ClientSubscriber(opcUaClient);
	subscriptionThread.run();
	
    }

    private void delete() throws UaException {
	subscription.delete();
	opcUaClient.disconnect();
    }

    @Override
    public void run() {
	while(!exit) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	try {
	    delete();
	} catch (UaException e) {
	    e.printStackTrace();
	}
    }
}
