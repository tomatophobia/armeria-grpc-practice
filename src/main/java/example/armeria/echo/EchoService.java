package example.armeria.echo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;

/**
 * Our implementation of RouteGuide service.
 *
 * <p>See route_guide.proto for details of the methods.
 */
final class EchoService extends EchoGrpc.EchoImplBase {
    private final Logger logger = LoggerFactory.getLogger(EchoService.class);

    @Override
    public void oneToOne(Message request, StreamObserver<Message> responseObserver) {
        responseObserver.onNext(request);
        responseObserver.onCompleted();
    }

    @Override
    public void oneToMany(Message request, StreamObserver<Message> responseObserver) {
        final int repeated = 3;
        for (int i = 0; i < repeated; i++) {
            responseObserver.onNext(request);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Message> manyToOne(StreamObserver<Message> responseObserver) {
        return new StreamObserver<>() {
            private final List<Message> messages = new ArrayList<>();

            @Override
            public void onNext(Message request) {
                messages.add(request);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("RPC cancelled");
            }

            @Override
            public void onCompleted() {
                final StringBuilder titleBuilder = new StringBuilder();
                final StringBuilder contentBuilder = new StringBuilder();
                for (Message message : messages) {
                    titleBuilder.append(message.getTitle());
                    contentBuilder.append(message.getContent());
                }
                final Message big = Message.newBuilder().setSeq(0).setTitle(titleBuilder.toString()).setContent(
                        contentBuilder.toString()).build();
                responseObserver.onNext(big);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Message> manyToMany(StreamObserver<Message> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Message request) {
                responseObserver.onNext(request);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("RPC cancelled");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
