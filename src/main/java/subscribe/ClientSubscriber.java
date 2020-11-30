package subscribe;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedEventItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import reader.SynchronousClient;
import utils.Constants;

public class ClientSubscriber implements Runnable {
    
    private Boolean exit = false;
    private ManagedSubscription subscription;
    private OpcUaClient opcUaClient;
    
    public ClientSubscriber(OpcUaClient opcUaClient) throws UaException {
	this.opcUaClient=opcUaClient;
	//Creiamo la configurazione necessaria al monitoraggio
	createConfiguration(opcUaClient);
    } 
    
    private void createConfiguration(OpcUaClient opcUaClient) throws UaException {
	//Creiamo il punto di accesso per monitorare gli elementi e la creazione delle sottoscrizioni
	//il secondo parametro è opzionale ed indica quanto tempo bisogna attendere per richiedere l'aggiornamento dell'elemento
	subscription = ManagedSubscription.create(opcUaClient, 2000);
	//Aggiungiamo al gestore di sottoscrizioni l'elemento da monitorare
	ManagedDataItem dataItem = subscription.createDataItem(new NodeId(2, "HelloWorld/ScalarTypes/Int32"));
	
	//Aggiungiamo al gestore di sottoscrizioni le reazioni a determinati eventi
	subscription.addChangeListener(new ChangeListener() {
	    	//Quando l'elemento (o gli elementi) da monitorare cambiano e passa il tempo di pulling
	    	//stampiamo il valore aggiornato
    	        @Override
    	        public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
    	            System.out.println("->"+dataValues.get(0).getValue().getValue());           
    	            if(dataValues.get(0).getValue().getValue().equals(-1)) {
    	        	exit = true;
    	            }
    	        }
    	        //Reazione ad un evento (non indagato)
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
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	//Viene istanziato il thread e fatto partire
	ClientSubscriber subscriptionThread =new ClientSubscriber(opcUaClient);
	subscriptionThread.run();
	
    }

    private void delete() throws UaException {
	//Chiudiamo tutte le sottoscrizioni, altrimenti il server continuerà a restituire i cambiamenti
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
