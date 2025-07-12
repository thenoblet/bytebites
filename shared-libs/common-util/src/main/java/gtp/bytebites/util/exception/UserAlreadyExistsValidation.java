package gtp.bytebites.util.exception;

public class UserAlreadyExistsValidation extends RuntimeException {
    public UserAlreadyExistsValidation(String email) {
        super("User with email " + email + "already exists");
    }
}
