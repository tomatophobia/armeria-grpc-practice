package example.armeria.echo;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.grpc.GrpcService;

final class EchoServer {
    private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);

    private static Server newServer(int port) throws Exception {
        final GrpcService grpcService = GrpcService.builder()
                                                   .addService(new EchoService())
                                                   .enableUnframedRequests(true)
                                                   .useBlockingTaskExecutor(true)
                                                   .build();
        return Server.builder()
                     .http(port)
                     .service(grpcService)
                     .requestTimeout(Duration.ofMinutes(5))
                     .build();
    }

    public static void main(String[] args) throws Exception {
        final Server server = newServer(8080);

        server.closeOnJvmShutdown().thenRun(() -> logger.info("Server has been stopped."));

        server.start().join();
    }
}
