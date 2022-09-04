package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class PingMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -6688214285688841977L;

    public PingMessage(ServentInfo senderInfo, ServentInfo receiverInfo) {
        super( MessageType.PING, senderInfo, receiverInfo);
    }
}
