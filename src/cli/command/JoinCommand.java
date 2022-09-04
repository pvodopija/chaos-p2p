package cli.command;

import app.AppConfig;
import app.ServentInfo;
import app.chaos.job.ChaosJob;
import servent.message.JoinMessage;
import servent.message.util.MessageUtil;

public class JoinCommand implements CLICommand {

    @Override
    public String commandName() {
        return "join";
    }

    @Override
    public void execute( String args ) {
        ChaosJob job = ( ChaosJob ) AppConfig.jobMap.get( args );
        if( job == null )
        {
            AppConfig.timestampedErrorPrint( "Job with name '" + args + "' does not exist. Check configuration for spelling." );
            return;
        }

        ServentInfo myServentInfo = job.getMyServentInfo();
        JoinMessage joinMessage =
                new JoinMessage(myServentInfo, AppConfig.bootstrapInfo, String.valueOf( job.getChaosWorker().getkAngles() ) );
        MessageUtil.sendMessage( joinMessage );
    }
}
