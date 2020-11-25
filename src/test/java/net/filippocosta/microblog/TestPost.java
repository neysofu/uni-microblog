package net.filippocosta.microblog;

public class TestPost {
    public boolean checkRep(Post post) {
        return User.usernameIsOk(post.author) && post.text.length() <= Post.MAX_LENGTH
                && !post.likes.contains(post.author);
    }

    public static void main(String[] args) {
    }
}
