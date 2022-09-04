package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class WelcomeMessage extends BasicMessage {
	@Serial
	private static final long serialVersionUID = -7945309736867059015L;

	private final String fractalID;
	
	public WelcomeMessage( ServentInfo senderInfo, ServentInfo receiverInfo, String fractalID ) {
		super( MessageType.WELCOME, senderInfo, receiverInfo );
		this.fractalID = fractalID;
	}
	public WelcomeMessage( ServentInfo senderInfo, ServentInfo receiverInfo ) {
		super( MessageType.WELCOME, senderInfo, receiverInfo );
		this.fractalID = null;
	}

	public String getFractalID() {
		return fractalID;
	}
}
