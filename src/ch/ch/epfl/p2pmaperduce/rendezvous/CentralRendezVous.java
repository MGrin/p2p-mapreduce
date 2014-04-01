package ch.epfl.p2pmaperduce.rendezvous;

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
            Tools.PopInformationMessage(name, "Start the JXTA network for RendezVous "+name);
            NetPeerGroup = MyNetworkManager.startNetwork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
