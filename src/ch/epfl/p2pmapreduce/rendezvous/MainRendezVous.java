package ch.epfl.p2pmapreduce.rendezvous;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import Examples.Z_Tools_And_Others.Tools;
import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.impl.content.ContentServiceImpl;
import net.jxta.impl.peergroup.CompatibilityUtils;
import net.jxta.impl.peergroup.StdPeerGroup;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.Module;
import net.jxta.protocol.ModuleImplAdvertisement;


public class MainRendezVous {

	public static final String CENTRAL_NAME = "Central Seed";
	public static final int PORT = 9711;

	public static final String PeerGroupName = "RAIDFS";
	public static final PeerGroupID CustPeerGroupID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, PeerGroupName.getBytes());

	private static HashMap<String, RendezVous> seeds = new HashMap<String, RendezVous>();

	public static void main(String[] args) {		
		CentralRendezVous centralSeed = new CentralRendezVous(CENTRAL_NAME, 
				PORT, IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, CENTRAL_NAME.getBytes()));
		seeds.put(CENTRAL_NAME, centralSeed);

		centralSeed.start();
		Tools.PopInformationMessage("Central seed", "Waiting for other peers to connect");



		// Creating a child group with PSE
		PeerGroup ChildPeerGroup = null;
		try {
			ChildPeerGroup = centralSeed.NetPeerGroup.newGroup(
					CustPeerGroupID,
					createAllPurposePeerGroupImplAdv(),
					PeerGroupName,
					"Custom peergroup..."
					);


		if (Module.START_OK != ChildPeerGroup.startApp(new String[0]))
			System.err.println("Cannot start custom peergroup");
		
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//MyNetPeerGroup.newGroup(ChildPeerGroup.getPeerGroupAdvertisement());

		// Checking membership implementation
		MembershipService ChildGroupMembership = ChildPeerGroup.getMembershipService();

		Tools.PopInformationMessage(CENTRAL_NAME
				, "Custom group membership implementation:\n"
						+ ChildGroupMembership.getClass().getSimpleName());

		// Stopping the network
		Tools.PopInformationMessage(CENTRAL_NAME, "Stop the JXTA network");
		centralSeed.stop();

}

public static ModuleImplAdvertisement createAllPurposePeerGroupImplAdv() {

	ModuleImplAdvertisement implAdv = CompatibilityUtils.createModuleImplAdvertisement(
			PeerGroup.allPurposePeerGroupSpecID, StdPeerGroup.class.getName(),
			"General Purpose Peer Group");

	// Create the service list for the group.
	StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv();

	// set the services
	paramAdv.addService(PeerGroup.endpointClassID, PeerGroup.refEndpointSpecID);
	paramAdv.addService(PeerGroup.resolverClassID, PeerGroup.refResolverSpecID);
	paramAdv.addService(PeerGroup.membershipClassID, PeerGroup.refMembershipSpecID);
	paramAdv.addService(PeerGroup.accessClassID, PeerGroup.refAccessSpecID);

	// standard services
	paramAdv.addService(PeerGroup.discoveryClassID, PeerGroup.refDiscoverySpecID);
	paramAdv.addService(PeerGroup.rendezvousClassID, PeerGroup.refRendezvousSpecID);
	paramAdv.addService(PeerGroup.pipeClassID, PeerGroup.refPipeSpecID);
	paramAdv.addService(PeerGroup.peerinfoClassID, PeerGroup.refPeerinfoSpecID);

	paramAdv.addService(PeerGroup.contentClassID, ContentServiceImpl.MODULE_SPEC_ID);

	// Insert the newParamAdv in implAdv
	XMLElement paramElement = (XMLElement) paramAdv.getDocument(MimeMediaType.XMLUTF8);
	implAdv.setParam(paramElement);

	return implAdv;

}

public static String getAddress(){
	try {
		return "tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + PORT;
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
}
}
