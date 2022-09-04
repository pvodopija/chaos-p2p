package cli.command;

import app.AppConfig;
import app.chaos.job.ChaosJob;
import app.chaos.dht.DhtResult;
import app.chaos.dht.DhtRoutingException;

import java.awt.*;

import java.util.List;

public class StatusCommand implements CLICommand {

	@Override
	public String commandName() {
		return "status";
	}

	@Override
	public void execute( String args ) {
		try {
			String[] ids = args.split( " " );
			String jobID = ids[0];
			ChaosJob chaosJob = ( ChaosJob ) AppConfig.jobMap.get( jobID );

			if( chaosJob == null )
			{
				AppConfig.timestampedErrorPrint( "JobID does not exist. Check the configuration file." );
			}

			String fractalID = ( ids.length == 2 ) ? ids[1] : chaosJob.getMyServentInfo().getFractalID();

			DhtResult result = chaosJob.getChaosDht().get( fractalID );

			if( result != null )
			{
				AppConfig.timestampedStandardPrint( "Status: " + String.valueOf( result.getValues().size() ) );
			}
			else
			{
				AppConfig.timestampedStandardPrint( "Status: unknown." );
			}

		} catch( NumberFormatException e ) {
			AppConfig.timestampedErrorPrint( "Invalid argument for status: " + args + ". Should be jobID [fractalID]?." );
		} catch( DhtRoutingException e ) {
			e.printStackTrace();
		}
	}

}
