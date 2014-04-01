package ch.epfl.p2pmaperduce.edge;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import Examples.Z_Tools_And_Others.Tools;

import ch.epfl.p2pmaperduce.rendezvous.MainRendezVous;


import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

public class Edge implements RendezvousListener, DiscoveryListener {

	public static final String getMainRendezVousIP = MainRendezVous.getAddress();
	
	protected String name;
	protected int port;
	protected PeerID peerID;
	protected File configFile;
	
	public NetworkManager MyNetworkManager;
	public NetworkConfigurator MyNetworkConfigurator;
	public PeerGroup NetPeerGroup;
	
	public Edge(String _name, int _port, PeerID _peerID){
		name = _name;
		port = _port;
		peerID = _peerID;
		configFile = new File("." + System.getProperty("file.separator") + name);
		
		try {
			MyNetworkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, name, configFile.toURI());
			MyNetworkConfigurator = MyNetworkManager.getConfigurator();
			
			MyNetworkConfigurator.addSeedRendezvous(URI.create(getMainRendezVousIP));
			MyNetworkConfigurator.setTcpPort(port);
            MyNetworkConfigurator.setTcpEnabled(true);
            MyNetworkConfigurator.setTcpIncoming(true);
            MyNetworkConfigurator.setTcpOutgoing(true);
            MyNetworkConfigurator.setUseMulticast(false);
            
            MyNetworkConfigurator.setPeerID(peerID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start(){
		try {
			NetPeerGroup = MyNetworkManager.startNetwork();
			NetPeerGroup.getRendezVousService().setAutoStart(false);
			NetPeerGroup.getRendezVousService().addListener(this);
			NetPeerGroup.getDiscoveryService().addDiscoveryListener(this);
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void stop(){
		MyNetworkManager.stopNetwork();
	}
	
	public void sendTestAdv(){
		try {
			NetPeerGroup.getDiscoveryService().publish(AdvertisementFactory.newAdvertisement("Test adv"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rendezvousEvent(RendezvousEvent rdvE) {
		System.out.println("New rendevouz id : "+rdvE.getPeerID().toString());
	}

	@Override
	public void discoveryEvent(DiscoveryEvent discE) {
		System.out.println("New discovery event id: "+discE.toString());
	}
}
