package ch.epfl.p2pmapreduce.edge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import Examples.D_Discovering_Resources.Tebogo_Jazz_Fan;
import Examples.Z_Tools_And_Others.Tools;
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
import ch.epfl.p2pmapreduce.rendezvous.MainRendezVous;

public class Edge implements RendezvousListener, DiscoveryListener {

	public static final String getMainRendezVousIP = MainRendezVous.getAddress();
	
	protected String name;
	protected int port;
	protected PeerID peerID;
	protected File configFile;
	
	public NetworkManager MyNetworkManager;
	public NetworkConfigurator MyNetworkConfigurator;
	public PeerGroup NetPeerGroup;
	
	public Edge() {
		//dummy object
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Rendez vous peer is : " + getMainRendezVousIP);
	}
	
	public void start(){
		try {
			
			NetPeerGroup = MyNetworkManager.startNetwork();
			//NetPeerGroup.getRendezVousService().setAutoStart(false);
			//NetPeerGroup.getRendezVousService().addListener(this);
			//NetPeerGroup.getDiscoveryService().addDiscoveryListener(this);
			
		} catch (PeerGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void discoverPgAdvertisement() {
		
		DiscoveryService ds = NetPeerGroup.getDiscoveryService();
		
		ds.getRemoteAdvertisements(null, DiscoveryService.GROUP, "Name", "*" , 5, new Edge());
		
		System.out.println("Going to sleep after sending the discovery.........");
		 // Sleeping for 60 seconds
        Tools.GoToSleep(60000);
	}

	@Override
	public void rendezvousEvent(RendezvousEvent rdvE) {
		System.out.println("New rendevouz id : "+rdvE.getPeerID().toString());
	}

	@Override
	public void discoveryEvent(DiscoveryEvent TheDiscoveryEvent) {
		
		System.out.println("DISCOVERY");
		
		 // Who triggered the event?
        DiscoveryResponseMsg TheDiscoveryResponseMsg = TheDiscoveryEvent.getResponse();
        
        if (TheDiscoveryResponseMsg!=null) {
            
            Enumeration<Advertisement> TheEnumeration = TheDiscoveryResponseMsg.getAdvertisements();
            
            
            while (TheEnumeration.hasMoreElements()) {
                
                try {
                    
                    PeerGroupAdvertisement ThePeer = (PeerGroupAdvertisement) TheEnumeration.nextElement();
                    
                    System.out.println("PEER GROUP ADVERTISEMENT FROM : " + ThePeer.getName());
                   
                    
                    
                    //Tools.PopInformationMessage(Name, "Received advertisement of: " + ThePeer.getName());
                    
                } catch (ClassCastException Ex) {
                    
                    // We are not dealing with a Peer Advertisement
                	//System.out.println("Not a PeerAdvertisement! It is a " + Ex.getMessage());
                    
                }
                
            }
                        
        }
	}
}
