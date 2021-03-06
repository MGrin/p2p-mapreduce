package ch.epfl.p2pmapreduce.edge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import Examples.Z_Tools_And_Others.Tools;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisementDiscoverer;
import ch.epfl.p2pmapreduce.rendezvous.MainRendezVous;

public class Edge implements RendezvousListener {

	public static final String getMainRendezVousIP = MainRendezVous.getAddress();

	protected String name;
	protected int port;
	protected PeerID peerID;
	protected File configFile;

	public NetworkManager MyNetworkManager;
	public NetworkConfigurator MyNetworkConfigurator;
	public PeerGroup NetPeerGroup;

	public Edge() {
		//dummy object.
	}

	public Edge(String _name, int _port, PeerID _peerID){
		name = _name;
		port = _port;
		peerID = _peerID;
		configFile = new File("." + System.getProperty("file.separator") + name);

		try {
			MyNetworkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, name, configFile.toURI());
			MyNetworkConfigurator = MyNetworkManager.getConfigurator();

			MyNetworkConfigurator.clearRendezvousSeeds();

			MyNetworkConfigurator.addSeedRendezvous(URI.create(getMainRendezVousIP));
			MyNetworkConfigurator.setTcpPort(port);
			MyNetworkConfigurator.setTcpEnabled(true); 
			MyNetworkConfigurator.setTcpIncoming(true);
			MyNetworkConfigurator.setTcpOutgoing(true);
			// MyNetworkConfigurator.setUseMulticast(false);

			MyNetworkConfigurator.setPeerID(peerID);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Rendez vous peer is: " + getMainRendezVousIP);
	}

	public void start(){
		try {

			NetPeerGroup = MyNetworkManager.startNetwork();
			//NetPeerGroup.getRendezVousService().setAutoStart(false);
			//NetPeerGroup.getRendezVousService().addListener(this);
			//NetPeerGroup.getDiscoveryService().addDiscoveryListener(this);
			
			// Registering our customized advertisement instance
	        AdvertisementFactory.registerAdvertisementInstance(
	                IndexAdvertisement.getAdvertisementType(),
	                new IndexAdvertisement.Instantiator());

		} catch (PeerGroupException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void waitForRendezVousConnection() {

		// Disabling any rendezvous autostart
		if (MyNetworkManager.waitForRendezvousConnection(120000)) {

			Tools.popConnectedRendezvous(NetPeerGroup.getRendezVousService(),name);

		} else {

			Tools.PopInformationMessage(name, "Did not connect to a rendezvous");

		}
	}

	public void stop(){
		MyNetworkManager.stopNetwork();
	}

	public void sendTestAdv(){
		try {
			NetPeerGroup.getDiscoveryService().publish(NetPeerGroup.getPeerAdvertisement());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void discoverAdvertisements(DiscoveryListener discoveryListener) {
		DiscoveryService discoveryService = NetPeerGroup.getDiscoveryService();

		discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 10, discoveryListener);

		System.out.println("Advertisement Discovery sent! Going to sleep for 60 seconds now...");

		// Sleeping for 60 seconds
		Tools.GoToSleep(60000);
	}
	
	public void discoverAdvertisements() {
		discoverAdvertisements(new IndexAdvertisementDiscoverer());
	}
	
	public void discoverPeerGroupAdvertisements(DiscoveryListener discoveryListener) {
		DiscoveryService discoveryService = NetPeerGroup.getDiscoveryService();

		discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 10, discoveryListener);

		System.out.println("PeerGroupAdvertisement Discovery sent! Going to sleep for 60 seconds now...");

		// Sleeping for 60 seconds
		Tools.GoToSleep(60000);
	}

	public void publishIndexAdvertisement() {

		IndexAdvertisement index = (IndexAdvertisement) AdvertisementFactory.newAdvertisement(IndexAdvertisement.getAdvertisementType());

		DiscoveryService discoveryService = NetPeerGroup.getDiscoveryService();

		try {
			discoveryService.publish(index);
			discoveryService.remotePublish(index);
			System.out.println("IndexAdvertiement with ID " + index.getID() + " sent");

		} catch (IOException e) {

			System.err.println("Edge " + name + " could not publish Index Advertisement!");
		}
	}

	@Override
	public void rendezvousEvent(RendezvousEvent rdvE) {
		System.out.println("New rendez-vous id : " + rdvE.getPeerID().toString());
	}
}
