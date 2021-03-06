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
import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilizedAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.network.INeighbourDiscoverer;
import ch.epfl.p2pmapreduce.nodeCore.network.Neighbour;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageHandler;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.UidGenerator;
/**
 * Class implementing the creation of the connexions in order to have a Jxta Network
 *
 */
/**
 * Class implementing the creation of the connexions in order to have a Jxta Network
 *
 */
public class JxtaCommunicator {

	private final static int MAIN_RENDEZVOUS_PORT = 9710;

//	private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://icdatasrv2.epfl.ch:"
//			+ MAIN_RENDEZVOUS_PORT;
	public static String SERVER_ADDRESS = "icdatasrv2.epfl.ch";
	
	 private final static String MAIN_RENDEZ_VOUS_ADDRESS = "tcp://" + SERVER_ADDRESS + ":"
	 + MAIN_RENDEZVOUS_PORT;

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

	private JxtaMessageListener messageListener;
	private PipeAdvertisement pipeAdvertisement = null;
	private Timer pipeAdvertisementPublisher;
	private Timer indexAdvertisementDiscoverer;

	static List<Neighbour> neighbours = new LinkedList<Neighbour>();

	// All the Peer Groups this Peer belongs to.
	// private Set<PeerGroup> peerGroups;

	private static Map<Integer, PipeAdvertisement> peerPipes = new HashMap<Integer, PipeAdvertisement>();

	// Will have a PeerID per PeerGroup.. to rethink

	public JxtaCommunicator(String name, int port) {
		this.name = name;
		this.port = port;
		this.peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID,
				name.getBytes());
		configFile = new File("." + System.getProperty("file.separator") + name);
		NetworkManager.RecursiveDelete(configFile);
		try {
			networkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					name, configFile.toURI());
			networkConfigurator = networkManager.getConfigurator();

			networkConfigurator.clearRendezvousSeeds();

			networkConfigurator.addSeedRendezvous(URI
					.create(MAIN_RENDEZ_VOUS_ADDRESS));
			networkConfigurator.setTcpPort(this.port);
			networkConfigurator.setTcpEnabled(true);
			networkConfigurator.setTcpIncoming(true);
			// netowkrConfigurator.setUseMulticat(false);

