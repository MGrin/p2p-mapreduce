package ch.epfl.p2pmapreduce.networkCore.communication;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;


/**
 * This class should only be passed as a parameter for discovering PeerGroupAdvertisements.
 * 
 * It might be used to implement secured joining of RAIDFS Peer Group.
 * 
 * @author Tketa
 *
 */
public class PeerGroupJoiner implements DiscoveryListener {

	private String joinRegex;
	private PeerGroup parentPeerGroup;
	
	private Set<String> joinedGroups;

	/**
	 * @param joinRegex regex for the names of the PeerGroups we will join
	 */
	public PeerGroupJoiner(String joinRegex, PeerGroup parent) {
		this.joinRegex = joinRegex;
		this.parentPeerGroup = parent;
		
		joinedGroups = new HashSet<String>();
	}
	
	public boolean isJoined(PeerGroup pg) {
		return joinedGroups.contains(pg.getPeerGroupName());
	}

	@Override
	public void discoveryEvent(DiscoveryEvent event) {

		// Who triggered the event?
		DiscoveryResponseMsg responseMsg = event.getResponse();

		if (responseMsg!=null) {

			Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();


			while (TheEnumeration.hasMoreElements()) {

				try {

					Advertisement adv = TheEnumeration.nextElement();

					PeerGroupAdvertisement pga = (PeerGroupAdvertisement) TheEnumeration.nextElement();

					if(pga.getName().matches(joinRegex)) {
						PeerGroup pg = parentPeerGroup.newGroup(pga);
						
						
						if(joinGroup(pg)) {
							joinedGroups.add(pg.getPeerGroupName());
						}
					}
					

				} catch (ClassCastException Ex) {

					// We are not dealing with an PeerGroupAdvertisement
					System.err.println("Cast Error!");
					System.err.println(Ex);
				} catch (PeerGroupException e) {

					System.err.println("PeerGroupException.." + e.getMessage());
					e.printStackTrace();
				}

			}
		}
	}

	public static boolean joinGroup(PeerGroup peerGroup) {

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