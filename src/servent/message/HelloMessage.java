package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class HelloMessage extends BasicMessage {


    @Serial
    private static final long serialVersionUID = 2089052012054818030L;

    public HelloMessage( ServentInfo senderInfo, ServentInfo receiverInfo ) {
        super( MessageType.HELLO, senderInfo, receiverInfo );
    }

}