			networkConfigurator.setPeerID(this.peerID);
		} catch (IOException e) {

			System.err.println("Exception in instantiating "
					+ this.getClass().getSimpleName());
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * This method tries to:
	 * 
	 * -Start the JXTA network.
	 * 
	 * -Discover and join the "RAIDFS" main PeerGroup of our DFS, that is
	 * created by the RendezVous launched on icdatasrv2.epfl.ch
	 * 
	 * -Create an InputPipe to start listening to messages from Peers in the
	 * PeerGroup. Also publishes this advertisement in order to be discovered by
	 * other peers.
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

		AdvertisementFactory.registerAdvertisementInstance(
				PutIndexAdvertisement.getAdvertisementType(),
				new PutIndexAdvertisement.Instantiator());
		AdvertisementFactory.registerAdvertisementInstance(
				RmIndexAdvertisement.getAdvertisementType(),
				new RmIndexAdvertisement.Instantiator());
		AdvertisementFactory.registerAdvertisementInstance(
				FileStabilizedAdvertisement.getAdvertisementType(), 
				new FileStabilizedAdvertisement.Instantiator());

		// Connect
		if (!connectToRDV(NetworkConstants.RENDEZVOUS_CONNECTION_TIMEOUT)) {
			System.err.println("Unable to connect to "
					+ MAIN_RENDEZ_VOUS_ADDRESS);
			return false;
		}

		// Try to join DFS Peer Group

		/*
		 * DiscoveryService discoveryService =
		 * netPeerGroup.getDiscoveryService();
		 * 
		 * PeerGroupJoiner pgj = new PeerGroupJoiner("RAIDFS", netPeerGroup);
		 * 
		 * discoveryService.getRemoteAdvertisements(null,
		 * DiscoveryService.GROUP, null, null, 1, pgj);
		 * 
		 * try { pgj.wait(DFS_JOIN_TIMEOUT);
		 * 
		 * if(pgj.isJoined("RAIDFS")) { dfsPeerGroup =
		 * pgj.getJoinedGroup("RAIDFS");
		 * 
		 * initMessageListener(dfsPeerGroup);
		 * 
		 * return true; } else { return false; }
		 * 
		 * } catch (InterruptedException e) {
		 * 
		 * System.err.println(
		 * "Interruption while trying to discover and join RAIDFS Peer Group");
		 * e.printStackTrace(); }
		 */

		return true;
	}

	/**
	 * 
	 * Tries to connect with the rendez-vous peers we already should have
	 * specified in the NetworkManager
	 * 
	 * @param timeout
	 *            timeout in milliseconds, a zero timeout of waits forever
	 * 
	 * @return true if we successfuly connected to the rendez-vous, false
	 *         otherwise.
	 */
	private boolean connectToRDV(int timeout) {

		if (networkManager.waitForRendezvousConnection(timeout)) {
			System.out.println("Successfuly connected to rendez-vous");
			return true;
		} else {
			System.err.println("Could not connect to rendez-vous");
			return false;
		}

	}

	/**
	 * Create a PipeAdvertisement, publish it on the PeerGroup passed in
	 * parameter, and create an input pipe that will handle connection and
	 * forward received messages to the Peer abstraction (cf
	 * JxtaMessageListener)
	 * 
	 * @param pg
	 *            the PeerGroup our Advertisement will be published in.
	 */
	public void initMessageListener(MessageHandler mh, final PeerGroup pg) {

		// Instantiating the Pipe Advertisement
		pipeAdvertisement = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		PipeID pipeID = IDFactory.newPipeID(pg.getPeerGroupID(),
				name.getBytes());

		pipeAdvertisement.setPipeID(pipeID);
		pipeAdvertisement.setType(PipeService.UnicastType);
		pipeAdvertisement.setName("pipeAdv:" + this.name);
		pipeAdvertisement.setDescription("Created by " + name);
		
		messageListener = new JxtaMessageListener(mh);

		try {
			pg.getPipeService().createInputPipe(pipeAdvertisement, messageListener);
		} catch (IOException e) {
			System.err
			.println("Did not manage to create input pipe.. Exiting then");
			System.exit(-1);
		}

		pipeAdvertisementPublisher = new Timer();

		pipeAdvertisementPublisher.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				try {
					System.out
					.println("publishing PipeAdvertisement with name "
							+ pipeAdvertisement.getName());
					System.out
					.println("next publishing in "
							+ (NetworkConstants.PIPE_ADVERTISEMENT_LIFETIME - 30 * 1000)
							/ 1000 + " seconds");
					pg.getDiscoveryService().publish(pipeAdvertisement, NetworkConstants.PIPE_ADVERTISEMENT_LIFETIME, 0);
				} catch (IOException e) {
					System.err
					.println("Could not publish PipeAdvertisement! Peers are not going to be able to send us messages then..");
					e.printStackTrace();
				}
			}
		}, 0, NetworkConstants.PIPE_ADVERTISEMENT_LIFETIME - 30 * 1000);

	}

	public void initIndexUpdateDiscovery(MessageHandler handler) {
		indexAdvertisementDiscoverer = new Timer();

		indexAdvertisementDiscoverer.schedule(new TimerTask() {

			@Override
			public void run() {

				System.out.println("discovering index updates");
				netPeerGroup.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.ADV, "MyIdentifierTag" , "*", 10, messageListener);

			}
		}, 0, NetworkConstants.INDEX_ADVERTISEMENT_DISCOVERY_RATE);

	}


	public void stop() {
		networkManager.stopNetwork();
		pipeAdvertisementPublisher.cancel();
	}

	public boolean isStarted() {
		return networkManager.isStarted();
	}

	/**
	 * Send a JXTA-Encoded message to the corresponding JxtaNeighbour using its
	 * PipeAdvertisement, and CURRENTLY the DFSPeerGroup.
	 * 
	 * Note that we might change that part if we create specific PeerGroups per
	 * shared File.
	 * 
	 * @param m
	 *            net.jxta.endpoint.Message to be sent on the OutputPipe
	 * @param neighbour
	 *            neighbour we are sending the message to
	 */
	public void sendMessage(Message m, Neighbour neighbour) {

		PeerGroup pg = netPeerGroup;
		// pg = neighbour.getPeerGroup();

		System.out.println("Sending "
				+ m.getMessageElement("name").toString());

		if (pg != null) {
			PipeService pipeService = pg.getPipeService();
			OutputPipe op = null;
			try {
				op = pipeService.createOutputPipe(peerPipes.get(neighbour.id),
						PIPE_RESOLVING_TIMEOUT);

				while (!op.send(m)) {
					System.out.println("Sending "
							+ m.getMessageElement("name").toString());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						System.err.println("InterruptedException");
//						e.printStackTrace();
					}
				}

				op.close();
			} catch (IOException e) {

				System.err.println("Problem writing the message to the pipe!");
//				e.printStackTrace();
			}

		}
	}

	public boolean publishAdvertisement(Advertisement adv, PeerGroup pg) {

		DiscoveryService discoveryService = pg.getDiscoveryService();

		try {
			discoveryService.publish(adv, 5 * 60 * 1000, 2 * 60 * 1000);
		} catch (IOException e) {

			System.err.println("Could not publish Advertisement "
					+ adv.getAdvType());
//			e.printStackTrace();
			return false;
		}

		return true;
	}

	public PipeAdvertisement getPipeAdvertisement() {
		return pipeAdvertisement;
	}

	public static int getIdForPipeAdv(PipeAdvertisement adv) {

		for (int i : peerPipes.keySet()) {
			if (adv.getName().equals(peerPipes.get(i).getName()))
				return i;
		}

		return -1;
	}

	public static void putPipeAdvertisement(int id, PipeAdvertisement pa) {
		peerPipes.put(id, pa);
		System.out.println("Linking " + id + " to " + pa.getName());
		System.out.println("Size of map is now " + peerPipes.size());

		Neighbour neighbour = new Neighbour(id);
		neighbours.add(neighbour);
	}

	/**
	 * Inner class of JxtaCommunicator, used to discover Peers ( well actually,
	 * their PipeAdvertisements in order to send them messages ).
	 * 
	 * Implements the NeighbourDiscoverer interface.
	 * 
	 * @author Tketa
	 * 
	 */
	public class JxtaNeighbourDiscoverer implements INeighbourDiscoverer,
	DiscoveryListener {

		@Override
		public List<Neighbour> getNeighbors() {

			if (netPeerGroup != null) {

				DiscoveryService discoveryService = netPeerGroup
						.getDiscoveryService();

				neighbours = new LinkedList<Neighbour>();
				// discoveryService.getRemoteAdvertisements(null,
				// DiscoveryService.PEER, null, null,
				// NetworkConstants.CANDIDATE_SIZE);
				// Only one PipeAdvertisement should be returned from each Peer in the DFS.
				System.out.println("Discovering neighbours...");
				discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "pipeAdv:*", 10, this);

				try {
					synchronized (this) {
						this.wait(10 * 1000);
					}
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
//					e.printStackTrace();
				}

				return neighbours;
			} else {
				return null;
			}

		}

		@Override
		public void discoveryEvent(DiscoveryEvent event) {
			// TODO: Handle limited number of neighbours

			// Who triggered the event?
			DiscoveryResponseMsg responseMsg = event.getResponse();

			if (responseMsg != null) {

				Enumeration<Advertisement> TheEnumeration = responseMsg
						.getAdvertisements();

				while (TheEnumeration.hasMoreElements()) {

					try {

						Advertisement adv = TheEnumeration.nextElement();

						// We are only interested in the PipeAdvertisements.
						if (adv.getAdvType().equals(
								PipeAdvertisement.getAdvertisementType())) {

							PipeAdvertisement pipeAdv = (PipeAdvertisement) adv;

							if(pipeAdv.getName().equals(pipeAdvertisement.getName())) {
								continue;
							}

							System.out.println("Found a peer! handling "
									+ pipeAdv.getName());

							// TODO: Test if not discovered already!

							boolean alreadyDiscovered = false;

							int neighbourId = 0;
							for(Integer nId : peerPipes.keySet()) {
								PipeAdvertisement pa = peerPipes.get(nId);
								if(pa != null && pipeAdv.getName().equals(pa.getName())) {
									System.out.println("We know this peer already!");
									alreadyDiscovered = true;
									neighbourId = nId;
								}
							}

							if(! alreadyDiscovered) {

								neighbourId = UidGenerator.freshId();
	
								peerPipes.put(neighbourId, pipeAdv);
								
							}
							
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
