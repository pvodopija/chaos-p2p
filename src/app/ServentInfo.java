package app;

import app.chaos.dht.ChaosDht;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = -7687373762037884815L;

	private final String ipAddress;
	private final int listenerPort;
	private final int id;
	private String fractalID;
	private final String jobID;
	
	public ServentInfo( String ipAddress, int listenerPort, int id ) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.id = id;
		jobID = null;
	}

	public ServentInfo( String ipAddress, int listenerPort, int id, String jobID ) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.id = id;
		this.jobID = jobID;
	}


	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getId() {
		return id;
	}

	public String getFractalID() {
		return fractalID;
	}

	public void setFractalID(String fractalID) {
		this.fractalID = fractalID;
	}

	public String getJobID() {
		return jobID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServentInfo that = (ServentInfo) o;
		return listenerPort == that.listenerPort && id == that.id
				&& Objects.equals(ipAddress, that.ipAddress) && Objects.equals(jobID, that.jobID)
		&& ChaosDht.areSameFractalIDs( fractalID, that.fractalID );
	}

	@Override
	public int hashCode() {
		return Objects.hash(ipAddress, listenerPort, id, jobID);
	}

	@Override
	public String toString() {
		return "[" + fractalID + "|" + ipAddress + "|" + listenerPort + "]";
	}

}
