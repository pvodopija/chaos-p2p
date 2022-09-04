package app;

import app.chaos.job.ChaosJob;
import app.chaos.job.Job;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint( String message ) {
		DateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
		Date now = new Date();
		
		System.out.println( timeFormat.format( now ) + " - " + message );
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint( String message ) {
		DateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
		Date now = new Date();
		
		System.err.println( timeFormat.format( now ) + " - " + message );
	}
	
	public static boolean INITIALIZED = false;
	public static int SERVENT_COUNT;

	public static Map<String, Job> jobMap = new ConcurrentHashMap<>();
	public static Map<String, List<Point>> mainPointsMap = new HashMap<>();
	public static ServentInfo bootstrapInfo;

	
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 * 
	 * @param configName name of configuration file
	 * @param serventId id of the servent, as used in the configuration file
	 **/
	public static void readConfig( String configName, int serventId )
	{
		Properties properties = new Properties();
		try {
			properties.load( new FileInputStream( new File( configName ) ) );
			
		} catch( IOException e ) {
			timestampedErrorPrint( "Couldn't open properties file. Exiting..." );
			System.exit( 0 );
		}
		
		try {
			String bootstrapIp = properties.getProperty( "bs.ip" );
			int bootstrapPort = Integer.parseInt( properties.getProperty( "bs.port" ) );
			bootstrapInfo = new ServentInfo( bootstrapIp, bootstrapPort, -1 );
		} catch( NumberFormatException e ) {
			timestampedErrorPrint( "Problem reading bootstrap_port. Exiting..." );
			System.exit( 0 );
		}
		
		try {
			SERVENT_COUNT = Integer.parseInt( properties.getProperty( "servent_count" ) );
		} catch ( NumberFormatException e ) {
			timestampedErrorPrint( "Problem reading servent_count. Exiting..." );
			System.exit( 0 );
		}

		if( serventId == -1 )
			return;
		
		String portProperty = "servent"+serventId+".port";
		
		int serventPort = -1;
		
		try {
			serventPort = Integer.parseInt( properties.getProperty( portProperty ) );
		} catch ( NumberFormatException e ) {
			timestampedErrorPrint( "Problem reading " + portProperty + ". Exiting..." );
			System.exit( 0 );
		}
		
		myServentInfo = new ServentInfo( "localhost", serventPort, serventId );

		/* Parsing jobs. Very important! */
		try
		{
			String[] jobs = properties.getProperty( "jobs" ).split( " \\| " );
			for( int i = 0; i < jobs.length; i++ )
			{
				String[] job = jobs[i].split( "," );
				String[] dim = job[1].split( "x" );
				String[] points = job[6].split( ";" );

				List<Point> startingPoints = new ArrayList<>();

				try {
					for( int j = 0; j < points.length; j++ )
					{
						String[] p = points[j].split( "_" );
						Point newPoint = new Point( Integer.parseInt( p[0] ), Integer.parseInt( p[1] ) );
						startingPoints.add( newPoint);
					}
				} catch( NumberFormatException e ) {
					timestampedErrorPrint( "Problem reading starting points Make sure coordinates are split with '_' and " +
							"points with ';'. Exiting..." );
					System.exit( 0 );
				}

				ServentInfo serventInfoCopy = new ServentInfo(
						myServentInfo.getIpAddress(), myServentInfo.getListenerPort(), myServentInfo.getId(), job[0]
				);

				ChaosJob newJob = new ChaosJob(
						job[0],
						serventInfoCopy,
						Integer.parseInt( dim[0] ), Integer.parseInt( dim[1] ),
						Integer.parseInt( job[2] ),
						Float.parseFloat( job[3] ),
						Integer.parseInt( job[4] ),
						Integer.parseInt( job[5] ),
						startingPoints
				);
				jobMap.put( newJob.getJobID(), newJob );
				mainPointsMap.put( newJob.getJobID(), new ArrayList<>( startingPoints ) );
			}

		} catch( NumberFormatException e ) {
			timestampedErrorPrint( "Problem reading jobs. Exiting..." );
			System.exit( 0 );
		}
	}
	
}
