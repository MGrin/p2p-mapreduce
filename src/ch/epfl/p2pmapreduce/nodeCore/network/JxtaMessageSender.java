package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.MessageElement;
import net.jxta.peer.PeerID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.PutIndex;
import ch.epfl.p2pmapreduce.advertisement.RmIndex;
import ch.epfl.p2pmapreduce.exchanger.All;
import ch.epfl.p2pmapreduce.exchanger.Connect;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

public class JxtaMessageSender implements IMessageSender{

	private JxtaCommunicator communicator;
	private static PipeService pipeService;
	
	@Override
	public boolean send(GetChunkfield message, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(SendChunkfield message, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(GetChunk getChunk, Neighbour receiver) {

		return false;
	}

	@Override
	public boolean send(SendChunk sendChunk, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(SendIndex sendIndex, Neighbour receiver) {
		OutputPipe pipe = createPipe(receiver);
		
		All all = new All(sendIndex);
		if (pipe != null) {
			try {
				byte[] array = metaFile(Metadata.file);
				MessageElement file = new ByteArrayMessageElement("data", MimeMediaType.XML_DEFAULTENCODING, array, null);
				all.addMessageElement(file);
				pipe.send(all);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Override
	public boolean send(GetIndex getIndex, Neighbour receiver) {
		OutputPipe pipe = createPipe(receiver);
		
		Connect message = new Connect(getIndex);
		
		if (pipe != null) {
			try {
				pipe.send(message);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Override
	public boolean send(PutIndex putIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(RmIndex rmIndex) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	//UTILS
	
	public OutputPipe createPipe(Neighbour neighbour){
		OutputPipe pipe = null;
		//TODO maybe need to set PipeID to adv
		PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		try {
			pipe = pipeService.createOutputPipe(adv, new HashSet<PeerID>(neighbour.id), 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pipe;
	}
	
	public static byte[] metaFile(File fileToSend) {
		byte[] array = null;
		if (fileToSend != null) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fileToSend);
				array = new byte[(int) fileToSend.length()];
				fis.read(array);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return array;
	}
}
