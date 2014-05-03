package ch.epfl.p2pmapreduce.advertisement;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;

public class IndexAdvertisementDiscoverer implements DiscoveryListener {

	@Override
	public void discoveryEvent(DiscoveryEvent event) {

		// Who triggered the event?
		DiscoveryResponseMsg responseMsg = event.getResponse();

		if (responseMsg!=null) {

			Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();


			while (TheEnumeration.hasMoreElements()) {

				try {
					Advertisement adv = TheEnumeration.nextElement();

					if(adv.getClass().equals(IndexAdvertisement.class)) {

						IndexAdvertisement index = (IndexAdvertisement) adv;

						System.out.println("Received index advertisement with id : " + index.getID());
						System.out.println("Displaying IndexAdvertisement...");
						System.out.println(index);

					} else if (adv.getClass().equals(PeerGroupAdvertisement.class)) {
						
						PeerGroupAdvertisement pga = (PeerGroupAdvertisement) adv;
						
						System.out.println("Received PeerGroupAdvertisement of PeerGroup with name : " + pga.getName());						
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
