package net.filippocosta.microblog;

import net.filippocosta.microblog.Post.ReplyRestriction;

public class TestPost {
    static String userAlice = "Alice";
    static String userBob = "super_bob99";
    static String userCharlie = "Charlie";

    public static boolean testDefaultReplyRestriction() {
        Post postFoo = new Post.Builder(userAlice, "Ciao").build();
        return postFoo.checkRep()
            && postFoo.getReplyRestriction() == Post.ReplyRestriction.EVERYONE;
    }

    public static boolean testSetReplyRestriction() {
        boolean success = true;
        // `null`.
        try {
            new Post.Builder(userAlice, "Ciao").setReplyRestriction(null).build();
            return false;
        } catch (NullPointerException e) {}
        // `EVERYONE`.
        Post post = new Post.Builder(userAlice, "Ciao")
            .setReplyRestriction(ReplyRestriction.EVERYONE).build();
        success = success
               && post.checkRep()
               && post.getReplyRestriction() == ReplyRestriction.EVERYONE;
        // `ONLY_AUTHOR_OR_TAGGED_USERS`.
        post = new Post.Builder(userBob, "Ciao")
            .setReplyRestriction(ReplyRestriction.ONLY_AUTHOR_OR_TAGGED_USERS).build();
        success = success
               && post.checkRep()
               && post.getReplyRestriction() == ReplyRestriction.ONLY_AUTHOR_OR_TAGGED_USERS;
        // `ONLY_AUTHOR`.
        post = new Post.Builder(userCharlie, "Ciao")
            .setReplyRestriction(ReplyRestriction.ONLY_AUTHOR).build();
        success = success
               && post.checkRep()
               && post.getReplyRestriction() == ReplyRestriction.ONLY_AUTHOR;
        return success;
    }

    public static boolean testIncrementalId() {
        Post post1 = new Post.Builder(userAlice, "Ciao").build();
        Post post2 = new Post.Builder(userAlice, "Hello").build();
        return post1.getId() < post2.getId();
    }

    public static boolean testTimestamp() {
        Post post1 = new Post.Builder(userAlice, "Ciao").build();
        Post post2 = new Post.Builder(userCharlie, "Salve a tutti!").build();
        return post1.getTimestamp().isBefore(post2.getTimestamp());
    }

    public static boolean testConstructorWithNull() {
        try {
            new Post.Builder(null, "Ciao").build();
            return false;
        } catch (NullPointerException e) {}
        try {
            new Post.Builder(userAlice, null).build();
            return false;
        } catch (NullPointerException e) {}
        try {
            new Post.Builder(null, null).build();
            return false;
        } catch (NullPointerException e) {}
        return true;
    }

    public static boolean testHashtags() {
        Post post1 = new Post.Builder(userCharlie, "Ciao #sabatomattina #nuvoloso").build();
        Post post2 = new Post.Builder(userCharlie, "Salve #PopoloDiStriscia!").build();
        return post1.checkRep()
            && post1.getHashtags().contains("sabatomattina")
            && post1.getHashtags().contains("nuvoloso")
            && post1.getHashtags().size() == 2
            && post2.getHashtags().contains("PopoloDiStriscia")
            && post2.getHashtags().size() == 1;
    }

    public static boolean testTaggedUsers() {
        Post post = new Post.Builder(userBob, "Oggi ho mangiato un gelato con @Marco!").build();
        return post.checkRep()
            && post.getTaggedUsers().contains("Marco")
            && post.getTaggedUsers().size() == 1;
    }

    public static boolean testConversation() {
        Post parent = new Post.Builder(userCharlie, "Ciao!").build();
        Post reply1 = new Post.Builder(userCharlie, "Quasi dimenticavo, buona giornata a tutti!")
            .inResponseTo(parent)
            .setReplyRestriction(ReplyRestriction.ONLY_AUTHOR)
            .build();
        boolean success = parent.checkRep()
                       && reply1.checkRep()
                       && reply1.getParent() == parent
                       && parent.getReplies().contains(reply1)
                       && parent.isControversial()
                       && parent.getAuthor() == userCharlie
                       && parent.totalReplies() == 1
                       && reply1.getReplies().size() == 0
                       && reply1.totalReplies() == 0
                       && !reply1.isControversial()
                       && parent.userCanReply(userCharlie)
                       && parent.userCanReply(userBob)
                       && reply1.userCanReply(userCharlie)
                       && !reply1.userCanReply(userBob);
        try {
            new Post.Builder(userBob, "Ciao anche a te!").inResponseTo(reply1).build();
            return false;
        } catch (IllegalArgumentException e) {}
        return success;
    }

    public static boolean testLikes() {
        Post post = new Post.Builder(userAlice, "Ciao!").build();
        boolean success = post.getLikes().size() == 0;
        try {
            post.toggleLike(null);
            return false;
        } catch (NullPointerException e) {}
        try {
            post.toggleLike(post.getAuthor());
            return false;
        } catch (IllegalArgumentException e) {}
        success = success && post.getLikes().size() == 0;
        post.toggleLike(userCharlie);
        success = success && post.getLikes().size() == 1;
        post.toggleLike(userBob);
        return success && post.getLikes().size() == 2;
    }

    public static void run() {
        UnitTest.runAndPrint("TestPost.testDefaultReplyRestriction", TestPost.testDefaultReplyRestriction());
        UnitTest.runAndPrint("TestPost.testSetReplyRestiction", TestPost.testSetReplyRestriction());
        UnitTest.runAndPrint("TestPost.testIncrementalId", TestPost.testIncrementalId());
        UnitTest.runAndPrint("TestPost.testTimestamp", TestPost.testTimestamp());
        UnitTest.runAndPrint("TestPost.testConstructorWithNull", TestPost.testConstructorWithNull());
        UnitTest.runAndPrint("TestPost.testHashtags", TestPost.testHashtags());
        UnitTest.runAndPrint("TestPost.testTaggedUsers", TestPost.testTaggedUsers());
        UnitTest.runAndPrint("TestPost.testConversation", TestPost.testConversation());
        UnitTest.runAndPrint("TestPost.testLikes", TestPost.testLikes());
    }
}
