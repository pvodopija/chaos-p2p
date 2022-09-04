package servent.message;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import app.ServentInfo;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	@Serial
	private static final long serialVersionUID = -361764283284107849L;

	private final MessageType type;
	private final ServentInfo senderInfo;
	private final ServentInfo receiverInfo;
	private final String messageText;
	
	/* This gives us a unique id - incremented in every natural constructor. */
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;
	
	public BasicMessage( MessageType type, ServentInfo senderInfo, ServentInfo receiverInfo ) {
		this.type = type;
		this.senderInfo = senderInfo;
		this.receiverInfo = receiverInfo;
		this.messageText = "";
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType type, ServentInfo senderInfo, ServentInfo receiverInfo, String messageText) {
		this.type = type;
		this.senderInfo = senderInfo;
		this.receiverInfo = receiverInfo;
		this.messageText = messageText;
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}


	@Override
	public ServentInfo getSenderInfo() {
		return senderInfo;
	}

	public ServentInfo getReceiverInfo() {
		return receiverInfo;
	}

	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}

	@Override
	public String getJobID() {
		return (  senderInfo.getJobID() != null ) ? senderInfo.getJobID() : receiverInfo.getJobID();
	}

	/**
	 * Comparing messages is based on their unique id and the original sender port.
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj instanceof BasicMessage ) {
			BasicMessage other = (BasicMessage)obj;
			
			if( getMessageId() == other.getMessageId() &&
				getSenderInfo() == other.getSenderInfo() ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash( getMessageId(), getSenderInfo() );
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|sender_port|message_id|text|type|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[" + getSenderInfo().getId() + "|" + getMessageId() + "|" +
					getMessageText() + "|" + getMessageType() + "|" + getReceiverInfo().getId() + "]";
	}

}
