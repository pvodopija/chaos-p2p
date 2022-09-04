package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.chaos.job.ChaosJob;
import app.chaos.job.Job;

import servent.message.HelloMessage;
import servent.message.JoinMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

import static servent.message.MessageType.JOIN;

public class JoinHandler implements MessageHandler {

    private Message clientMessage;
    private ChaosJob chaosJob;

    public JoinHandler( Message clinetMessage, Job chaosJob ) {
        this.clientMessage = clinetMessage;
        this.chaosJob = ( ChaosJob ) chaosJob;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == JOIN )
        {
            JoinMessage message = ( JoinMessage ) clientMessage;

            if( message.getFractalID() != null && message.getFractalID().equals( "0" ) )
            {
                chaosJob.getChaosWorker().setWorking( true );
            }

            chaosJob.getMyServentInfo().setFractalID( message.getFractalID() );

            if( clientMessage.getSenderInfo().getId() != -1 )
            {
                chaosJob.setParentInfo( clientMessage.getSenderInfo() );
            }

            // AppConfig.timestampedStandardPrint( message.getSayHelloInfoList().toString() );

            /* Say hello to neighbors if we are one of the first K nodes or just to a parent and previous neighbour.
            After this we should be getting WelcomeMessage. */
            for( ServentInfo s: message.getSayHelloInfoList() )
            {
                MessageUtil.sendMessage( new HelloMessage( chaosJob.getMyServentInfo(), s ) );
            }

        }
    }
}
