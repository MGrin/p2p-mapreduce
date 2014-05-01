package ch.epfl.p2pmapreduce.edge;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import Examples.Z_Tools_And_Others.Tools;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.peergroup.FilePeerGroup;

public class PeerGroupJoiner extends Thread {

	public final static String courseRendezVousAddress = "tcp://icdatasrv2.epfl.ch:9711";
	private PeerGroup dfsPeerGroup = null;

	

	private Map<String, FilePeerGroup> fileGroups = new HashMap<String, FilePeerGroup>();

	private Edge edge;

	public PeerGroupJoiner(String name, int port, PeerGroupID dfsID) {

		PeerID id = IDFactory.newPeerID(dfsID, name.getBytes());

		this.edge = new Edge(name, port, id);
	}

	public static void main(String[] args) {
		PeerGroupJoiner p = new PeerGroupJoiner("PEER TEST", 8383, PeerGroupID.defaultNetPeerGroupID);

		
		p.start();
	}

	@Override
	public void run() {
		super.run();

		edge.start();

		//while(edge.isActive()) {

			Tools.PopInformationMessage("PEER TEST", "Entered the loop. Going to discover and Join Peer Groups");

			edge.discoverAdvertisements(new PeerGroupJoiner.DFSPeerGroupAdvertisementDiscoverer());

			Tools.PopInformationMessage("PEER TEST", "Going to stop JXTA Network once you press \"Enter\" ");
			edge.stop();
		//}


	}


	public class DFSPeerGroupAdvertisementDiscoverer implements DiscoveryListener {


		@Override
		public void discoveryEvent(DiscoveryEvent event) {

			// Who triggered the event?
			DiscoveryResponseMsg responseMsg = event.getResponse();

			if (responseMsg!=null) {

				Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();

				while (TheEnumeration.hasMoreElements()) {
					try {
						Advertisement adv = TheEnumeration.nextElement();

						// It can only be the PeerGroup for the DFS because FilePeerGroupAdvertisement is a CustomAdvertisement.
						PeerGroupAdvertisement pga = (PeerGroupAdvertisement) adv;
						dfsPeerGroup = edge.NetPeerGroup.newGroup(pga);

						joinGroup(dfsPeerGroup);

				} catch (ClassCastException Ex) {
					// We are not dealing with an Index Advertisement
					System.err.println("Cast Error! Not a PeerGroupAdvertisement we received..");

					System.err.println(Ex);
				} catch (PeerGroupException e) {
					// TODO Auto-generated catch block
					System.err.println("ERRR Cannot join DFS Peer Group :(");
					e.printStackTrace();
				}

			}
		}
	}

	private void joinGroup(PeerGroup pg) {
		System.out.println("Joining peer group...");

		StructuredDocument creds = null;

		try {
			// Generate the credentials for the Peer Group
			AuthenticationCredential authCred =
					new AuthenticationCredential( pg, null , creds );

			// Get the MembershipService from the peer group
			MembershipService membership = pg.getMembershipService();
			
			System.out.println(pg.getPeerGroupName() + " membership implementation:\n"+ membership.getClass().getSimpleName());
			
			// Get the Authenticator from the Authentication creds
			Authenticator auth = membership.apply( authCred );

			// Check if everything is okay to join the group
			if (auth.isReadyForJoin()){
				Credential myCred = membership.join(auth);

				System.out.println("Successfully joined group " +
						pg.getPeerGroupName());

				// display the credential as a plain text document.
				System.out.println("\nCredential: ");
				StructuredTextDocument doc = (StructuredTextDocument)
						myCred.getDocument(new MimeMediaType("text/plain"));

				StringWriter out = new StringWriter();
				doc.sendToWriter(out);
				System.out.println(out.toString());
				out.close();
			} else {
				System.out.println("Failure: unable to join group");
				
			}
		}
		catch (Exception e){
			System.out.println("Failure in authentication.");
			e.printStackTrace();
		}
	}
}
}
