package servent.message;

import app.ServentInfo;

import java.awt.*;
import java.io.Serial;
import java.util.List;

public class WorkMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = 8683047814407067807L;
    private final List<Point> mainPoints;

    public WorkMessage( ServentInfo senderInfo, ServentInfo receiverInfo, List<Point> mainPoints ) {
        super( MessageType.WORK, senderInfo, receiverInfo);
        this.mainPoints = mainPoints;
    }

    public List<Point> getMainPoints() {
        return mainPoints;
    }
}
