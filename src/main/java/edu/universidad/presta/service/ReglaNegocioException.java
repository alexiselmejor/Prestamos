package edu.universidad.presta.service;

/** Se incumple una regla de negocio; el mensaje se muestra tal cual al usuario. */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
