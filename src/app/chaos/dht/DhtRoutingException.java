package app.chaos.dht;

public class DhtRoutingException extends Exception {

    public DhtRoutingException() {
        super( "Error in the routing algorithm, all options are exhausted and parent is null.");
    }

    public DhtRoutingException(String message ) {
        super( message );
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
