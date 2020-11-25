package net.filippocosta.microblog;

public class TestUser {

    public static void testOkUsernames() {
        assert User.usernameIsOk("filippocosta");
        assert User.usernameIsOk("filippobonchi");
        assert User.usernameIsOk("prof_merlotto_11");
        assert User.usernameIsOk("_____");
        assert User.usernameIsOk("1337");
    }

    public static void testUsernameMinLength() {
        assert !User.usernameIsOk("");
        assert !User.usernameIsOk("_");
        assert !User.usernameIsOk("_");
        assert !User.usernameIsOk("a2");
        assert User.usernameIsOk("f".repeat(User.USERNAME_MIN_LENGTH));
    }

    public static void testUsernameMaxLength() {
        assert !User.usernameIsOk("ciao".repeat(10));
        assert User.usernameIsOk("h".repeat(User.USERNAME_MAX_LENGTH));
    }

    public static void testNullUsername() {
        try {
            User.usernameIsOk(null);
            assert false;
        } catch (NullPointerException e) {
        }
    }

    public static void main(String[] args) {
        TestUser.testUsernameMinLength();
        TestUser.testUsernameMaxLength();
        TestUser.testOkUsernames();
        TestUser.testNullUsername();
    }
}
