package servent.handler;

import app.ServentInfo;
import app.chaos.job.ChaosJob;
import app.chaos.job.Job;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

import static servent.message.MessageType.REPLACE_NODE;

public class ReplaceNodeHandler implements MessageHandler {
    private Message clientMessage;
    private ChaosJob chaosJob;

    public ReplaceNodeHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == REPLACE_NODE )
        {
            /* Disconnect from adjacent nodes. */
            if( chaosJob.getParentInfo() != null )
            {
                MessageUtil.sendMessage( new DisconnectMessage( chaosJob.getMyServentInfo(), chaosJob.getParentInfo() ) );
            }
            for( ServentInfo neighbour: chaosJob.getNeighbourInfo() )
            {
                MessageUtil.sendMessage( new DisconnectMessage( chaosJob.getMyServentInfo(), neighbour ) );
            }
            for( ServentInfo child: chaosJob.getChildrenInfo() )
            {
                MessageUtil.sendMessage( new DisconnectMessage( chaosJob.getMyServentInfo(), child ) );
            }

            try {
                Thread.sleep( 2000 );
            } catch( InterruptedException e ) {
                e.printStackTrace();
            }

            /* Replace deleted node. */
            ReplaceNodeMessage message = ( ReplaceNodeMessage ) clientMessage;
            chaosJob.getMyServentInfo().setFractalID( clientMessage.getSenderInfo().getFractalID() );
            chaosJob.setNeighbourInfo( new ArrayList<>() );
            chaosJob.setParentInfo( null );

            if( message.getParentInfo() != null )
            {
                MessageUtil.sendMessage( new HelloMessage( chaosJob.getMyServentInfo(), message.getParentInfo() ) );
            }
            for( ServentInfo neighbour: message.getNeighbourInfo() )
            {
                MessageUtil.sendMessage( new HelloMessage( chaosJob.getMyServentInfo(), neighbour ) );
            }

            /* Make us their new parent. */
            chaosJob.setChildrenInfo( message.getChildrenInfo() );
            List<ServentInfo> sayHelloToNobody = new ArrayList<>();
            for( int i = 0; i < chaosJob.getChildrenInfo().size(); i++ )
            {
                List<ServentInfo> sayHello = new ArrayList<>();
                ServentInfo child = chaosJob.getChildrenInfo().get( i );
                try {
                    sayHello.add( chaosJob.getChildrenInfo().get( i + 1 ) );
                } catch( IndexOutOfBoundsException e ) { }

                MessageUtil.sendMessage( new JoinMessage(
                        chaosJob.getMyServentInfo(),
                        child,
                        sayHello,
                        child.getFractalID()
                ) );
            }

        }
    }
}
