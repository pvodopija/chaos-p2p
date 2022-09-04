package cli.command;

import app.AppConfig;
import app.ServentInfo;
import app.chaos.job.ChaosJob;
import servent.message.DisconnectMessage;
import servent.message.ReplaceNodeMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;

public class DisconnectCommand implements CLICommand {

    @Override
    public String commandName() {
        return "disconnect";
    }

    @Override
    public void execute( String args ) {
        String jobID = args;
        ChaosJob chaosJob = ( ChaosJob ) AppConfig.jobMap.get( jobID );
        chaosJob.getChaosWorker().setWorking( false );

        MessageUtil.sendMessage( new ReplaceNodeMessage(
                chaosJob.getMyServentInfo(),
                AppConfig.bootstrapInfo,
                chaosJob.getParentInfo(),
                chaosJob.getNeighbourInfo(),
                chaosJob.getChildrenInfo()
        ) );

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

        chaosJob.setParentInfo( null );
        chaosJob.setChildrenInfo( new ArrayList<>() );
        chaosJob.setNeighbourInfo( new ArrayList<>() );
    }
}
