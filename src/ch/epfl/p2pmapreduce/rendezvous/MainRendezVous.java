package ch.epfl.p2pmapreduce.rendezvous;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import Examples.Z_Tools_And_Others.Tools;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;


public class MainRendezVous {

	public static final String CENTRAL_NAME = "Central Seed";
	public static final int PORT = 9711;
	
	private static HashMap<String, RendezVous> seeds = new HashMap<String, RendezVous>();
	
	public static void main(String[] args) {		
        CentralRendezVous centralSeed = new CentralRendezVous(CENTRAL_NAME, 
        		PORT, IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, CENTRAL_NAME.getBytes()));
        seeds.put(CENTRAL_NAME, centralSeed);
        
        centralSeed.start();
        Tools.PopInformationMessage("Central seed", "Waiting for other peers to connect");
        
        // Stopping the network
        Tools.PopInformationMessage("Central seed", "Stop the JXTA network");
        centralSeed.stop();
        
	}
	
	public static String getAddress(){
		try {
			return "tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + PORT;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
