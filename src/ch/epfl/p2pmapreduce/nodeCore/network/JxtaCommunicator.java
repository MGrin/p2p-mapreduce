package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.networkCore.communication.PeerGroupJoiner;

public class JxtaCommunicator {

	private final static int MAIN_RENDEZVOUS_PORT = 9711;

	private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://icdatasrv2.epfl.ch:" + MAIN_RENDEZVOUS_PORT;

	private String name;
	private int port;
	private PeerID peerID;
	private File configFile;

	private NetworkManager networkManager;
	private NetworkConfigurator networkConfigurator;
	private PeerGroup netPeerGroup;
	private PeerGroup dfsPeerGroup = null;

	//All the Peer Groups this Peer belongs to.
	private Set<PeerGroup> peerGroups;

	
	// Will have a PeerID per PeerGroup.. to rethink

	public JxtaCommunicator(String name, int port, PeerID peerID) {
		this.name = name;
		this.port = port;
		this.peerID = peerID;
		configFile = new File("." + System.getProperty("file.separator") + name);
		try {
			networkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, name, configFile.toURI());
			networkConfigurator = networkManager.getConfigurator();

			networkConfigurator.clearRendezvousSeeds();

			networkConfigurator.addSeedRendezvous(URI.create(MAIN_RENDEZ_VOUS_ADDRESS));
			networkConfigurator.setTcpPort(this.port);
			networkConfigurator.setTcpEnabled(true);
			networkConfigurator.setTcpIncoming(true);
			//netowkrConfigurator.setUseMulticat(false);

			networkConfigurator.setPeerID(this.peerID);
		} catch(IOException e) {

			System.err.println("Exception in instantiating " + this.getClass().getSimpleName());
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	public boolean start() {
		try {
			netPeerGroup = networkManager.startNetwork();
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// TODO: Registering our customized advertisement instances
		AdvertisementFactory.registerAdvertisementInstance(IndexAdvertisement.getAdvertisementType(), new IndexAdvertisement.Instantiator());

		// TODO: Join DFS Peer Group.
		if(!connectToRDV(60000)) {
			System.err.println("Unable to connect to " + MAIN_RENDEZ_VOUS_ADDRESS);
			return false;
		}

		DiscoveryService discoveryService = netPeerGroup.getDiscoveryService();

		//discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, "Name", "RAIDFS", 1, this);
		return false;
	}
	
	/**
	 * 
	 * @param timeout timeout in milliseconds, a zero timeout of waits forever 

	 * @return true if we successfuly connected to the rendez-vous, false otherwise.
	 */
	public boolean connectToRDV(int timeout) {

		if(networkManager.waitForRendezvousConnection(timeout)) {
			System.out.println("Successfuly connected to rendez-vous");
			return true;
		} else {
			System.err.println("Could not connect to rendez-vous");
			return false;
		}

	}
	
	public void discoverAdvertisement(Advertisement adv, PeerGroup pg) {
		DiscoveryService discoveryService = pg.getDiscoveryService();
		
		
	}
	
	
	
}
