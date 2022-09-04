package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import app.chaos.job.Job;
import servent.handler.*;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;


	public SimpleServentListener() { }

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket( AppConfig.myServentInfo.getListenerPort(), 100 );

			/* If there is no connection after 1s, wake up and see if we should terminate. */
			listenerSocket.setSoTimeout( 1000 );
		} catch( IOException e ) {
			AppConfig.timestampedErrorPrint( "Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort() );
			System.exit( 0 );
		}
		
		
		while( working )
		{
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				/* GOT A MESSAGE! <3 */
				clientMessage = MessageUtil.readMessage( clientSocket );

				/* Each handler should work with appropriate network topology. */
				Job job = AppConfig.jobMap.get( clientMessage.getJobID() );
				
				MessageHandler messageHandler = new NullHandler( clientMessage );
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch( clientMessage.getMessageType() )
				{
					case JOIN:
						messageHandler = new JoinHandler( clientMessage, job );
						break;
					case HELLO:
						messageHandler = new HelloHandler( clientMessage, job );
						break;
					case WELCOME:
						messageHandler = new WelcomeHandler( clientMessage, job );
						break;
					case WORK:
						messageHandler = new WorkHandler( clientMessage, job );
						break;
					case DHT_GET:
						messageHandler = new DhtGetHandler( clientMessage, job );
						break;
					case RESULT:
						messageHandler = new ResultHandler( clientMessage, job );
						break;
					case DISCONNECT:
						messageHandler = new DisconnectHandler( clientMessage, job );
						break;
					case REPLACE_NODE:
						messageHandler = new ReplaceNodeHandler( clientMessage, job );
						break;
					case PING:
						messageHandler = new PingHandler( clientMessage, job );
						break;
				}
				
				threadPool.submit( messageHandler );

			} catch( SocketTimeoutException timeoutEx ) {
				// Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint( "Waiting..." );
			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
