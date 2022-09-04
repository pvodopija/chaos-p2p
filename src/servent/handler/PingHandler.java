package servent.handler;

import app.chaos.job.ChaosJob;
import app.chaos.job.Job;
import servent.message.Message;
import servent.message.MessageType;

public class PingHandler implements MessageHandler {

    private Message clientMessage;
    private ChaosJob chaosJob;

    public PingHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == MessageType.PING )
        {
            chaosJob.getFailureDetectionWorker().getResponseMap().put( clientMessage.getSenderInfo(), System.currentTimeMillis() );
        }
    }
}
