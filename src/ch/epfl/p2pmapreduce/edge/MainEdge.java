package ch.epfl.p2pmapreduce.edge;

import Examples.Z_Tools_And_Others.Tools;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;

public class MainEdge {

	public static final String name = "Anna";
    public static final PeerID PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());

	
	public static void main(String[] args){
		Edge edge = new Edge(name, 9729, PID);
		
		edge.start();
		
		edge.waitForRendezVousConnection();
		
		Tools.PopInformationMessage(name, "Press Enter to start PeerGroup discovery");
		
		edge.discoverPgAdvertisement();
		
		// Stopping the network
        Tools.PopInformationMessage(name, "Stop the JXTA network");
        edge.stop();;
	}
}
