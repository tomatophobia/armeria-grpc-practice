package example.armeria.blog;

class BlogNotFoundException extends IllegalStateException {
    BlogNotFoundException(String s) {
        super(s);
    }
}
