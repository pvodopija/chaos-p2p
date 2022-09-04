package servent.handler;

import app.chaos.dht.DhtResult;
import app.chaos.dht.DhtResultGroup;
import app.chaos.job.ChaosJob;
import app.chaos.job.Job;
import servent.message.Message;
import servent.message.ResultMessage;
import servent.message.util.MessageUtil;

import java.util.List;

import static servent.message.MessageType.RESULT;

public class ResultHandler implements MessageHandler {

    private Message clientMessage;
    private ChaosJob chaosJob;

    public ResultHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job ;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == RESULT )
        {
            try {
                ResultMessage message = ( ResultMessage ) clientMessage;
                if( message.getResultGroup() == null )
                {
                    DhtResultGroup resultGroup = chaosJob.getChaosDht().collectResults(
                            message.getCollectorFractalID(), message.getSenderInfo().getFractalID()
                    );
                    MessageUtil.sendMessage( new ResultMessage(
                            chaosJob.getMyServentInfo(),
                            message.getSenderInfo(),
                            message.getCollectorFractalID(),
                            resultGroup
                    ) );
                }
                else
                {
                    chaosJob.getChaosDht().putGroupToCollector( message.getCollectorFractalID(), message.getResultGroup() );
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
