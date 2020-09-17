package ee.sk.mid.exception;

public class MidServiceUnavailableException extends MidInternalErrorException {
    public MidServiceUnavailableException(String message) {
        super(message);
    }

}
