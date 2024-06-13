package dev.otthon.sistemaweb.core.exceptions;

public class PositionNotFoundException extends ModelNotFoundException {

    public PositionNotFoundException() {
        super("Cargo não encontrado");
    }

    public PositionNotFoundException(String message) {
        super(message);
    }
}
