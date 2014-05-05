package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.PutIndex;
import ch.epfl.p2pmapreduce.advertisement.RmIndex;
import ch.epfl.p2pmapreduce.exchanger.All;
import ch.epfl.p2pmapreduce.exchanger.ChunkGetter;
import ch.epfl.p2pmapreduce.exchanger.ChunkSender;
import ch.epfl.p2pmapreduce.exchanger.ChunkfieldGetter;
import ch.epfl.p2pmapreduce.exchanger.ChunkfieldSender;
import ch.epfl.p2pmapreduce.exchanger.Connect;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.networkCore.JxtaCommunicator;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;


/**
 * This class is responsible for transforming abstract messages into JXTA messages, and send them to the corresponding Neighbour.
 * 
 * TODO: Add PipeAdvertisement of this peer to the message, so that the other peer can ANSWER messages.
 * 
 * @author Tketa
 *
 */

public class JxtaMessageSender implements IMessageSender{

	private JxtaCommunicator communicator;
	private PipeAdvertisement senderPipeAdvertisement;

	public JxtaMessageSender(JxtaCommunicator jxtaCommunicator) {
		this.communicator = jxtaCommunicator;
		
		this.senderPipeAdvertisement = communicator.getPipeAdvertisement();
	}

	@Override
	public boolean send(GetChunkfield getChunkfield, Neighbour receiver) {
		ChunkfieldGetter message = new ChunkfieldGetter(getChunkfield);
		MessageElement name = new StringMessageElement("name", message.getName(), null);
		message.addMessageElement(name);
		MessageElement from = new StringMessageElement(String.valueOf(message.getFrom()), message.getName(), null);
		message.addMessageElement(from);
		
		communicator.sendMessage(message, (JxtaNeighbour)receiver);
		return false;
	}

	@Override
	public boolean send(SendChunkfield sendChunkfield, Neighbour receiver) {
		ChunkfieldSender message = new ChunkfieldSender(sendChunkfield);
		MessageElement name = new StringMessageElement("name", message.getName(), null);
		message.addMessageElement(name);
		MessageElement from = new StringMessageElement("from", String.valueOf(message.getFrom()), null);
		message.addMessageElement(from);
		MessageElement chunkfield = new ByteArrayMessageElement("chunkfield", MimeMediaType.XML_DEFAULTENCODING, convertMapToBytes(message.getChunkfields()), null);
		message.addMessageElement(chunkfield);
		
		communicator.sendMessage(message, (JxtaNeighbour) receiver);
		
		return false;
	}

	@Override
	public boolean send(GetChunk getChunk, Neighbour receiver) {

		ChunkGetter message = new ChunkGetter(getChunk);
		MessageElement name = new StringMessageElement("name", message.getName(), null);
		message.addMessageElement(name);
		MessageElement fileId = new StringMessageElement("fileId", String.valueOf(message.getFile().uid), null);
		message.addMessageElement(fileId);
		MessageElement chunkId = new StringMessageElement("chunkId", String.valueOf(message.getChunkId()), null);
		message.addMessageElement(chunkId);

		communicator.sendMessage(message, (JxtaNeighbour)receiver);

		return false;
	}

	@Override
	public boolean send(SendChunk sendChunk, Neighbour receiver) {

		ChunkSender message = new ChunkSender(sendChunk);
		MessageElement name = new StringMessageElement("name", message.getName(), null);
		message.addMessageElement(name);
		MessageElement fileId = new StringMessageElement("fileId", String.valueOf(message.getFileId()), null);
		message.addMessageElement(fileId);
		MessageElement chunkId = new StringMessageElement("chunkId", String.valueOf(message.getChunkId()), null);
		message.addMessageElement(chunkId);
		MessageElement chunk = new ByteArrayMessageElement("chunk", MimeMediaType.XML_DEFAULTENCODING, message.getChunkData(), null);
		message.addMessageElement(chunk);

		communicator.sendMessage(message, (JxtaNeighbour)receiver);


		return false;
	}

	@Override
	public boolean send(SendIndex sendIndex, Neighbour receiver) {

		All all = new All(sendIndex);

		byte[] array = metaFile(Metadata.file);
		MessageElement file = new ByteArrayMessageElement("index", MimeMediaType.XML_DEFAULTENCODING, array, null);
		all.addMessageElement(file);
		communicator.sendMessage(all, (JxtaNeighbour)receiver);

		return false;
	}

	@Override
	public boolean send(GetIndex getIndex, Neighbour receiver) {

		Connect message = new Connect(getIndex);

		communicator.sendMessage(message, (JxtaNeighbour)receiver);

		return false;
	}

	@Override
	public boolean send(PutIndex putIndex) {
		
		return false;
	}

	@Override
	public boolean send(RmIndex rmIndex) {
		// TODO Auto-generated method stub
		return false;
	}


	//UTILS
	
	public byte[] convertMapToBytes(Map<Integer, Chunkfield> map) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    ObjectOutputStream out;
	    
		try {
			out = new ObjectOutputStream(byteOut);
			out.writeObject(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    return byteOut.toByteArray();
	}

	//	public OutputPipe createPipe(Neighbour neighbour){
	//		OutputPipe pipe = null;
	//		//TODO maybe need to set PipeID to adv
	//		PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
	//		try {
	//			pipe = pipeService.createOutputPipe(adv, new HashSet<PeerID>(neighbour.id), 1000);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		return pipe;
	//	}

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
