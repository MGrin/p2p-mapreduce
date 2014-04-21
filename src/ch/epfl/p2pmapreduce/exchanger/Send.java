package ch.epfl.p2pmapreduce.exchanger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import ch.epfl.p2pmapreduce.index.Metadata;


public class Send {
	private PipeID id;
	private PipeService pipeService;
	
	public void connect() {
		send(null, "peersId", "CONNECT");
	}
	
	public void put(String localFileName, String DFSFileName) {
		File file = new File(localFileName);
		
		if (file.exists()) {
			long fileSize = file.length()/1024; //bytes to kb 
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			String fileDate = sdf.format(file.lastModified());
			String infos = DFSFileName + "," + fileSize + "," + fileDate;

			Metadata.metaPut(infos);

			//envoyer message avec infos
			send(infos, "peersId", "PUT");
		} else {
			System.out.println("File " + localFileName + " doesn't exist.");
		}
	}
	

	public void rm(String fileName, boolean directory) {
		//Metadata.check(filename, directory);
		Metadata.metaRm(fileName);
		//envoyer message avec infos
		send(infos, "peersId", "RM");
	}
	
	public void send(String infos, String peersId, String type) {
		id = getId();
		PipeAdvertisement adv = getAdvertisement(id, true);
		Set<PeerID> peers = new HashSet<PeerID>();
		Message message = null;
		
		//send message to all the peersId we want
		try {
			peers.add((PeerID) IDFactory.fromURI(new URI(peersId)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		OutputPipe sender = null;
		try {
			sender = pipeService.createOutputPipe(adv, peers, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> data = tokenize(infos, ",");
		
		if (type.compareTo("PUT") == 0) {
			message = new Put("PUT");
			MessageElement name = new StringMessageElement("name", data.get(0), null);
			MessageElement size = new StringMessageElement("size", data.get(1), null);
			MessageElement date = new StringMessageElement("date", data.get(2), null);
			message.addMessageElement(name);
			message.addMessageElement(size);
			message.addMessageElement(date);
		} else if (type.compareTo("CONNECT") == 0) {
			message = new Connect("CONNECT");
		} else if (type.compareTo("RM") == 0) {
			message = new Rm("RM");
			MessageElement name = new StringMessageElement("name", data.get(0), null);
			message.addMessageElement(name);
		}
		
		
		try {
			if (sender != null && message != null) {
				sender.send(message);
			} else {
				throw new Exception("outputPipe = null");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private PipeAdvertisement getAdvertisement(PipeID id, boolean isMulticast) {
		PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		
		adv.setPipeID(id);
		adv.setName("newAd");
		adv.setDescription("");
		if (isMulticast) {
			adv.setType(PipeService.PropagateType);
		} else {
			adv.setType(PipeService.UnicastType);
		}
		
		return adv;
	}
	
	
	//UTILITY FUNCTIONS
	public static List<String> tokenize(String input, String delim) {
		List<String> output = null;
		
		if (input != null) {
			output = new ArrayList<String>();
			
			StringTokenizer tok = new StringTokenizer(input, delim);
			while(tok.hasMoreTokens()) {
				 output.add(tok.nextToken());
			}
		}
		
		return output;
	}
	
	public static PipeID getId() {
		return (PipeID) PipeID.nullID;
	}
}

