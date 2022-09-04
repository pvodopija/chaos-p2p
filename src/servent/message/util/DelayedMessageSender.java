package servent.message.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.CompletionException;

import app.AppConfig;
import app.chaos.job.ChaosJob;
import servent.message.Message;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 * 
 * @author bmilojkovic
 *
 */
public class DelayedMessageSender implements Runnable {

	private Message messageToSend;
	
	public DelayedMessageSender( Message messageToSend ) {
		this.messageToSend = messageToSend;
	}
	
	public void run() {
		/*
		 * A random sleep before sending.
		 * It is important to take regular naps for health reasons.
		 */
		try {
			Thread.sleep( ( long ) ( Math.random() * 1000 ) + 500 );
		} catch ( InterruptedException e1 ) {
			e1.printStackTrace();
		}
		
		if ( MessageUtil.MESSAGE_UTIL_PRINTING )
		{
			AppConfig.timestampedStandardPrint(
					"( " + messageToSend.getJobID().substring( 0, 6 ) + " ) Sending message " + messageToSend
			);
		}
		
		try {
			Socket sendSocket = new Socket(
					messageToSend.getReceiverInfo().getIpAddress(),
					messageToSend.getReceiverInfo().getListenerPort()
			);

			ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(messageToSend);
			oos.flush();

			sendSocket.close();
		}
//		catch( ConnectException e ) {
//			if( messageToSend.getSenderInfo().getId() != -1 )
//			{
//				ChaosJob chaosJob = ( ChaosJob ) AppConfig.jobMap.get( messageToSend.getJobID() );
//				chaosJob.getFailureDetectionWorker().notResponding( messageToSend.getReceiverInfo() );
//			}
//		}
		catch ( IOException e ) {
			AppConfig.timestampedErrorPrint( "Couldn't send message: " + messageToSend.toString() );
			e.printStackTrace();
		}
	}
	
}
