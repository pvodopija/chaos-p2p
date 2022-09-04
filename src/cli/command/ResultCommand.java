package cli.command;

import app.AppConfig;
import app.chaos.dht.DhtResult;
import app.chaos.dht.DhtResultGroup;
import app.chaos.dht.DhtRoutingException;
import app.chaos.job.ChaosJob;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResultCommand implements CLICommand {

    @Override
    public String commandName() {
        return "result";
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
                return;
            }

            List<DhtResult> results = null;
            if( ids.length == 2 )
            {
                 results = new ArrayList<>();
                results.add( chaosJob.getChaosDht().get( ids[1] ) );
            }
            else if( ids.length == 1 )
            {
                DhtResultGroup resultGroup = chaosJob.getChaosDht().getAllResults();
                results = resultGroup.getResults();
            }

            exportJobImage( chaosJob, results );

            // AppConfig.timestampedStandardPrint( "( " + jobID + " ) Result " + results );

        } catch( NumberFormatException e ) {
            AppConfig.timestampedErrorPrint( "Invalid argument for status: " + args + ". Should be jobID [fractalID]?." );
        }
        catch( DhtRoutingException | InterruptedException e ) {
            e.printStackTrace();
        }
    }

    private void exportJobImage( ChaosJob chaosJob, List<DhtResult> results )
    {
        final int WIDTH = chaosJob.getShapeWidth() + 20, HEIGHT = chaosJob.getShapeHeight() + 20;
        BufferedImage bufferedImage = new BufferedImage(
                WIDTH,
                HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = ( Graphics2D ) bufferedImage.getGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, WIDTH, HEIGHT );

        Color[] colors = { Color.BLACK, Color.GREEN, Color.DARK_GRAY, Color.MAGENTA, Color.BLUE, Color.ORANGE };
        int c = 0;
        for( DhtResult result: results )
        {
            g.setColor( colors[ ( c++ ) % colors.length ] );
            for( Point point: result.getValues() )
            {
                g.fillOval( point.x, point.y, 10, 10 );
            }
        }

        g.setColor( Color.RED );
        for( Point mainPoint: AppConfig.mainPointsMap.get( chaosJob.getJobID() ) )
        {
            g.fillOval( mainPoint.x - 5, mainPoint.y - 5, 15, 15 );
        }

        String s = ( results.size() == 1 ) ? results.get( 0 ).getKey() : "";
        File outputFile = new File( "chaos/results/" + chaosJob.getJobID() + s + "-result.jpg" );

        try {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            ImageIO.write( bufferedImage, "jpg", outputFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
