package br.com.wedocode.shopping.presentation.shared.business.exception;

public class DAOException extends Exception {

    private static final long serialVersionUID = 1L;

    public static DAOException wrap(String message, Exception e) {
        if (e instanceof DAOException) {
            return (DAOException) e;
        }
        var exn = new DAOException(message);
        exn.addSuppressed(e);
        return exn;
    }

    public DAOException() {
        super();
    }

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }

}
