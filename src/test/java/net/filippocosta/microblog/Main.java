package net.filippocosta.microblog;

public class Main {
    public static void main(String []args) {
        System.out.println("-- MicroBlog: Batteria di test.");
        System.out.println("");
        TestUser.run();
        System.out.println("");
        TestPost.run();
        System.out.println("");
        TestSocialNetwork.run();
    }
}
