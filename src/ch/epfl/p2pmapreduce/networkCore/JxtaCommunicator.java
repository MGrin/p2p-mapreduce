package ch.epfl.p2pmapreduce.networkCore;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import Examples.Z_Tools_And_Others.Tools;
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
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.network.INeighbourDiscoverer;
import ch.epfl.p2pmapreduce.nodeCore.network.Neighbour;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageHandler;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.UidGenerator;

public class JxtaCommunicator {

	private final static int MAIN_RENDEZVOUS_PORT = 9710;

	//private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://icdatasrv2.epfl.ch:" + MAIN_RENDEZVOUS_PORT;
	private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://localhost:" + MAIN_RENDEZVOUS_PORT;


	private final static int PIPE_RESOLVING_TIMEOUT = 30000;
	private final static int DFS_JOIN_TIMEOUT = 60000;

	private String name;
	private int port;
	private PeerID peerID;
	private File configFile;

	private NetworkManager networkManager;
	private NetworkConfigurator networkConfigurator;
	public PeerGroup netPeerGroup;
	private PeerGroup dfsPeerGroup;

	private PipeAdvertisement pipeAdvertisement = null;
	private Timer pipeAdvertisementPublisher;
	private Timer indexAdvertisementDiscoverer;

	//All the Peer Groups this Peer belongs to.
	//private Set<PeerGroup> peerGroups;

	private static Map<Integer, PipeAdvertisement> peerPipes = new HashMap<Integer, PipeAdvertisement>();

	// Will have a PeerID per PeerGroup.. to rethink

	public JxtaCommunicator(String name, int port) {
		this.name = name;
		this.port = port;
		this.peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());
		configFile = new File("." + System.getProperty("file.separator") + name);
		NetworkManager.RecursiveDelete(configFile);
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

