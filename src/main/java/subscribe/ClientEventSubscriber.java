package subscribe;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.asn1.eac.UnsignedInteger;
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
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.FilterOperator;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.structured.ContentFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.ContentFilterElement;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.ModifySubscriptionResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.SimpleAttributeOperand;

import client.SynchronousClient;
import utils.Constants;

public class ClientEventSubscriber implements Runnable {
    
    private Boolean exit = false;
    private ManagedSubscription subscription;
    private OpcUaClient opcUaClient;
    private Integer namespace =2;
    private String nodeId="HelloWorld";
    
    //Il tempo (espresso in ms) che passa prima che il subscriber richieda eventuali aggiornamenti al server
    private Integer publishingInterval= 0;
    
    //Il tempo (espresso in ms) che passa prima che il server controlli eventuali cambiamenti dell'elemento da monitorare
    private Double samplingInterval= 0.0;
    
    //La dimensione della coda rappresenta quanti cambiamenti di valore possono essere salvati prima di applicare una policy di discard
    private Integer queueSize= 5;
    
    //La policy di discard da applicare
    private Boolean discardOldest=true;
    
    //Se settato a REPORTING il server campiona il monitoredItem e invia i cambiamenti passato l'intervallo di publishing,
    //se settato a SAMPLING il server campiona il monitoredItem ma non invia i cambiamenti
    //se settato a DISABLED il server non effettua alcuna operazione
    private MonitoringMode monitoringMode=MonitoringMode.Reporting;
    
    public ClientEventSubscriber(OpcUaClient opcUaClient) throws UaException {
	this.opcUaClient=opcUaClient;
	
	//Creiamo la configurazione necessaria al monitoraggio
	createConfiguration(opcUaClient);
    } 
    
    private void createConfiguration(OpcUaClient opcUaClient) throws UaException {
	
	//Creiamo il punto di accesso per monitorare gli elementi e la creazione delle sottoscrizioni
	//il secondo parametro è opzionale ed indica quanto tempo bisogna attendere per richiedere l'aggiornamento dell'elemento
	subscription = ManagedSubscription.create(opcUaClient, publishingInterval);
	
	EventFilter eventFilter = new EventFilter(
		    new SimpleAttributeOperand[]{
		        new SimpleAttributeOperand(
		            Identifiers.BaseEventType,
		            new QualifiedName[]{new QualifiedName(0, "EventId")},
		            AttributeId.Value.uid(),
		            null),
		        new SimpleAttributeOperand(
		                    Identifiers.BaseEventType,
		                    new QualifiedName[]{new QualifiedName(0, "EventType")},
		                    AttributeId.Value.uid(),
		                    null),
		        new SimpleAttributeOperand(
		                    Identifiers.BaseEventType,
		                    new QualifiedName[]{new QualifiedName(0, "SourceName")},
		                    AttributeId.Value.uid(),
		                    null),
		        new SimpleAttributeOperand(
		                    Identifiers.BaseEventType,
		                    new QualifiedName[]{new QualifiedName(0, "Severity")},
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
		    new ContentFilter(null));
	ManagedEventItem eventItem = subscription.createEventItem(Identifiers.EventQueueOverflowEventType, eventFilter);
		
	//Aggiungiamo al gestore di sottoscrizioni le reazioni a determinati eventi
	subscription.addChangeListener(new ChangeListener() {
    	        
    	        @Override
    		public void onEventReceived(List<ManagedEventItem> eventItems, List<Variant[]> eventFields) {
    	                for(Variant[] event : eventFields) {
    	                    for(Variant v: event) {
    	                	System.out.println(v.getValue());
    	                    }
    	                System.out.println();
    	                }
    	                System.out.println("----------");
    		}
    	    });

    }


    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);
	
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	//Viene istanziato il thread e fatto partire
	ClientEventSubscriber subscriptionThread =new ClientEventSubscriber(opcUaClient);
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
