package servent.message;

import app.ServentInfo;

import java.io.Serial;
import java.util.List;

public class ReplaceNodeMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 8489579736666661000L;

    private ServentInfo parentInfo;
    private List<ServentInfo> neighbourInfo;
    private List<ServentInfo> childrenInfo;

    public ReplaceNodeMessage( ServentInfo senderInfo, ServentInfo receiverInfo, ServentInfo parentInfo, List<ServentInfo> neighbourInfo, List<ServentInfo> childrenInfo) {
        super( MessageType.REPLACE_NODE, senderInfo, receiverInfo);
        this.parentInfo = parentInfo;
        this.neighbourInfo = neighbourInfo;
        this.childrenInfo = childrenInfo;
    }

    public ServentInfo getParentInfo() {
        return parentInfo;
    }

    public List<ServentInfo> getNeighbourInfo() {
        return neighbourInfo;
    }

    public List<ServentInfo> getChildrenInfo() {
        return childrenInfo;
    }
}
