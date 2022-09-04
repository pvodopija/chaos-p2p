package app;

import servent.message.*;
import servent.message.util.MessageUtil;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BootstrapServer {

	private volatile boolean working = true;

	private Map<String, Job> jobMap = new ConcurrentHashMap<>();

	private ExecutorService threadPool = Executors.newCachedThreadPool();


	private class CLIWorker implements Runnable {

		@Override
		public void run() {
			Scanner sc = new Scanner( System.in );
			
			String line;
			while( true )
			{
				line = sc.nextLine();
				
				if ( line.equals( "stop" ) ) {
					working = false;
					break;
				}
			}
			
			sc.close();
		}
	}

	private class Node {
		public ServentInfo myServentInfo;
		public ServentInfo parentInfo;
		public List<ServentInfo> neighbourInfo;
		public List<ServentInfo> childrenInfo;

		public Node( ServentInfo myServentInfo, ServentInfo parentInfo, List<ServentInfo> neighbourInfo,
					List<ServentInfo> childrenInfo ) {
			this.myServentInfo = myServentInfo;
			this.parentInfo = parentInfo;
			this.neighbourInfo = neighbourInfo;
			this.childrenInfo = childrenInfo;
		}

		public Node() {
			neighbourInfo = new ArrayList<>();
			childrenInfo = new ArrayList<>();
		}
	}

	private class Job {
		Map<Integer, ServentInfo> activeServants = new ConcurrentHashMap<>();

		Node notRespondingNode = new Node();
		long firstNotResponding;

		AtomicInteger nodeCount = new AtomicInteger( 0 );
		AtomicInteger nextParent;
		AtomicInteger degree = new AtomicInteger( 1 );
		AtomicInteger nextTrigger;

		final int K;

		public Job( int K ) {
			this.K = K;
			this.nextParent = new AtomicInteger( K );
			this.nextTrigger = new AtomicInteger( K );
		}

		void disconnectNodeFromJob( Node disconnectingNode )
		{
			activeServants.forEach( ( key, value ) -> {
				if( value.getIpAddress().equals( disconnectingNode.myServentInfo.getIpAddress() )
						&& value.getListenerPort() == disconnectingNode.myServentInfo.getListenerPort() )
				{
					final int last = nodeCount.decrementAndGet();
					ServentInfo lastAddedInfo = activeServants.get( last );
					activeServants.put( key, lastAddedInfo );
					MessageUtil.sendMessage( new ReplaceNodeMessage(
							disconnectingNode.myServentInfo,
							lastAddedInfo,
							disconnectingNode.parentInfo,
							disconnectingNode.neighbourInfo,
							disconnectingNode.childrenInfo
						)
					);
				}
			} );
		}

		void assignNodeToJob( Message clientMessage )
		{
			final int N = nodeCount.getAndIncrement();
			activeServants.put( N, clientMessage.getSenderInfo() );

			List<ServentInfo> sayHelloList = new ArrayList<>();

			/* First K nodes are gonna be in a complete graph so they can route messages without a main node. */
			if( N < K )
			{
				for( int i = 0; i < N; i++ )
				{
					sayHelloList.add( activeServants.get( i ) );
				}

				MessageUtil.sendMessage( new JoinMessage(
							AppConfig.bootstrapInfo,
							clientMessage.getSenderInfo(),
							sayHelloList,
							String.valueOf( N )
						)
				);
			}
			else
			{
				if( N % K == 0 )
				{
					nextParent.getAndIncrement();
				}
				if( N == nextTrigger.get() )
				{
					nextParent.getAndSet( 0 );
					degree.getAndUpdate( x -> K * x );
					nextTrigger.updateAndGet( x -> x + degree.get() );
				}
				sayHelloList.add( activeServants.get( nextParent.get() ) );

				MessageUtil.sendMessage( new JoinMessage(
								AppConfig.bootstrapInfo,
								clientMessage.getSenderInfo(),
								sayHelloList,
								null
					)
				);
			}
		}

	}
	
	public BootstrapServer() {
		jobMap = new ConcurrentHashMap<>();
	}
	
	public void doBootstrap( int bsPort ) {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket( bsPort, 100 );

			/* If there is no connection after 1s, wake up and see if we should terminate. */
			listenerSocket.setSoTimeout( 1000 );
		} catch( IOException e ) {
			AppConfig.timestampedErrorPrint( "Couldn't open listener socket on: "
					+ AppConfig.bootstrapInfo.getListenerPort() );
			System.exit( 0 );
		}

		while( working )
		{
			try {
				Socket clientSocket = listenerSocket.accept();

				/* GOT A MESSAGE! <3 */
				Message clientMessage = MessageUtil.readMessage( clientSocket );

				switch( clientMessage.getMessageType() )
				{
					case JOIN:
						threadPool.execute( () -> {
							final int K = Integer.parseInt( clientMessage.getMessageText() );
							final String jobID = clientMessage.getJobID();

							jobMap.putIfAbsent( jobID , new Job( K ) );
							jobMap.get( jobID ).assignNodeToJob( clientMessage );
						} );
						break;
					case REPLACE_NODE:
						threadPool.execute( () -> {
							ReplaceNodeMessage message = ( ReplaceNodeMessage ) clientMessage;
							jobMap.get( clientMessage.getJobID() ).disconnectNodeFromJob( new Node(
									message.getSenderInfo(),
									message.getParentInfo(),
									message.getNeighbourInfo(),
									message.getChildrenInfo()
								)
							);
						} );
						break;
					case NOT_RESPONDING:
						threadPool.execute( () -> {
							NotRespondingMessage message = ( NotRespondingMessage ) clientMessage;
							Job job = jobMap.get( message.getJobID() );

							if( job.notRespondingNode.myServentInfo == null )
							{
								job.notRespondingNode.myServentInfo = message.getNotRespondingInfo();
								job.firstNotResponding = System.currentTimeMillis();
							}

							if( message.getMessageText().equals( "P" ) )
							{
								job.notRespondingNode.parentInfo = message.getSenderInfo();
							}
							else if( message.getMessageText().equals( "C" ) )
							{
								job.notRespondingNode.childrenInfo.add( message.getSenderInfo() );
							}
							else if( message.getMessageText().equals( "N" ) )
							{
								job.notRespondingNode.neighbourInfo.add( message.getSenderInfo() );
							}

							if( System.currentTimeMillis() - job.firstNotResponding > 7000 )
							{
								job.disconnectNodeFromJob( job.notRespondingNode );
							}

						});
						break;
				}

			} catch( SocketTimeoutException e ) {

			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 **/
	public static void main( String[] args )
	{

		if( args.length != 1 ) {
			AppConfig.timestampedErrorPrint( "Bootstrap started without port argument." );
		}

		int bsPort = 0;
		try {
			bsPort = Integer.parseInt( args[0] );
		} catch ( NumberFormatException e ) {
			AppConfig.timestampedErrorPrint( "Bootstrap port not valid: " + args[0] );
			System.exit( 0 );
		}

		AppConfig.timestampedStandardPrint( "Bootstrap server started on port: " + bsPort);

		AppConfig.readConfig( "chaos/servent_list.properties", -1 );

		BootstrapServer bs = new BootstrapServer();
		bs.doBootstrap( bsPort );
	}
}
