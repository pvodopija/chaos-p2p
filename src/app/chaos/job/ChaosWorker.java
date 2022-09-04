package app.chaos.job;

import app.Cancellable;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;


public class ChaosWorker implements Runnable, Cancellable {

    private volatile boolean working = false;
    private volatile boolean alive = true;
    private int width;
    private int height;
    private final int kAngles;
    private final float stepRatio;

    private Stack<List<Point>> mainPointsHistory;
    private List<Point> mainPoints;
    private List<Point> generatedPoints;

    public ChaosWorker( int width, int height, int kAngles, float stepRatio, List<Point> mainPoints )
    {
        this.width = width;
        this.height = height;
        this.kAngles = kAngles;
        this.stepRatio = stepRatio;
        this.mainPoints = mainPoints;

        mainPointsHistory = new Stack<>();
        mainPointsHistory.push( new ArrayList<>( mainPoints ) );
        generatedPoints = new ArrayList<>();
    }

    @Override
    public void run()
    {
        Random random = new Random();
        Point previousPoint = mainPoints.get( random.nextInt( mainPoints.size() ) );

        while( alive )
        {
           if( working )
           {
               Point randomPoint = mainPoints.get( random.nextInt( mainPoints.size() ) );
               final int directionX = ( previousPoint.x <= randomPoint.x ) ? 1 : -1;
               final int directionY = ( previousPoint.y <= randomPoint.y ) ? 1 : -1;

               final int xDist = directionX * Math.abs( previousPoint.x - randomPoint.x );
               final int yDist = directionY * Math.abs( previousPoint.y - randomPoint.y );

               final int x = Math.round( previousPoint.x + xDist * stepRatio );
               final int y = Math.round( previousPoint.y + yDist * stepRatio );

               Point newPoint = new Point( x, y );
               previousPoint = newPoint;

               generatedPoints.add( newPoint );
           }
           try {
               Thread.sleep( 100 );
           } catch( InterruptedException e ) {
               e.printStackTrace();
           }
       }
        System.out.println( "I'm dead." );
    }

    public void revertMainPoints()
    {
        mainPoints = mainPointsHistory.pop();
    }


    @Override
    public void stop() {
        alive = false;
    }

    public int getkAngles() {
        return kAngles;
    }

    public float getStepRatio() {
        return stepRatio;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking( boolean working ) {
        this.working = working;
    }

    public List<Point> getMainPoints() {
        return mainPoints;
    }

    public void setMainPoints(List<Point> mainPoints) {
        this.mainPoints = mainPoints;
    }

    public List<Point> getGeneratedPoints() {
        return generatedPoints;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setGeneratedPoints(List<Point> generatedPoints) {
        this.generatedPoints = generatedPoints;
    }

    public Stack<List<Point>> getMainPointsHistory() {
        return mainPointsHistory;
    }
}
