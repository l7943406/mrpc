package cn.muchen7.utils;

/**
 * @author muchen
 */
public class MrpcException extends RuntimeException {
    private static final long serialVersionUID = -5579050345640289609L;

    public MrpcException(String message) {
        super(message);
    }

    public MrpcException(Throwable cause) {
        super(cause);
    }

    public MrpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
