package example.armeria.blog.grpc;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.grpc.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static Server newServer(int port) throws Exception {
        final GrpcService grpcService = GrpcService.builder()
                .addService(new BlogService())
                .exceptionMapping(new GrpcExceptionHandler())
                .build();
        return Server.builder()
                .http(port)
                .service(grpcService)
                .build();
    }

    public static void main(String[] args) throws Exception {
        final Server server = newServer(8080);

        server.closeOnJvmShutdown().thenRun(() -> {
            logger.info("Server has been stopped.");
        });

        server.start().join();
    }
}
