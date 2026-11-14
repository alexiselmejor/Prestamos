package edu.universidad.presta.dao;

/** Error al acceder a la base de datos (envuelve SQLException para no ensuciar el código con throws). */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(Throwable causa) {
        super(causa.getMessage(), causa);
    }
}
