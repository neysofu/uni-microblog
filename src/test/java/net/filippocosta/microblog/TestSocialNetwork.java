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

    public static void run() {
        UnitTest.runAndPrint("TestSocialNework.testWrittenBy", TestSocialNetwork.testWrittenBy());
        UnitTest.runAndPrint("TestSocialNework.testInfluencers", TestSocialNetwork.testInfluencers());
    }

    private static SocialNetwork socialNetworkWithUsers() {
        SocialNetwork microblog = new SocialNetwork();
        microblog.register(userAlice);
        microblog.register(userBob);
        microblog.register(userCharlie);
        return microblog;
    }
}
