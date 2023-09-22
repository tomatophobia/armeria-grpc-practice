package example.armeria.echo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.armeria.client.grpc.GrpcClients;

import example.armeria.echo.EchoGrpc.EchoBlockingStub;
import example.armeria.echo.EchoGrpc.EchoStub;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

final class EchoClient {
    private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);
    private static EchoBlockingStub blockingStub;
    private static EchoStub asyncStub;

    EchoClient(int port) {
        // h1c = HTTP/1 cleartext, h2c = HTTP/2 cleartext
        blockingStub = GrpcClients.builder("h1c://127.0.0.1:" + port)
                                  .build(EchoBlockingStub.class);
        asyncStub = GrpcClients.builder("h1c://127.0.0.1:" + port)
                               .build(EchoStub.class);
    }

    void oneToOne(Message request) {
        logger.info("one-to-one request: seq={} title={} content={}", request.getSeq(), request.getTitle(),
                    request.getContent());
        final Message response;
        try {
            response = blockingStub.oneToOne(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("one-to-one response: seq={} title={} content={}", response.getSeq(), response.getTitle(),
                    response.getContent());
    }

    void oneToMany(Message request) {
        logger.info("one-to-many request: seq={} title={} content={}", request.getSeq(), request.getTitle(),
                    request.getContent());
        final Iterator<Message> responses;
        try {
            responses = blockingStub.oneToMany(request);
            while (responses.hasNext()) {
                final Message response = responses.next();
                logger.info("one-to-many response: seq={} title={} content={}", response.getSeq(),
                            response.getTitle(), response.getContent());
            }
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
        }
    }

    void ManyToOne(List<Message> requests) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final StreamObserver<Message> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Message response) {
                logger.info("many-to-one response: seq={} title={} content={}", response.getSeq(),
                            response.getTitle(), response.getContent());
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("RPC failed: {}", Status.fromThrowable(t));
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("RPC finished");
                finishLatch.countDown();
            }
        };

        final StreamObserver<Message> requestObserver = asyncStub.manyToOne(responseObserver);
        try {
            for (Message request : requests) {
                logger.info("many-to-one request: seq={} title={} content={}", request.getSeq(),
                            request.getTitle(), request.getContent());
                requestObserver.onNext(request);
                Thread.sleep(1000);
                if (finishLatch.getCount() == 0) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            logger.warn("RPC can not finish within 1 minutes");
        }
    }

    void ManyToMany(List<Message> requests) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final StreamObserver<Message> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Message response) {
                logger.info("many-to-many response: seq={} title={} content={}", response.getSeq(),
                            response.getTitle(), response.getContent());
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("RPC failed: {}", Status.fromThrowable(t));
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("RPC finished");
                finishLatch.countDown();
            }
        };

        final StreamObserver<Message> requestObserver = asyncStub.manyToMany(responseObserver);
        try {
            for (Message request : requests) {
                logger.info("many-to-many request: seq={} title={} content={}", request.getSeq(),
                            request.getTitle(), request.getContent());
                requestObserver.onNext(request);
                Thread.sleep(1000);
                if (finishLatch.getCount() == 0) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            logger.warn("RPC can not finish within 1 minutes");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int port = 8080;

        final EchoClient client = new EchoClient(port);
        List<Message> messages = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            messages.add(Message.newBuilder().setSeq(i).setTitle("title" + i).setContent("content" + i).build());
        }
        client.ManyToMany(messages);
    }

}
