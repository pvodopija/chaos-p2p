package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class PongMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -7697721175488705446L;

    public PongMessage( ServentInfo senderInfo, ServentInfo receiverInfo) {
        super( MessageType.PONG, senderInfo, receiverInfo);
    }
}
