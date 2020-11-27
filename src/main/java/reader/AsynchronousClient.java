package reader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import utils.Constants;


public class AsynchronousClient {
    
    
    private static OpcUaClientConfig buildConfiguration(final List<EndpointDescription> endpoints) {
        final OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(endpoints.get(0));
        return cfg.build();
    }
    
    public static CompletableFuture<OpcUaClient> createClient() {
        final String endpoint = String.format("opc.tcp://%s:%s%s", Constants.HOST, Constants.PORT, Constants.PATH);

        return DiscoveryClient
                .getEndpoints(endpoint) // look up endpoints from remote
                .thenCompose(endpoints -> {
                    try {
                        return CompletableFuture.completedFuture(OpcUaClient.create(buildConfiguration(endpoints)));
                    } catch (final UaException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
    }
    
    public static CompletableFuture<OpcUaClient> connect() {
        return createClient()
                .thenCompose(OpcUaClient::connect) // trigger connect
                .thenApply(OpcUaClient.class::cast); // cast result of connect from UaClient to OpcUaClient
    }
    
    public static void main(String[] args) throws InterruptedException, UaException {
      final Semaphore s = new Semaphore(0);
      connect()
              .whenComplete((client, e) -> {
                  // called when the connect operation finished ... either way

                  if (e == null) {
                      System.out.println("Connected");
                  } else {
                      System.err.println("Failed to connect");
                      e.printStackTrace();
                  }
              })
              .thenCompose(OpcUaClient::disconnect)
              .thenRun(s::release); // wake up s.acquire() below

      System.out.println("Wait for completion");
      s.acquire(); // what could could wrong?
      System.out.println("Bye bye");

    }
}
