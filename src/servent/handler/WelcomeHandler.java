package servent.handler;

import app.chaos.job.ChaosJob;
import app.chaos.job.Job;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.WelcomeMessage;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;
	private ChaosJob chaosJob;
	
	public WelcomeHandler(Message clientMessage, Job job ) {
		this.clientMessage = clientMessage;
		this.chaosJob = ( ChaosJob ) job;
	}
	
	@Override
	public void run()
	{
		if( clientMessage.getMessageType() == MessageType.WELCOME )
		{
			WelcomeMessage message = ( WelcomeMessage ) clientMessage;
			if( chaosJob.getMyServentInfo().getFractalID() == null )
			{
				chaosJob.getMyServentInfo().setFractalID( message.getFractalID() );
				chaosJob.setParentInfo( message.getSenderInfo() );
			}
			else
			{
				chaosJob.getNeighbourInfo().add( message.getSenderInfo() );
			}

		}

	}

}
