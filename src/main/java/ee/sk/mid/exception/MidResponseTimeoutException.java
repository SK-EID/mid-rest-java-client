package ee.sk.mid.exception;

public class MidResponseTimeoutException extends MidException{

    public MidResponseTimeoutException() {
        super("MID response time exceeded maximum waiting time");
    }
}
