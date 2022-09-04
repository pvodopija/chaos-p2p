package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class DhtGetResultMessage<R> extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -9044730600783796628L;

    private final R result;

    public DhtGetResultMessage( ServentInfo senderInfo, ServentInfo receiverInfo, R result )
    {
        super( MessageType.DHT_GET, senderInfo, receiverInfo );
        this.result = result;
    }

    public R getResult() {
        return result;
    }
}
