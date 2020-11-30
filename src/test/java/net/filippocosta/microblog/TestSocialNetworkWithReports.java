package net.filippocosta.microblog;

public class TestSocialNetworkWithReports {
    static String userAlice = "Alice";
    static String userBob = "super_bob99";
    static String userCharlie = "Charlie";
   
    public static boolean testBlacklist() {
        SocialNetworkWithReports microblog = socialNetworkWithUsers();
        Post p1 = microblog.writePost(new Post.Builder(userAlice, "Ciao"));
        Post p2 = microblog.writePost(new Post.Builder(userAlice, "Buonasera"));
        // I like non dovrebbero influire sulle segnalazioni.
        microblog.like(p1, userCharlie);
        microblog.like(p2, userCharlie);
        try {
            microblog.report(p1, userAlice);
            // Un autore non può segnalare il proprio post.
            return false;
        } catch (PostReportException e) {}
        try {
            microblog.report(p1, userBob);
            microblog.report(p1, userCharlie);
        } catch (PostReportException e) {
            return false;
        }
        return microblog.blacklist().size() == 1
            && microblog.postIsBlacklisted(p1);
    } 

    public static boolean testAuthorReportFails() {
        SocialNetworkWithReports microblog = socialNetworkWithUsers();
        Post p1 = microblog.writePost(new Post.Builder(userAlice, "Ciao"));
        try {
            microblog.report(p1, userAlice);
            // Un autore non può segnalare il proprio post.
            return false;
        } catch (PostReportException e) {}
        return true;
    }



    public static void run() {
        UnitTest.runAndPrint("TestSocialNeworkWithReports.testBlacklist",
                             TestSocialNetworkWithReports.testBlacklist());
        UnitTest.runAndPrint("TestSocialNeworkWithReports.testAuthorReportFails",
                             TestSocialNetworkWithReports.testAuthorReportFails());
    }

    private static SocialNetworkWithReports socialNetworkWithUsers() {
        SocialNetworkWithReports microblog = new SocialNetworkWithReports();
        microblog.register(userAlice);
        microblog.register(userBob);
        microblog.register(userCharlie);
        return microblog;
    }
}
