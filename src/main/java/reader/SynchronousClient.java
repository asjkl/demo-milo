package reader;

import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;

public class SynchronousClient {
    
    public static OpcUaClient connect(String url) throws UaException, InterruptedException, ExecutionException {
	OpcUaClient client = OpcUaClient.create(url,
		    endpoints ->
		        endpoints.stream()
		            .findFirst(),
		    configBuilder ->
		        configBuilder.build()
		);
	return (OpcUaClient) client.connect().get();
    }
}
