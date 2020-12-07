package external.server.opcuaserver;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.asn1.eac.UnsignedInteger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedEventItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.structured.ModifySubscriptionResponse;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import client.SynchronousClient;
import utils.Constants;

public class Subscriber implements Runnable {
    
    private Boolean exit = false;
    private ManagedSubscription subscription;
    private OpcUaClient opcUaClient;
    private static Integer namespace =3;
    private static String nodeId = "AirConditioner_1.Temperature";
 
    //Il tempo (espresso in ms) che passa prima che il subscriber richieda eventuali aggiornamenti al server
    private Integer publishingInterval= 2000;
    
    //Il tempo (espresso in ms) che passa prima che il server controlli eventuali cambiamenti dell'elemento da monitorare
    private Double samplingInterval= 1000.0;
    
    //La dimensione della coda rappresenta quanti cambiamenti di valore possono essere salvati prima di applicare una policy di discard
    private Integer queueSize= 5;
    
    //La policy di discard da applicare
    private Boolean discardOldest=true;
    
    //Se settato a REPORTING il server campiona il monitoredItem e invia i cambiamenti passato l'intervallo di publishing,
    //se settato a SAMPLING il server campiona il monitoredItem ma non invia i cambiamenti
    //se settato a DISABLED il server non effettua alcuna operazione
    private MonitoringMode monitoringMode=MonitoringMode.Reporting;
    
    public Subscriber(OpcUaClient opcUaClient) throws UaException {
	this.opcUaClient=opcUaClient;
	
	//Creiamo la configurazione necessaria al monitoraggio
	createConfiguration(opcUaClient);
    } 
    
    private void createConfiguration(OpcUaClient opcUaClient) throws UaException {
	
	//Creiamo il punto di accesso per monitorare gli elementi e la creazione delle sottoscrizioni
	//il secondo parametro è opzionale ed indica quanto tempo bisogna attendere per richiedere l'aggiornamento dell'elemento
	subscription = ManagedSubscription.create(opcUaClient, publishingInterval);
	
	//Aggiungiamo al gestore di sottoscrizioni l'elemento da monitorare
	ManagedDataItem dataItem = subscription.createDataItem(new NodeId(namespace,nodeId ));
	dataItem.setSamplingInterval(samplingInterval);
	dataItem.setQueueSize(UInteger.valueOf(queueSize));
	dataItem.setDiscardOldest(discardOldest);
	dataItem.setMonitoringMode(monitoringMode);
	
	//ManagedSubscription.StatusListener
	//subscription.addStatusListener();     // StatusListener statusListener)
	
	//Aggiungiamo al gestore di sottoscrizioni le reazioni a determinati eventi
	subscription.addChangeListener(new ChangeListener() {
	    	
	    	//Quando l'elemento (o gli elementi) da monitorare cambiano e passa il tempo di pulling
	    	//stampiamo il valore aggiornato
    	        @Override
    	        public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
    	            System.out.print("Dato ricevuto: ");
    	            for (DataValue dataValue : dataValues) {
    	        	System.out.print(dataValue.getValue().getValue()+" ");           
		    }
    	            System.out.println();
    	        }
    	        
    	    });
    }


    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
	final String endpoint = String.format("opc.tcp://%s:%s%s", "opcuaserver.com", 48010, "/test");
	
	//Creiamo una connessione sincrona con il server OPC
	OpcUaClient opcUaClient = SynchronousClient.connect(endpoint);
	
	//Viene istanziato il thread e fatto partire
	Subscriber subscriptionThread =new Subscriber(opcUaClient);
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
