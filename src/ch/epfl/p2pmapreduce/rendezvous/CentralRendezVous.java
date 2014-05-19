package ch.epfl.p2pmapreduce.rendezvous;

import java.io.IOException;

import Examples.Z_Tools_And_Others.Tools;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;

public class CentralRendezVous extends RendezVous{

	public CentralRendezVous(String _name, int _port, PeerID _peerID) {
		super(_name, _port, _peerID);
	}
	
	public void start(){
		try {
            // Starting the JXTA network
            System.out.println("Start the JXTA network for RendezVous " + name);
            NetPeerGroup = MyNetworkManager.startNetwork();
		} catch (IOException e) {
			System.err.println("IOException");
//			e.printStackTrace();
		} catch (PeerGroupException e) {
			System.err.println("PeerGroupException");
//			e.printStackTrace();
		}
	}

}
