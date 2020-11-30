package net.filippocosta.microblog;

import java.util.Arrays;

public class TestSocialNetwork {
    static String userAlice = "Alice";
    static String userBob = "super_bob99";
    static String userCharlie = "Charlie";
    
    public static boolean testWrittenBy() {
        SocialNetwork microblog = socialNetworkWithUsers();
        microblog.writePost(new Post.Builder(userAlice, "Salve #PopoloDiStriscia"));
        microblog.writePost(new Post.Builder(userBob, "Sto guardando Striscia La Notizia!"));
        microblog.writePost(new Post.Builder(userBob, "Ora invece comincia Harry Potter :)"));
        Post post = microblog.writePost(new Post.Builder(userBob, "Buonanotte a tutti!"));
        return microblog.checkRep()
            && microblog.writtenBy(userAlice).size() == 1
            && microblog.writtenBy(userBob).size() == 3
            && microblog.writtenBy(userCharlie).size() == 0
            && microblog.getPosts().contains(post)
            && microblog.getPosts().size() == 3
            && microblog.containing(Arrays.asList("Striscia", "Notizia", "Harry")).size() == 3;
    }

    public static boolean testInfluencers() {
        SocialNetwork microblog = socialNetworkWithUsers();
        microblog.writePost(new Post.Builder(userAlice, "Salve #PopoloDiStriscia"));
        return true;
    }

    public static boolean testGetters() {
        SocialNetwork microblog = socialNetworkWithUsers();
        Post p1 = microblog.writePost(new Post.Builder(userAlice, "Salve #PopoloDiStriscia"));
        Post p2 = microblog.writePost(new Post.Builder(userAlice, "Buonasera!"));
        boolean success = microblog.getPosts().size() == 2
                       && microblog.getUsers().size() == 3
                       && microblog.getFollowers().size() == 3
                       && microblog.getFollowers().get(userAlice).size() == 0
                       && microblog.getFollowers().get(userBob).size() == 0;
        microblog.like(p2, userBob);
        success = success
               && microblog.getFollowees().get(userBob).size() == 0
               && microblog.getFollowers().get(userAlice).size() == 0;
        microblog.like(p1, userBob);
        success = success
               && microblog.getFollowees().get(userAlice).size() == 0
               && microblog.getFollowers().get(userAlice).size() == 1
               && microblog.getFollowees().get(userBob).size() == 1
               && microblog.getFollowees().get(userBob).contains(userAlice)
               && microblog.getFollowers().get(userBob).size() == 1;
        microblog.dislike(p2, userBob);
        success = success
               && microblog.getFollowers().get(userAlice).size() == 1;
        microblog.dislike(p1, userBob);
        success = success
               && microblog.getFollowers().get(userAlice).size() == 0
               && microblog.getFollowees().get(userBob).size() == 0
               && microblog.getFollowers().get(userBob).size() == 0;
        return success;
    }

    public static boolean testGuessFollowers() {
        SocialNetwork microblog = socialNetworkWithUsers();
        Post p1 = microblog.writePost(new Post.Builder(userAlice, "Ciao"));
        Post p2 = microblog.writePost(new Post.Builder(userAlice, "Buonasera"));
        Post p3 = microblog.writePost(new Post.Builder(userCharlie, "Ciao Alice, come stai?").inResponseTo(p1));
        microblog.like(p1, userCharlie);
        microblog.like(p2, userCharlie);
        microblog.dislike(p2, userCharlie);
        microblog.like(p2, userCharlie);
        microblog.like(p3, userBob);
        microblog.like(p3, userAlice);
        return SocialNetwork.guessFollowers(microblog.getPosts()) == microblog.getFollowers();
    }

    public static void run() {
        UnitTest.runAndPrint("TestSocialNework.testWrittenBy", TestSocialNetwork.testWrittenBy());
        UnitTest.runAndPrint("TestSocialNework.testInfluencers", TestSocialNetwork.testInfluencers());
        UnitTest.runAndPrint("TestSocialNework.testGetters", TestSocialNetwork.testGetters());
        UnitTest.runAndPrint("TestSocialNework.testGuessFollowers", TestSocialNetwork.testGuessFollowers());
    }

    private static SocialNetwork socialNetworkWithUsers() {
        SocialNetwork microblog = new SocialNetwork();
        microblog.register(userAlice);
        microblog.register(userBob);
        microblog.register(userCharlie);
        return microblog;
    }
}
