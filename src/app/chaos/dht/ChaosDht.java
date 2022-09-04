package app.chaos.dht;

import app.AppConfig;
import app.ServentInfo;
import app.chaos.job.ChaosJob;
import servent.message.DhtGetMessage;
import servent.message.ResultMessage;
import servent.message.util.MessageUtil;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ChaosDht implements Dht<String, DhtResult> {

    private ServentInfo myServentInfo;

    private ServentInfo parentInfo;
    private List<ServentInfo> childrenInfo;
    private List<ServentInfo> neighbourInfo;

    private List<Point> myGeneratedPoints;
    private BlockingQueue<DhtResult> results;
    private Map<String, BlockingQueue<DhtResultGroup>> collectorTableResults;

    public ChaosDht( ChaosJob chaosJob ) {
        this.myServentInfo = chaosJob.getMyServentInfo();
        this.parentInfo = chaosJob.getParentInfo();
        this.childrenInfo = chaosJob.getChildrenInfo();
        this.neighbourInfo = chaosJob.getNeighbourInfo();
        this.myGeneratedPoints = chaosJob.getChaosWorker().getGeneratedPoints();

        results = new LinkedBlockingDeque<>();
        collectorTableResults = new ConcurrentHashMap<>();
    }

    /**
     * @param key fractalID of the node with the wanted value.
     * @param originalSender ServentInfo to whom the result is sent.
     * @return Returns value when it becomes available. Note that this is a blocking operation
     * and no other get() should be called until this one is done.
     * @throws DhtRoutingException
     */
    @Override
    public DhtResult get( ServentInfo originalSender, String key ) throws DhtRoutingException
    {
        ServentInfo routeInfo = route( key );
        try {
            /* Checking for pointer equality. */
            if( routeInfo == myServentInfo )
            {
                results.put( new DhtResult( key, myGeneratedPoints ) );
            }
            else if( routeInfo == null )
            {
                AppConfig.timestampedErrorPrint( "Error in the routing algorithm, " +
                        "all options are exhausted and parent is null." );
                throw new DhtRoutingException();
            }
            else
            {
                MessageUtil.sendMessage( new DhtGetMessage( originalSender, routeInfo, key ) );
            }

            /* Waiting. */
            return results.take();

        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
        /* Shouldn't reach this. */
        return null;
    }

    /**
     * @param key fractalID of the node with the wanted value.
     * @return Returns value if we are the owner.
     * Returns null if we don't have the value and forward the DhtGetMessage to the node that might have it.
     * @throws DhtRoutingException
     */
    public DhtResult get( String key ) throws DhtRoutingException {
        return get( myServentInfo, key );
    }

    public DhtResult getMyResult() throws DhtRoutingException{
        return get( myServentInfo, myServentInfo.getFractalID() );
    }


    @Override
    public void put( String key, DhtResult value ) throws DhtRoutingException {
        // TODO: Implement.
    }

    /* I'm not particularly happy with this code but it's the best I could come up with :( */
    private ServentInfo route( String key )
    {
        String myFractalID = myServentInfo.getFractalID();

        if( myFractalID.length() < key.length() )
        {
            myFractalID += "0".repeat( key.length() - myFractalID.length() );
        }
        else if( myFractalID.length() > key.length() )
        {
            key += "0".repeat( myFractalID.length() - key.length() );
        }

        /* We are the owner of the value in the DHT. */
        if( myFractalID.equals( key ) )
        {
            return myServentInfo;
        }

        System.out.println( myFractalID + " <-myID, key-> " + key );

        for( int i = 0; i < key.length(); i++ )
        {
            /* Looking for the first left-most mismatch. */
            if( myFractalID.charAt( i ) != key.charAt( i ) )
            {
                String pattern = key.substring( 0, i + 1 );
                for( ServentInfo child: childrenInfo )
                {
                    System.out.println( "route startsWith: " + child.getFractalID() + " " + pattern );
                    if( child.getFractalID().startsWith( pattern ) ) /* TODO: Check this again! */
                    {
                        return child;
                    }
                }

                /* If no one matched send to parent. */
                if( parentInfo != null )
                    return parentInfo;

                /* If we are one of the first ( main ) K nodes. */
                int guard = ( i == 0 ) ? 1 : 0;
                pattern = key.substring( 0, guard + i );
                for( ServentInfo neighbour: neighbourInfo )
                {
                    System.out.println( "route startsWith: " + neighbour.getFractalID() + " " + pattern );
                    if( neighbour.getFractalID().startsWith( pattern ) )
                    {
                        return neighbour;
                    }
                }

                /* No matches return null. */
                break;
            }
        }

        return null;
    }


    /**
     * This will get all the DhtResults in the Distributed Hash Table.
     * Keep in mind that this operation is blocking and no other get() operations should be called until this one is finished.
     **/
    public DhtResultGroup getAllResults() throws InterruptedException, DhtRoutingException
    {
        return collectResults( myServentInfo.getFractalID(), null );
    }

    /**
     * This will get all the DhtResults for the collectorFractalID in the Distributed Hash Table.
     * Keep in mind that this operation is blocking and no other get() operations should be called until this one is finished.
     **/
    public DhtResultGroup collectResults( String collectorFractalID, String excludeID )
            throws DhtRoutingException, InterruptedException
    {
        System.out.println( "collectResults();" );

        /* This means we are currently collecting for this collector so we return an empty list to whomever asked us. */
        if( collectorTableResults.containsKey( collectorFractalID ) )
        {
            System.out.println( "Already collected." );
            return new DhtResultGroup(true );
        }
        collectorTableResults.put( collectorFractalID, new LinkedBlockingDeque<>() );
        DhtResult myResult = getMyResult();

        List<ServentInfo> nodesToSendTo = new ArrayList<>( childrenInfo );
        List<DhtResult> results = new ArrayList<>();
        results.add( myResult );

        if( parentInfo == null )
            nodesToSendTo.addAll( neighbourInfo );
        else
            nodesToSendTo.add( parentInfo );

        if( excludeID == null )
            excludeID = "X";

        /* Asking adjacent nodes to send their result and collect results from their adjacent nodes. */
        String finalExcludeID = excludeID;
        nodesToSendTo.removeIf( node -> areSameFractalIDs( node.getFractalID(), finalExcludeID ) );
        for( ServentInfo node: nodesToSendTo )
        {
            MessageUtil.sendMessage( new ResultMessage( myServentInfo, node, collectorFractalID, null ) );
        }

        System.out.println( "Waiting for results from: " + nodesToSendTo );

        /* Waiting for the results of adjacent nodes. Keep in mind that take() is a blocking operation. */
        for( int got = 0; got < nodesToSendTo.size(); got++ )
        {
            results.addAll( collectorTableResults.get( collectorFractalID ).take().getResults() );
        }
        // results.removeIf( DhtResult::isPoison );

        System.out.println( "Got results: " + results );

        // collectorTableResults.remove( collectorFractalID );

        return new DhtResultGroup( results );
    }

    public void putGroupToCollector( String collectorFractalID, DhtResultGroup resultGroup )
    {
        if( !collectorTableResults.containsKey( collectorFractalID ) )
            return;

        try {
            collectorTableResults.get( collectorFractalID ).put( resultGroup );
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    public static boolean areSameFractalIDs( String firstID, String secondID )
    {
        if( firstID.length() < secondID.length() )
        {
            firstID += "0".repeat( secondID.length() - firstID.length() );
        }
        else if( firstID.length() > secondID.length() )
        {
            secondID += "0".repeat( firstID.length() - secondID.length() );
        }

        return firstID.equals( secondID );
    }


    public void setParentInfo( ServentInfo parentInfo ) {
        this.parentInfo = parentInfo;
    }

    public BlockingQueue<DhtResult> getResults() {
        return results;
    }

}
