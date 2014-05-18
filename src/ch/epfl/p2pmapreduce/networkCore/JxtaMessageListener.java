package ch.epfl.p2pmapreduce.networkCore;

import java.util.Date;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.endpoint.Message;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.DiscoveryResponseMsg;
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileRemoved;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageDecoder;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageHandler;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

public class JxtaMessageListener implements PipeMsgListener, DiscoveryListener{

	private MessageHandler handler;
	
	private long latestDiscovery;
	
	public JxtaMessageListener(MessageHandler handler) {
		this.handler = handler;
		latestDiscovery = 0;
	}
	
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		
		 // We received a message
        Message received = event.getMessage();
                
        ch.epfl.p2pmapreduce.nodeCore.messages.Message message = MessageDecoder.decode(received);
        
        handler.enqueue(message);
	}

	@Override
	public void discoveryEvent(DiscoveryEvent event) {

		// Who triggered the event?
		DiscoveryResponseMsg responseMsg = event.getResponse();

		if (responseMsg!=null) {

			Enumeration<Advertisement> TheEnumeration = responseMsg.getAdvertisements();

			long minDiscoveryTime = Long.MAX_VALUE;
			
			while (TheEnumeration.hasMoreElements()) {

				try {
					Advertisement adv = TheEnumeration.nextElement();

					if(adv.getClass().equals(PutIndexAdvertisement.class)) {

						PutIndexAdvertisement putAdvertisement = (PutIndexAdvertisement) adv;

						long creationTime = putAdvertisement.getFileCreationTime();
						
						if(creationTime <= latestDiscovery) {
							Date latest = new Date(latestDiscovery);
							
							System.out.println("put advertisement for file " + putAdvertisement.getFileName() + " is too old");
							System.out.println("was created at " + new Date(creationTime));
							System.out.println("only discover after " + latest);
							continue;
						}
						
						if(creationTime > latestDiscovery && creationTime < minDiscoveryTime) {
							minDiscoveryTime = creationTime;
						}
						
						System.out.println("Received " + putAdvertisement.getClass().getSimpleName() + " for file : " + putAdvertisement.getFileName() + " at time " + new Date(putAdvertisement.getFileCreationTime()));

						NewFile newFileMessage = new NewFile(-1, putAdvertisement.getFileSize(), putAdvertisement.getFileName());
						handler.enqueue(newFileMessage);
						
					} else if (adv.getClass().equals(RmIndexAdvertisement.class)) {
						
						RmIndexAdvertisement rmAdvertisement = (RmIndexAdvertisement) adv;
						
						long deletionTime = rmAdvertisement.getFileDeletionTime();
						
						if(deletionTime <= latestDiscovery) {
							Date latest = new Date(latestDiscovery);
							
							System.out.println("put advertisement for file " + rmAdvertisement.getFileName() + " is too old");
							System.out.println("was created at " + new Date(deletionTime));
							System.out.println("only discover after " + latest);
							continue;
						}
						
						if(deletionTime > latestDiscovery && deletionTime < minDiscoveryTime) {
							minDiscoveryTime = deletionTime;
						}
						
						FileRemoved fileRemovedMessage = new FileRemoved(-1, -1, rmAdvertisement.getFileName());
						
						System.out.println("Received RmIndexAdvertisement for file : " + rmAdvertisement.getFileName() + " at time " + new Date(rmAdvertisement.getFileDeletionTime()));	
						
						handler.enqueue(fileRemovedMessage);
						
					} else {
						
//						System.out.println("Received an Advertisement which is neither an IndexAdvertisement nor a PeerGroupAdvertisement..");
//						System.out.println("It is a " + adv.getAdvType());
					}

				} catch (ClassCastException Ex) {

					// We are not dealing with an Index Advertisement
					System.err.println("Cast Error!");

					System.err.println(Ex);
				}

			}
			
			latestDiscovery = minDiscoveryTime;
		}
	}

	
}
