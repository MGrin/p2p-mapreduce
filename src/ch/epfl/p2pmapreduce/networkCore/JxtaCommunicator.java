package ch.epfl.p2pmapreduce.networkCore;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.network.INeighbourDiscoverer;
import ch.epfl.p2pmapreduce.nodeCore.network.JxtaNeighbour;
import ch.epfl.p2pmapreduce.nodeCore.network.Neighbour;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

public class JxtaCommunicator {

	private final static int MAIN_RENDEZVOUS_PORT = 9711;

	private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://icdatasrv2.epfl.ch:" + MAIN_RENDEZVOUS_PORT;

	private final static int PIPE_RESOLVING_TIMEOUT = 30000;
	private final static int DFS_JOIN_TIMEOUT = 60000;

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

	public JxtaCommunicator(String name, int port) {
		this.name = name;
		this.port = port;
		this.peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());
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
	
	/**
	 * This method tries to:
	 * 
	 * -Start the JXTA network.
	 * 
	 * -Discover and join the "RAIDFS" main PeerGroup of our DFS, that is created by the RendezVous launched on icdatasrv2.epfl.ch 
	 * 
	 * -Create an InputPipe to start listening to messages from Peers in the PeerGroup. Also publishes this advertisement in order to be discovered by other peers.
	 * 
	 * 
	 * @return false if any of those steps failed, true otherwise.
	 */

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

		// TODO: Connect and try to join DFS Peer Group.
		if(!connectToRDV(60000)) {
			System.err.println("Unable to connect to " + MAIN_RENDEZ_VOUS_ADDRESS);
			return false;
		}

		DiscoveryService discoveryService = netPeerGroup.getDiscoveryService();

		PeerGroupJoiner pgj = new PeerGroupJoiner("RAIDFS", netPeerGroup);

		discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 1, pgj);

		try {
			pgj.wait(DFS_JOIN_TIMEOUT);

			if(pgj.isJoined("RAIDFS")) {
				dfsPeerGroup = pgj.getJoinedGroup("RAIDFS");
				
				initMessageListener();
				
				return true;
			} else {
				return false;
			}

		} catch (InterruptedException e) {
			
			System.err.println("Interruption while trying to discover and join RAIDFS Peer Group");
			e.printStackTrace();
		}


		return false;
	}

	/**
	 * 
	 * Tries to connect with the rendez-vous peers we already should have specified in the NetworkManager
	 * 
	 * @param timeout timeout in milliseconds, a zero timeout of waits forever 

	 * @return true if we successfuly connected to the rendez-vous, false otherwise.
	 */
	private boolean connectToRDV(int timeout) {

		if(networkManager.waitForRendezvousConnection(timeout)) {
			System.out.println("Successfuly connected to rendez-vous");
			return true;
		} else {
			System.err.println("Could not connect to rendez-vous");
			return false;
		}

	}
	
	/**
	 * Create a PipeAdvertisement, publish it on the DFS PeerGroup, and create an input pipe that will handle connection and
	 * forward received messages to the Peer abstraction (cf JxtaMessageListener)
	 * 
	 * TODO: This method is currently being called after we join the RAIDFS PeerGroup, but it might be moved after we discover the Index.
	 * 
	 */
	private void initMessageListener() {
		
		 // Creating a Pipe Advertisement
        PipeAdvertisement pipeAdvertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        PipeID pipeID = IDFactory.newPipeID(dfsPeerGroup.getPeerGroupID(), name.getBytes());

        pipeAdvertisement.setPipeID(pipeID);
        pipeAdvertisement.setType(PipeService.UnicastType);
        pipeAdvertisement.setName("Incoming Pipe for " + this.name);
        pipeAdvertisement.setDescription("Created by " + name);
        
        try {
        	// TODO: May have to start Thread to re-publish PipeAdvertisement! Otherwise it expires.
			dfsPeerGroup.getDiscoveryService().publish(pipeAdvertisement);
			
			dfsPeerGroup.getPipeService().createInputPipe(pipeAdvertisement, Peer.getMessageListener());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isStarted() {
		return networkManager.isStarted();
	}

	/**
	 * Send a JXTA-Encoded message to the corresponding JxtaNeighbour using its PipeAdvertisement,
	 * and CURRENTLY the DFSPeerGroup.
	 * 
	 * Note that we might change that part if we create specific PeerGroups per shared File.
	 * 
	 * @param m net.jxta.endpoint.Message to be sent on the OutputPipe
	 * @param neighbour neighbour we are sending the message to
	 */
	public void sendMessage(Message m, JxtaNeighbour neighbour) {

		PeerGroup pg = dfsPeerGroup;
		//pg = neighbour.getPeerGroup();

		if(pg != null) {
			PipeService pipeService = pg.getPipeService();
			OutputPipe op = null;
			try {
				op = pipeService.createOutputPipe(neighbour.getPipeAdvertisement(), PIPE_RESOLVING_TIMEOUT);

				op.send(m);
			} catch (IOException e) {

				System.err.println("Problem writing the message to the pipe!");
				e.printStackTrace();
			}


		}

	}

	/**
	 * Inner class of JxtaCommunicator, used to discover Peers ( well actually, their PipeAdvertisements 
	 * in order to send them messages ).
	 * 
	 * Implements the NeighbourDiscoverer interface.
	 * 
	 * @author Tketa
	 *
	 */
	public class JxtaNeighbourDiscoverer implements INeighbourDiscoverer, DiscoveryListener {

		List<Neighbour> neighbours = new LinkedList<Neighbour>();

		@Override
		public List<Neighbour> getNeighbors() {

			if(dfsPeerGroup != null) {

				DiscoveryService discoveryService = dfsPeerGroup.getDiscoveryService();

				neighbours.clear();
				//discoveryService.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, NetworkConstants.CANDIDATE_SIZE);

				// Only one PipeAdvertisement should be returned from each Peer in the DFS.
				discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 1);

				try {
					wait();
				} catch (InterruptedException e) {

					System.err.println("Wait for Neighbour Discovery interrupted!");
					e.printStackTrace();
				}

				return neighbours;
			} else {
				return null;
			}

		}

		@Override
		public void discoveryEvent(DiscoveryEvent event) {

			if(neighbours.size() >= NetworkConstants.CANDIDATE_SIZE) {
				notify();
				return;
			}

			// Who triggered the event?
			DiscoveryResponseMsg responseMsg = event.getResponse();

			if (responseMsg!=null) {

				Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();


				while (TheEnumeration.hasMoreElements()) {

					try {

						Advertisement adv = TheEnumeration.nextElement();

						// We are only interested in the PipeAdvertisements.
						if(adv.getAdvType().equals(PipeAdvertisement.getAdvertisementType())) {

							PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;

							JxtaNeighbour neighbour = new JxtaNeighbour(pipeAdv.hashCode(), pipeAdv);

							neighbours.add(neighbour);
						}


					} catch (ClassCastException Ex) {

						// We are not dealing with a PipeAdvertisement
						System.err.println("Cast Error!");
						System.err.println(Ex);
					}

				}
			}


		}

	}


}