			e.printStackTrace();
			return false;
		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}

		AdvertisementFactory.registerAdvertisementInstance(PutIndexAdvertisement.getAdvertisementType(), new PutIndexAdvertisement.Instantiator());
		AdvertisementFactory.registerAdvertisementInstance(RmIndexAdvertisement.getAdvertisementType(), new RmIndexAdvertisement.Instantiator());


		// Connect
		if(!connectToRDV(20000)) { // TODO Change to 60000
			System.err.println("Unable to connect to " + MAIN_RENDEZ_VOUS_ADDRESS);
			return false;
		}

		// Try to join DFS Peer Group

		/*
		DiscoveryService discoveryService = netPeerGroup.getDiscoveryService();

		PeerGroupJoiner pgj = new PeerGroupJoiner("RAIDFS", netPeerGroup);

		discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 1, pgj);

		try {
			pgj.wait(DFS_JOIN_TIMEOUT);

			if(pgj.isJoined("RAIDFS")) {
				dfsPeerGroup = pgj.getJoinedGroup("RAIDFS");

				initMessageListener(dfsPeerGroup);

				return true;
			} else {
				return false;
			}

		} catch (InterruptedException e) {

			System.err.println("Interruption while trying to discover and join RAIDFS Peer Group");
			e.printStackTrace();
		}
		 */


		return true;
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
	 * Create a PipeAdvertisement, publish it on the PeerGroup passed in parameter, and create an input pipe that will handle connection and
	 * forward received messages to the Peer abstraction (cf JxtaMessageListener)
	 * 
	 * @param pg the PeerGroup our Advertisement will be published in.
	 */
	public void initMessageListener(MessageHandler mh, final PeerGroup pg) {

		// Instantiating the Pipe Advertisement
		pipeAdvertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		
		PipeID pipeID = IDFactory.newPipeID(pg.getPeerGroupID(), name.getBytes());

		pipeAdvertisement.setPipeID(pipeID);
		pipeAdvertisement.setType(PipeService.UnicastType);
		pipeAdvertisement.setName("pipeAdv:" + this.name);
		pipeAdvertisement.setDescription("Created by " + name);

		pipeAdvertisementPublisher = new Timer();

		pipeAdvertisementPublisher.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				try {
					System.out.println("publishing PipeAdvertisement with name " + pipeAdvertisement.getName());
					pg.getDiscoveryService().publish(pipeAdvertisement);
				} catch (IOException e) {
					System.err.println("Could not publish PipeAdvertisement! Peers are not going to be able to send us messages then..");
					e.printStackTrace();
				}
			}
		}, 0, NetworkConstants.PIPE_ADVERTISEMENT_LIFETIME - 30 * 1000);

		final JxtaMessageListener listener = new JxtaMessageListener(mh);

		indexAdvertisementDiscoverer = new Timer();

		indexAdvertisementDiscoverer.schedule(new TimerTask() {

			@Override
			public void run() {

				System.out.println("discovering index updates");
				pg.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 10, listener);

			}
		}, 0, NetworkConstants.INDEX_ADVERTISEMENT_DISCOVERY_RATE);


		try {
			pg.getPipeService().createInputPipe(pipeAdvertisement, listener);
		} catch(IOException e) {
			System.err.println("Did not manage to create input pipe.. Exiting then");
			System.exit(-1);
		}
	}

	public void stop() {
		networkManager.stopNetwork();
		pipeAdvertisementPublisher.cancel();
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
	public void sendMessage(Message m, Neighbour neighbour) {

		PeerGroup pg = netPeerGroup;
		//pg = neighbour.getPeerGroup();

		if(pg != null) {
			PipeService pipeService = pg.getPipeService();
			OutputPipe op = null;
			try {
				op = pipeService.createOutputPipe(peerPipes.get(neighbour.id) , PIPE_RESOLVING_TIMEOUT);

				op.send(m);

				op.close();
			} catch (IOException e) {

				System.err.println("Problem writing the message to the pipe!");
				e.printStackTrace();
			}


		}
	}

	public boolean publishAdvertisement(Advertisement adv, PeerGroup pg) {

		DiscoveryService discoveryService = pg.getDiscoveryService();

		try {
			discoveryService.publish(adv, 5 * 60 * 1000, 2 * 60 * 1000 );
		} catch (IOException e) {

			System.err.println("Could not publish Advertisement " + adv.getAdvType());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public PipeAdvertisement getPipeAdvertisement() {
		return pipeAdvertisement;
	}

	public static int getIdForPipeAdv(PipeAdvertisement adv) {

		for(int i : peerPipes.keySet()) {
			if(adv.equals( peerPipes.get(i) )) return i;
		}

		return -1;
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

			if(netPeerGroup != null) {

				DiscoveryService discoveryService = netPeerGroup.getDiscoveryService();

				neighbours = new LinkedList<Neighbour>();
				//discoveryService.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, NetworkConstants.CANDIDATE_SIZE);

				// Only one PipeAdvertisement should be returned from each Peer in the DFS.
				System.out.println("Discovering..");
				discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "pipeAdv:*", 10, this);

				try {
					synchronized(this) {
						this.wait(20 * 1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return neighbours;
			} else {
				return null;
			}

		}

		@Override
		public void discoveryEvent(DiscoveryEvent event) {

			//TODO: Handle limited number of neighbours

			System.out.println("Advertisement discovered!");


			// Who triggered the event?
			DiscoveryResponseMsg responseMsg = event.getResponse();

			if (responseMsg!=null) {

				Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();


				while (TheEnumeration.hasMoreElements()) {

					try {

						Advertisement adv = TheEnumeration.nextElement();

						System.out.println("Discovered Advertisement is a " + adv.getAdvType());

						// We are only interested in the PipeAdvertisements.
						if(adv.getAdvType().equals(PipeAdvertisement.getAdvertisementType())) {

							System.out.println("We identified the PipeAdvertisement and handle it");
							PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;

							int neighbourId = UidGenerator.freshId();

							peerPipes.put(neighbourId, pipeAdv);

							Neighbour neighbour = new Neighbour(neighbourId);

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
