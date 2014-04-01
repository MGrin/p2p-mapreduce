package ch.epfl.p2pmaperduce.edge;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

public class MainEdge {

	public static void main(String[] args){
		Edge edge = new Edge("Anna", 9712, IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, "Anna".getBytes()));
		edge.start();
		edge.sendTestAdv();
	}
}
