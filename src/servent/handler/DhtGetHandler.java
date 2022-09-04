package servent.handler;

import app.chaos.job.ChaosJob;
import app.chaos.job.Job;
import app.chaos.dht.DhtResult;
import app.chaos.dht.DhtRoutingException;
import servent.message.DhtGetMessage;
import servent.message.DhtGetResultMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

import java.awt.*;

import java.util.List;

import static servent.message.MessageType.DHT_GET;

public class DhtGetHandler implements MessageHandler {

    private Message clientMessage;
    private ChaosJob chaosJob;

    public DhtGetHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run() {
        if( clientMessage.getMessageType() == DHT_GET ) {
            try {
                if( clientMessage instanceof DhtGetMessage message )
                {
                    DhtResult result = chaosJob.getChaosDht().get( message.getKey() );

                    MessageUtil.sendMessage( new DhtGetResultMessage<DhtResult>(
                                    chaosJob.getMyServentInfo(),
                                    clientMessage.getSenderInfo(),
                                    result
                            )
                    );
                }
                else if( clientMessage instanceof DhtGetResultMessage )
                {
                    @SuppressWarnings( "unchecked" )
                    DhtGetResultMessage<DhtResult> resultMessage = ( DhtGetResultMessage<DhtResult> ) clientMessage;

                    chaosJob.getChaosDht().getResults().put( resultMessage.getResult() );
                }
            } catch( DhtRoutingException | InterruptedException e ) { e.printStackTrace(); }
        }
    }
}
