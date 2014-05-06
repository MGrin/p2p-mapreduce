package ch.epfl.p2pmapreduce.networkCore;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.endpoint.Message;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.IndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageDecoder;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;

public class JxtaMessageListener implements PipeMsgListener, DiscoveryListener{

	private Peer p;
	
	public JxtaMessageListener(Peer p) {
		this.p = p;
	}
	
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		
		 // We received a message
        Message received = event.getMessage();
                
        ch.epfl.p2pmapreduce.nodeCore.messages.Message message = MessageDecoder.decode(received);
        
        p.enqueue(message);
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

					if(adv.getClass().equals(PutIndexAdvertisement.class)) {

						PutIndexAdvertisement putAdvertisement = (PutIndexAdvertisement) adv;

						System.out.println("Received " + putAdvertisement.getClass().getSimpleName() + " with id : " + putAdvertisement.getID());

					} else if (adv.getClass().equals(RmIndexAdvertisement.class)) {
						
						RmIndexAdvertisement rmAdvertisement = (RmIndexAdvertisement) adv;
						
						System.out.println("Received RmIndexAdvertisement with id : " + rmAdvertisement.getID());						
					} else {
						
						System.out.println("Received an Advertisement which is neither an IndexAdvertisement nor a PeerGroupAdvertisement..");
						System.out.println("It is a " + adv.getAdvType());
					}

				} catch (ClassCastException Ex) {

					// We are not dealing with an Index Advertisement
					System.err.println("Cast Error!");

					System.err.println(Ex);
				}

			}
		}
	}

	
}
