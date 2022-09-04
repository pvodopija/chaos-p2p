package cli.command;

import app.AppConfig;
import app.chaos.job.ChaosJob;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute( String args ) {
		AppConfig.timestampedStandardPrint( "########################## INFO ##########################" );
		AppConfig.jobMap.forEach( ( key, value ) -> {
			ChaosJob job = ( ChaosJob ) value;
			AppConfig.timestampedStandardPrint( "FractalID: " + job.getMyServentInfo().getFractalID() );
			AppConfig.timestampedStandardPrint( "JobID: " + key );
			AppConfig.timestampedStandardPrint( "Working: " + job.getChaosWorker().isWorking() );
			AppConfig.timestampedStandardPrint( "Parent: " + job.getParentInfo() );
			AppConfig.timestampedStandardPrint( "Neighbors: " + job.getNeighbourInfo().toString() );
			AppConfig.timestampedStandardPrint( "Children: " + job.getChildrenInfo().toString() );
			AppConfig.timestampedStandardPrint( "----------------------------------------------------------" );
		} );
	}

}
