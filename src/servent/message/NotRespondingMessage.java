package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class NotRespondingMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 7155010601197412229L;

    private ServentInfo notRespondingInfo;

    public NotRespondingMessage( ServentInfo senderInfo, ServentInfo receiverInfo,
                                 ServentInfo notRespondingInfo, String messageText ) {
        super( MessageType.NOT_RESPONDING, senderInfo, receiverInfo, messageText );
        this.notRespondingInfo = notRespondingInfo;
    }

    public ServentInfo getNotRespondingInfo() {
        return notRespondingInfo;
    }
}
