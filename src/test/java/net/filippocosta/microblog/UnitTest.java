package net.filippocosta.microblog;

public class UnitTest {
    static void runAndPrint(String testName, boolean result) {
        System.out.println(String.format("-- %s():", testName));
        if (result) {
            System.out.println("   SUCCESS");
        } else {
            System.out.println("   FAIL");
        }
    }
}
