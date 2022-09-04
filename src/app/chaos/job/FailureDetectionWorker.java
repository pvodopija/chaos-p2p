package app.chaos.job;

import app.AppConfig;
import app.Cancellable;
import app.ServentInfo;
import servent.message.NotRespondingMessage;
import servent.message.PingMessage;
import servent.message.util.MessageUtil;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FailureDetectionWorker implements Runnable, Cancellable {

    private volatile boolean alive = true;

    Map<ServentInfo, Long> responseMap;

    ServentInfo myServentInfo;
    ServentInfo parentInfo;
    List<ServentInfo> neighbourInfo;
    List<ServentInfo> childrenInfo;

    private final int weakFailureBound;
    private final int strongFailureBound;

    public FailureDetectionWorker(ServentInfo myServentInfo, ServentInfo parentInfo,
                                  List<ServentInfo> neighbourInfo, List<ServentInfo> childrenInfo,
                                  int weakFailureBound, int storngFailureBound ) {
        this.myServentInfo = myServentInfo;
        this.parentInfo = parentInfo;
        this.neighbourInfo = neighbourInfo;
        this.childrenInfo = childrenInfo;
        this.weakFailureBound = weakFailureBound;
        this.strongFailureBound = storngFailureBound;

        responseMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run()
    {
        while( alive )
        {
            try {
                if( myServentInfo.getFractalID() == null )
                {
                    Thread.sleep( 1000 );
                    continue;
                }

                long previousPulse = System.currentTimeMillis();

                if( parentInfo != null )
                {
                    MessageUtil.sendMessage( new PingMessage( myServentInfo, parentInfo ) );
                }

                for( ServentInfo neighbour: neighbourInfo )
                {
                    MessageUtil.sendMessage( new PingMessage( myServentInfo, neighbour ) );
                }

                for( ServentInfo child: childrenInfo )
                {
                    MessageUtil.sendMessage( new PingMessage( myServentInfo, child ) );
                }

                /* Check responses. */
                Thread.sleep( strongFailureBound );

                responseMap.forEach( ( key, value ) -> {
                    if( value - previousPulse > strongFailureBound )
                    {
                        String relationship;
                        ServentInfo notRespondingInfo = key;

                        if( key.equals( parentInfo ) )
                        {
                            relationship = "C";
                            parentInfo = null;
                        }
                        else if( neighbourInfo.contains( key ) )
                        {
                            relationship = "N";
                            neighbourInfo.remove( key );
                        }
                        else
                        {
                            relationship = "P";
                            childrenInfo.remove( key );
                        }

                        MessageUtil.sendMessage( new NotRespondingMessage(
                                    myServentInfo,
                                    AppConfig.bootstrapInfo,
                                    notRespondingInfo,
                                    relationship
                        ) );

                        responseMap.remove( key );
                    }
                } );



            } catch( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        alive = false;
    }

    public Map<ServentInfo, Long> getResponseMap() {
        return responseMap;
    }

    public void notResponding( ServentInfo node )
    {
        String relationship;
        ServentInfo notRespondingInfo = node;

        if( node.equals( parentInfo ) )
        {
            relationship = "C";
            parentInfo = null;
        }
        else if( neighbourInfo.contains( node ) )
        {
            relationship = "N";
            neighbourInfo.remove( node );
        }
        else
        {
            relationship = "P";
            childrenInfo.remove( node );
        }

        MessageUtil.sendMessage( new NotRespondingMessage(
                myServentInfo,
                AppConfig.bootstrapInfo,
                notRespondingInfo,
                relationship
        ) );

    }

}
