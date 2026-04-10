package main.web.rest.errors;

public class UtenteNonAutenticatoException extends RuntimeException {

    public UtenteNonAutenticatoException(String message) {
        super(message);
    }
}
