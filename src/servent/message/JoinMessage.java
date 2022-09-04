package servent.message;

import app.ServentInfo;

import java.io.Serial;
import java.util.List;

public class JoinMessage extends BasicMessage {
    @Serial
    private static final long serialVersionUID = 1118659539764371090L;

    private final String fractalID;

    /* This list is usually 1 in length because it only says hello to its parent. Unless its one of the first K nodes. */
    private final List<ServentInfo> sayHelloInfoList;

    /* This one node sends to bootstrap to join the network. */
    public JoinMessage( ServentInfo senderInfo, ServentInfo receiverInfo, String text ) {
        super( MessageType.JOIN, senderInfo, receiverInfo, text );
        fractalID = null;
        sayHelloInfoList = null;
    }

    /* This one bootstrap sends to the new node so it can say hello to its new neighbours. */
    public JoinMessage( ServentInfo senderInfo, ServentInfo receiverInfo, List<ServentInfo> sayHelloInfoList, String fractalID ) {
        super( MessageType.JOIN, senderInfo, receiverInfo );
        this.fractalID = fractalID;
        this.sayHelloInfoList = sayHelloInfoList;
    }

    public String getFractalID() {
        return fractalID;
    }

    public List<ServentInfo> getSayHelloInfoList() {
        return sayHelloInfoList;
    }

}
