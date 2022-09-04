package servent.message;

import app.ServentInfo;
import app.chaos.dht.DhtResultGroup;

import java.io.Serial;

public class ResultMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -8522794004563545208L;

    private final String collectorFractalID;
    private final DhtResultGroup resultGroup;

    public ResultMessage( ServentInfo senderInfo, ServentInfo receiverInfo, String collectorFractalID, DhtResultGroup resultGroup ) {
        super( MessageType.RESULT, senderInfo, receiverInfo );
        this.collectorFractalID = collectorFractalID;
        this.resultGroup = resultGroup;
    }

    public String getCollectorFractalID() {
        return collectorFractalID;
    }

    public DhtResultGroup getResultGroup() {
        return resultGroup;
    }
}
