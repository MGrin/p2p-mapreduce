package ch.epfl.p2pmaperduce;

import java.util.HashMap;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

import ch.epfl.p2pmaperduce.rendezvous.CentralRendezVous;
import ch.epfl.p2pmaperduce.rendezvous.RendezVous;

public class MainRendezVous {

	public static final String CENTRAL_NAME = "Central Seed";
	public static final int PORT = 9710;
	
	private static HashMap<String, RendezVous> seeds = new HashMap<String, RendezVous>();
	
	public static void main(String[] args) {		
        CentralRendezVous centralSeed = new CentralRendezVous(CENTRAL_NAME, 
        		PORT, IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, CENTRAL_NAME.getBytes()));
        seeds.put(CENTRAL_NAME, centralSeed);
        
        centralSeed.start();
        
        
	}
}
