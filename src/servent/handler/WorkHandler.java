package servent.handler;

import app.AppConfig;

import app.chaos.job.ChaosJob;
import app.chaos.job.ChaosWorker;
import app.chaos.job.Job;

import servent.message.Message;
import servent.message.WorkMessage;

import static servent.message.MessageType.WORK;

public class WorkHandler implements MessageHandler {

   private Message clientMessage;
   private ChaosJob chaosJob;

    public WorkHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == WORK )
        {
            WorkMessage workMessage = ( WorkMessage ) clientMessage;

            ChaosWorker chaosWorker = chaosJob.getChaosWorker();
            chaosWorker.setMainPoints( workMessage.getMainPoints() );
            chaosWorker.setWorking( true );

            // AppConfig.timestampedStandardPrint( chaosWorker.getMainPoints().toString() );
        }
    }
}
