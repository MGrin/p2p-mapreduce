package ch.epfl.p2pmapreduce.edge;

import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import Examples.Z_Tools_And_Others.Tools;

public class IndexBroadcaster {


	public static final String name = "IndexBroadcaster";
    public static final PeerID PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());
    public static final int PORT = 9780;
    
	public static void main(String[] args){
		
		AdvertisementFactory.registerAdvertisementInstance(IndexAdvertisement.getAdvertisementType(), new IndexAdvertisement.Instantiator());
		
		Edge edge = new Edge(name, PORT, PID);
		
		edge.start();
		
		edge.waitForRendezVousConnection();
		
		Tools.PopInformationMessage(name, "Press Enter to send IndexAdvertisement");
		
		edge.publishIndexAdvertisement();
		
		// Stopping the network
        Tools.PopInformationMessage(name, "Stop the JXTA network");
        edge.stop();
	}
	
}
