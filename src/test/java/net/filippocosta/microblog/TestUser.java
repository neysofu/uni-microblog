package net.filippocosta.microblog;

public class TestUser {

    public static boolean testOkUsernames() {
        return User.usernameIsOk("filippocosta")
            && User.usernameIsOk("MrPizer")
            && User.usernameIsOk("marko_pisa")
            && User.usernameIsOk("fili99")
            && User.usernameIsOk("_____")
            && User.usernameIsOk("1337");
    }

    public static boolean testUsernameMinLength() {
        return !User.usernameIsOk("")
            && !User.usernameIsOk("_")
            && !User.usernameIsOk("_")
            && !User.usernameIsOk("a2")
            && User.usernameIsOk("f".repeat(User.USERNAME_MIN_LENGTH));
    }

    public static boolean testUsernameMaxLength() {
        return !User.usernameIsOk("ciao".repeat(10))
            && User.usernameIsOk("h".repeat(User.USERNAME_MAX_LENGTH));
    }

    public static boolean testNullUsername() {
        try {
            User.usernameIsOk(null);
            return false;
        } catch (NullPointerException e) {
            return true;
        }
    }

    public static void main(String[] args) {
        UnitTest.runAndPrint("TestUser.testUsernameMinLength", TestUser.testUsernameMinLength());
        UnitTest.runAndPrint("TestUser.testUsernameMaxLength", TestUser.testUsernameMaxLength());
        UnitTest.runAndPrint("TestUser.testOkUsernames", TestUser.testOkUsernames());
        UnitTest.runAndPrint("TestUser.testNullUsername", TestUser.testNullUsername());
    }
}
