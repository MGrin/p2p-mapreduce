package ch.epfl.p2pmapreduce.rendezvous;

import java.io.File;
import java.io.IOException;

import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import Examples.Z_Tools_And_Others.Tools;

public class RendezVous {
	
	protected String name;
	protected int port;
	protected PeerID peerID;
	protected File configurationFile;
	
	protected NetworkManager MyNetworkManager;
	protected PeerGroup NetPeerGroup;
	
	public RendezVous(String _name, int _port, PeerID _peerID){
		this.name = _name;
		this.port = _port;
		this.peerID = _peerID;
		this.configurationFile = new File("."+ System.getProperty("file.separator") + this.name);
		
		try {
			// Creation of the network manager
			MyNetworkManager = new NetworkManager(NetworkManager.ConfigMode.RENDEZVOUS,
                    name, configurationFile.toURI());
            // Retrieving the configurator
            NetworkConfigurator MyNetworkConfigurator = MyNetworkManager.getConfigurator();
            // Setting configuration
            MyNetworkConfigurator.setTcpPort(port);
            MyNetworkConfigurator.setTcpEnabled(true);
            MyNetworkConfigurator.setTcpIncoming(true);
            MyNetworkConfigurator.setTcpOutgoing(true);
            //MyNetworkConfigurator.setUseMulticast(false);            
            MyNetworkConfigurator.setPeerID(peerID);
            
		} catch (IOException e) {
			System.err.println("IOException");
//			e.printStackTrace();
		}
	}
	
	public void start(){
        try {
            // Starting the JXTA network
           System.out.println("Start the JXTA network for RendezVous " + name);
            NetPeerGroup = MyNetworkManager.startNetwork();
            
            //NetPeerGroup.getRendezVousService();
		} catch (IOException e) {
			System.err.println("IOException");
//			e.printStackTrace();
		} catch (PeerGroupException e) {
			System.err.println("PeerGroupException");
//			e.printStackTrace();
		}
	}
	
	public void stop(){
		MyNetworkManager.stopNetwork();
		System.exit(0);
	}
}
