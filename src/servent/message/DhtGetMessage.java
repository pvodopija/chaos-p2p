package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class DhtGetMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -599137394699081347L;

	private final String key;

	public DhtGetMessage( ServentInfo senderInfo, ServentInfo receiverInfo, String key ) {
		super( MessageType.DHT_GET, senderInfo, receiverInfo );
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
