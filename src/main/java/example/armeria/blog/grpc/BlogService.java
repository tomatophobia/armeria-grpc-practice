package example.armeria.blog.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class BlogService extends BlogServiceGrpc.BlogServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();

    @Override
    public void createBlogPost(CreateBlogPostRequest request, StreamObserver<BlogPost> responseObserver) {
        final int id = idGenerator.getAndIncrement();
        final Instant now = Instant.now();
        final BlogPost updated = BlogPost.newBuilder()
                .setId(id)
                .setTitle(request.getTitle())
                .setContent(request.getContent())
                .setModifiedAt(now.toEpochMilli())
                .setCreatedAt(now.toEpochMilli())
                .build();
        blogPosts.put(id, updated);
        logger.info("Created at {} - {}", updated.getId(), updated.getTitle());
        responseObserver.onNext(updated);
        responseObserver.onCompleted();
    }
}
