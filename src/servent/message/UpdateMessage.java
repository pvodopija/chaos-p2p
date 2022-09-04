package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class UpdateMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -5003885810742071229L;

	public UpdateMessage(ServentInfo senderInfo, ServentInfo receiverInfo, String text ) {
		super( MessageType.UPDATE, senderInfo, receiverInfo, text );
	}
}
