package example.armeria.client.blog.grpc;

import com.linecorp.armeria.client.grpc.GrpcClients;
import example.armeria.blog.grpc.BlogPost;
import example.armeria.blog.grpc.BlogServiceGrpc;
import example.armeria.blog.grpc.CreateBlogPostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlogClient {
    private static final Logger logger = LoggerFactory.getLogger(BlogClient.class);
    static BlogServiceGrpc.BlogServiceBlockingStub client;

    void createBlogPost(String title, String content) {
        final CreateBlogPostRequest request = CreateBlogPostRequest.newBuilder()
                .setTitle(title)
                .setContent(content)
                .build();
        final BlogPost response = client.createBlogPost(request);
        logger.info("[Create response] Title: {} Content: {}", response.getTitle(), response.getContent());
    }

    void testRun(){
        createBlogPost("Another blog post", "Creating a post via createBlogPost().");
    }

    public static void main(String[] args) throws Exception {
        client = GrpcClients.newClient("http://127.0.0.1:8080/", BlogServiceGrpc.BlogServiceBlockingStub.class);
        CreateBlogPostRequest request = CreateBlogPostRequest.newBuilder()
                .setTitle("My first blog")
                .setContent("Yay")
                .build();
        BlogPost response = client.createBlogPost(request);

        BlogClient blogClient = new BlogClient();
        blogClient.testRun();
    }
}
