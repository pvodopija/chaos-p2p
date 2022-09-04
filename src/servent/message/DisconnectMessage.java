package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class DisconnectMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -8182195704707045272L;

    public DisconnectMessage( ServentInfo senderInfo, ServentInfo receiverInfo) {
        super( MessageType.DISCONNECT, senderInfo, receiverInfo );
    }
}
