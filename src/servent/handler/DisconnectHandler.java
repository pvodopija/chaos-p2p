package servent.handler;

import app.chaos.dht.ChaosDht;
import app.chaos.job.ChaosJob;
import app.chaos.job.Job;

import servent.message.Message;


import static servent.message.MessageType.DISCONNECT;

public class DisconnectHandler implements MessageHandler {
    private Message clientMessage;
    private ChaosJob chaosJob;

    public DisconnectHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == DISCONNECT )
        {
            if( chaosJob.getParentInfo() != null && ChaosDht.areSameFractalIDs(
                    chaosJob.getParentInfo().getFractalID(), clientMessage.getSenderInfo().getFractalID() ) )
            {
                chaosJob.setParentInfo( null );
            }

            chaosJob.getChildrenInfo().removeIf( ( n ) -> ChaosDht.areSameFractalIDs(
                    clientMessage.getSenderInfo().getFractalID(), n.getFractalID() )
            );
            chaosJob.getNeighbourInfo().removeIf( ( n ) -> ChaosDht.areSameFractalIDs(
                    clientMessage.getSenderInfo().getFractalID(), n.getFractalID() )
            );

            String myFractalID = chaosJob.getMyServentInfo().getFractalID();
            if( myFractalID.length() > 1 && myFractalID.endsWith( "0" ) )
            {
                chaosJob.getMyServentInfo().setFractalID( myFractalID.substring( 0, myFractalID.length() - 1 ) );
            }
            else
            {
                chaosJob.getChaosWorker().setWorking( false );
            }


            /* This makes sure that job division goes as expected. */
            chaosJob.getChaosWorker().revertMainPoints();

        }
    }
}
