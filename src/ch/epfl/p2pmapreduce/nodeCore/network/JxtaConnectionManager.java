package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

public class JxtaConnectionManager implements IConnectionManager, DiscoveryListener {

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

	public JxtaConnectionManager(String name, int port, PeerID peerID) {
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


	/**
	 * Our Peer is going to start the JXTA network, and try to join
	 * the main DFS Peer Group 
	 */
	@Override
	public void init() {
		try {
			netPeerGroup = networkManager.startNetwork();

			// TODO: Registering our customized advertisement instances
			AdvertisementFactory.registerAdvertisementInstance(IndexAdvertisement.getAdvertisementType(), new IndexAdvertisement.Instantiator());

			// TODO: Join DFS Peer Group.
			if(!connectToRDV(60000)) {
				System.err.println("Unable to connect to " + MAIN_RENDEZ_VOUS_ADDRESS);
				return;
			}

			DiscoveryService discoveryService = netPeerGroup.getDiscoveryService();

			discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, "Name", "RAIDFS", 1, this);
			
			// TODO: Get neighbors from DFS Peer Group?
			DiscoveryService dfsDiscoveryService = dfsPeerGroup.getDiscoveryService();
			
			// Need to an AdvertisementDiscoverer for peers.
			//dfsDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, NetworkConstants.CANDIDATE_SIZE);

			
		} catch(PeerGroupException e) {

			e.printStackTrace();
		} catch(IOException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		System.out.println("Stopping JXTA network now..");
		networkManager.stopNetwork();
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

	@Override
	public void broadcast(Message message) {


	}

	@Override
	public void send(GetIndex getIndex) {
		// TODO: Discover IndexAdvertisment
		DiscoveryService dfsDiscoveryService = dfsPeerGroup.getDiscoveryService();
		
		
		dfsDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 1);

	}

	@Override
	public boolean send(GetChunk getChunk) {
		// TODO Auto-generated method stub
		return false;
	}


	// TODO: To refactor, should not be here.
	
	@Override
	public void discoveryEvent(DiscoveryEvent event) {

		DiscoveryResponseMsg TheDiscoveryResponseMsg = event.getResponse();

		if (TheDiscoveryResponseMsg != null) {

			Enumeration<Advertisement> TheEnumeration = TheDiscoveryResponseMsg.getAdvertisements();


			while (TheEnumeration.hasMoreElements()) {

				try {
					PeerGroupAdvertisement peerGroupAdv = (PeerGroupAdvertisement) TheEnumeration.nextElement();

					System.out.println("PeerGroupAdvertisement from group : " + peerGroupAdv.getName());

					if(peerGroupAdv.getName().equals("RAIDFS")) {

						PeerGroup dfsPeerGroup = netPeerGroup.newGroup(peerGroupAdv);

						if(joinGroup(dfsPeerGroup)) {
							System.out.println("Officialy a member of RAIDFS! You can now list, put, and get files from the DFS");
							this.dfsPeerGroup = dfsPeerGroup;
						} else {
							System.out.println("Could not join group :( Cannot do anything then");
							System.exit(-1);
						}

					}

				} catch (ClassCastException e) {
					// We are not dealing with a Peer Advertisement
					e.printStackTrace();
				} catch (PeerGroupException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}


	private boolean joinGroup(PeerGroup peerGroup) {

		System.out.println("Trying to join PeerGroup " + peerGroup.getPeerGroupName());

		
		StructuredDocument creds = null;

		try {

			// Generate the credentials for the Peer Group
			AuthenticationCredential authCred =
					new AuthenticationCredential( peerGroup.getParentGroup(), null , creds );

			// Get the MembershipService from the peer group
			MembershipService membership = peerGroup.getMembershipService();

			System.out.println(peerGroup.getPeerGroupName() + "membership implementation:\n"+ membership.getClass().getSimpleName());

			// Get the Authenticator from the Authentication creds
			Authenticator auth = membership.apply( authCred );

			// Check if everything is okay to join the group
			if (auth.isReadyForJoin()){
				Credential myCred = membership.join(auth);

				System.out.println("Successfully joined group " +
						peerGroup.getPeerGroupName());

				// display the credential as a plain text document.
				System.out.println("\nCredential: ");
				StructuredTextDocument doc = (StructuredTextDocument)
						myCred.getDocument(new MimeMediaType("text/plain"));

				StringWriter out = new StringWriter();
				doc.sendToWriter(out);
				System.out.println(out.toString());
				out.close();
				return true;
			} else {
				System.out.println("Failure: unable to join group");
				return false;
			}
		} catch (Exception e){
			System.out.println("Failure in authentication.");
			e.printStackTrace();
			return false;
		}
	}

}
