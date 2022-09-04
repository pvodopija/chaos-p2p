package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.chaos.job.ChaosJob;
import app.chaos.job.ChaosWorker;
import app.chaos.job.Job;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HelloHandler implements MessageHandler {

    private Message clientMessage;
    private ChaosJob chaosJob;

    public HelloHandler( Message clientMessage, Job job ) {
        this.clientMessage = clientMessage;
        this.chaosJob = ( ChaosJob ) job;
    }

    @Override
    public void run()
    {
        if( clientMessage.getMessageType() == MessageType.HELLO )
        {
            /* This means we are the parent of this new node and should send them their new fractalID. */
            if( clientMessage.getSenderInfo().getFractalID() == null )
            {
                String childFractalID = chaosJob.getMyServentInfo().getFractalID() + ( chaosJob.getChildrenInfo().size() + 1 );
                if( chaosJob.getChildrenInfo().isEmpty() )
                {
                    MessageUtil.sendMessage(
                            new WelcomeMessage( chaosJob.getMyServentInfo(), clientMessage.getSenderInfo(), childFractalID )
                    );
                }
                else if( chaosJob.getChildrenInfo().size() < chaosJob.getChaosWorker().getkAngles() )
                {
                    List<ServentInfo> helloNeighbour = new ArrayList<>();
                    helloNeighbour.add( chaosJob.getChildrenInfo().get( chaosJob.getChildrenInfo().size() - 1 ) );
                    MessageUtil.sendMessage(
                            new JoinMessage( chaosJob.getMyServentInfo(), clientMessage.getSenderInfo(), helloNeighbour, childFractalID )
                    );
                }

                /* Add new node to the list of children. */
                ServentInfo childInfo = clientMessage.getSenderInfo();
                childInfo.setFractalID( childFractalID );
                chaosJob.getChildrenInfo().add( clientMessage.getSenderInfo() );

                /* Should split the job and start working. */
                if( chaosJob.getChildrenInfo().size() == chaosJob.getChaosWorker().getkAngles() - 1 )
                {
                    chaosJob.getMyServentInfo().setFractalID( chaosJob.getMyServentInfo().getFractalID() + "0" );
                    delegateWorkToNodes( chaosJob.getChildrenInfo() );
                }
            }
            else
            {
                chaosJob.getNeighbourInfo().add( clientMessage.getSenderInfo() );
                MessageUtil.sendMessage(
                        new WelcomeMessage( chaosJob.getMyServentInfo(), clientMessage.getSenderInfo() )
                );

                if( chaosJob.getNeighbourInfo().size() == chaosJob.getChaosWorker().getkAngles() - 1
                        && chaosJob.getMyServentInfo().getFractalID().startsWith( "0" ) )
                {
                    delegateWorkToNodes( chaosJob.getNeighbourInfo() );
                }
            }
        }
    }

    private void delegateWorkToNodes( List<ServentInfo> nodes )
    {
        ChaosWorker chaosWorker = chaosJob.getChaosWorker();
        final float stepRatio = chaosWorker.getStepRatio();
        List<Point> mainPoints = chaosWorker.getMainPoints();

        if( chaosWorker.getMainPoints().size() != nodes.size() + 1 )
        {
            AppConfig.timestampedErrorPrint( "Number of children should be one less than number of angls in a shape." +
                    " But instead got: " + chaosJob.getChildrenInfo().size() + " and " + chaosWorker.getMainPoints().size() );
        }
        else if( !chaosWorker.isWorking() )
        {
            return;
        }

        /* Split the work and send it to nodes. The first point is always gonna be our pivot and the rest is for the other nodes.
        That's why for loop starts from 1. */
        try {
            for( int i = 1; i < mainPoints.size(); i++ )
            {
                List<Point> newPoints = new ArrayList<>();
                Point pivot = mainPoints.get( i );
                newPoints.add( pivot );

                for( int j = ( i + 1 ) % mainPoints.size(); j != i; j = ( j + 1 ) % mainPoints.size() )
                {
                    Point rotationPoint = mainPoints.get( j );
                    final int directionX = ( pivot.x <= rotationPoint.x ) ? 1 : -1;
                    final int directionY = ( pivot.y <= rotationPoint.y ) ? 1 : -1;

                    final int xDist = directionX * Math.abs( pivot.x - rotationPoint.x );
                    final int yDist = directionY * Math.abs( pivot.y - rotationPoint.y );

                    final int x = Math.round( pivot.x + xDist * stepRatio );
                    final int y = Math.round( pivot.y + yDist * stepRatio );
                    newPoints.add( new Point( x, y ) );
                }
                MessageUtil.sendMessage( new WorkMessage( chaosJob.getMyServentInfo(), nodes.get( i - 1 ), newPoints ) );
            }

        } catch( Exception e ) { e.printStackTrace();}

        /* Save current main points in case some node is deleted and update our chaosWorker to work with new set of points. */
        chaosWorker.getMainPointsHistory().push( mainPoints );

        List<Point> newPoints = new ArrayList<>();
        Point pivot = mainPoints.get( 0 );
        newPoints.add( pivot );
        mainPoints.remove( 0 );

        for( Point p: mainPoints )
        {
            int x = Math.round( ( pivot.x + p.x ) * stepRatio );
            int y = Math.round( ( pivot.y + p.y ) * stepRatio );
            newPoints.add( new Point( x, y ) );
        }

        chaosWorker.setMainPoints( newPoints );
    }
}
