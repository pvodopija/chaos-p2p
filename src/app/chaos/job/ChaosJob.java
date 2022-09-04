package app.chaos.job;

import app.ServentInfo;
import app.chaos.dht.ChaosDht;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChaosJob implements Job {

    private final String jobID;
    private ServentInfo myServentInfo; /* This one is not in the constructor. */

    private ServentInfo parentInfo = null;
    private List<ServentInfo> neighbourInfo;
    private List<ServentInfo> childrenInfo;

    private final int weakFailureBound;
    private final int strongFailureBound;

    private ChaosWorker chaosWorker;
    private FailureDetectionWorker failureDetectionWorker;
    private ChaosDht chaosDht;

    public ChaosJob( String jobID, ServentInfo myServentInfo, int shapeWidth, int shapeHeight, int shapeAngles, float stepRatio, int weakFailureBound,
                    int strongFailureBound, List<Point> startingPoints )
    {
        this.jobID = jobID;
        this.weakFailureBound = weakFailureBound;
        this.strongFailureBound = strongFailureBound;
        this.myServentInfo = myServentInfo;

        neighbourInfo = new ArrayList<>();
        childrenInfo = new ArrayList<>();

        chaosWorker = new ChaosWorker( shapeWidth, shapeHeight, shapeAngles, stepRatio, startingPoints );
        new Thread( chaosWorker ).start();

        failureDetectionWorker = new FailureDetectionWorker(
                myServentInfo, parentInfo, neighbourInfo, childrenInfo, weakFailureBound, strongFailureBound
        );
        // new Thread( failureDetectionWorker ).start();

        chaosDht = new ChaosDht( this );
    }

    @Override
    public String getJobID() {
        return jobID;
    }

    @Override
    public void stop() {
        chaosWorker.stop();
        failureDetectionWorker.stop();
    }

    public ChaosDht getChaosDht() {
        return chaosDht;
    }

    public ServentInfo getMyServentInfo() {
        return myServentInfo;
    }

    public void setMyServentInfo( ServentInfo myServentInfo ) {
        this.myServentInfo = myServentInfo;
    }

    public ServentInfo getParentInfo() {
        return parentInfo;
    }

    public void setParentInfo(ServentInfo parentInfo) {
        this.parentInfo = parentInfo;
        this.chaosDht.setParentInfo( parentInfo );
    }

    public List<ServentInfo> getNeighbourInfo() {
        return neighbourInfo;
    }

    public void setNeighbourInfo(List<ServentInfo> neighbourInfo) {
        this.neighbourInfo = neighbourInfo;
    }

    public List<ServentInfo> getChildrenInfo() {
        return childrenInfo;
    }

    public void setChildrenInfo(List<ServentInfo> childrenInfo) {
        this.childrenInfo = childrenInfo;
    }

    public int getWeakFailureBound() {
        return weakFailureBound;
    }

    public int getStrongFailureBound() {
        return strongFailureBound;
    }

    public ChaosWorker getChaosWorker() {
        return chaosWorker;
    }

    public FailureDetectionWorker getFailureDetectionWorker() {
        return failureDetectionWorker;
    }

    public void setChaosWorker(ChaosWorker chaosWorker ) {
        this.chaosWorker = chaosWorker;
    }

    public int getShapeWidth()
    {
        return chaosWorker.getWidth();
    }
    public int getShapeHeight()
    {
        return chaosWorker.getHeight();
    }



}
