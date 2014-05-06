package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.exchanger.Send;
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
 * This class is responsible for transforming abstract messages into JXTA
 * messages, and send them to the corresponding Neighbour.
 * 
 * 
 * @author Tketa
 * 
 */

public class JxtaMessageSender implements IMessageSender {

	private JxtaCommunicator communicator;
	private PipeAdvertisement senderPipeAdvertisement;

	private Message messageBasis;

	//types of messages
	public static final String SEND_INDEX = "SENDINDEX";
	public static final String GET_INDEX = "GETINDEX";
	public static final String RM = "RM";
	public static final String PUT = "PUT";
	public static final String SEND_CHUNK = "SENDCHUNK";
	public static final String GET_CHUNK = "GETCHUNK";
	public static final String GET_CHUNKFIELD = "GETCHUNKFIELD";
	public static final String SEND_CHUNKFIELD = "SENDCHUNKFIELD";

	public JxtaMessageSender(JxtaCommunicator jxtaCommunicator) {
		this.communicator = jxtaCommunicator;

		this.senderPipeAdvertisement = communicator.getPipeAdvertisement();

		messageBasis = new Message();

		// Creating the message element and adding it
		TextDocumentMessageElement senderAdvertisementElement = new TextDocumentMessageElement(
				"from", 
				(XMLDocument) senderPipeAdvertisement.getDocument(MimeMediaType.XMLUTF8),
				null);

		messageBasis.addMessageElement(senderAdvertisementElement);

	}

	@Override
	public boolean send(GetChunkfield getChunkfield, Neighbour receiver) {
		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", GET_CHUNKFIELD,
				null);
		message.addMessageElement(name);

		communicator.sendMessage(message, receiver);

		return false;
	}

	@Override
	public boolean send(SendChunkfield sendChunkfield, Neighbour receiver) {
		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name",
				SEND_CHUNKFIELD, null);
		message.addMessageElement(name);
		
		
		//TODO: TO CHANGE! Chunkfield is not serializable..
//		MessageElement chunkfield = new ByteArrayMessageElement("chunkfield",
//				MimeMediaType.XML_DEFAULTENCODING,
//				convertMapToBytes(sendChunkfield.chunkfields()), null);
//		
//		message.addMessageElement(chunkfield);

		communicator.sendMessage(message, receiver);

		return false;
	}
	
	public static String convertMapToString(Map<Integer,Chunkfield> map){
		String result = "";
		Set listKeys = map.keySet(); 
		Iterator iterator=listKeys.iterator();
		while(iterator.hasNext())
		{
			Object key= iterator.next();
			Chunkfield chunkfield = map.get(key);
			String value = chunkfield.toBitString();
			//to test
			//String value = map.get(key);
			result += key+":"+value+"/";
		}
		return result;
	}
	public static Map<Integer, Chunkfield> convertStringToMap(String text){
		Map<Integer, Chunkfield> map = new HashMap<Integer, Chunkfield>();
		List<String> list = Send.tokenize(text, "/");
		for (int i = 0; i<list.size();i++){
			List<String> tempList = Send.tokenize(list.get(i), ":");
			int key = Integer.parseInt(tempList.get(0));
			String temp = tempList.get(1);
			int size = temp.length();
			boolean[] field = new boolean[size];
			for(int j = 0; j<field.length; j++){
				field[j] = temp.charAt(j) == '1';
			}
			Chunkfield chunkfield = new Chunkfield(field);
			map.put(key,chunkfield);
		}
		return map;
	}
	//test TO DELETE !!!!!
	public static void main(String[] args){
		
	}
	@Override
	public boolean send(GetChunk getChunk, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", GET_CHUNK, null);
		message.addMessageElement(name);
		MessageElement fileId = new StringMessageElement("fileId",
				String.valueOf(getChunk.file().uid), null);
		message.addMessageElement(fileId);
		MessageElement chunkId = new StringMessageElement("chunkId",
				String.valueOf(getChunk.chunkId()), null);
		message.addMessageElement(chunkId);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(SendChunk sendChunk, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", SEND_CHUNK,
				null);
		message.addMessageElement(name);
		//		TextDocumentMessageElement from = new TextDocumentMessageElement("from", (XMLDocument) getChunkfield.sender().getDocument(MimeMediaType.XMLUTF8), null);
		//		message.addMessageElement(from);
		MessageElement fileId = new StringMessageElement("fileId",
				String.valueOf(sendChunk.fileId()), null);
		message.addMessageElement(fileId);
		MessageElement chunkId = new StringMessageElement("chunkId",
				String.valueOf(sendChunk.chunkId()), null);
		message.addMessageElement(chunkId);
		MessageElement chunk = new ByteArrayMessageElement("chunk",
				MimeMediaType.XML_DEFAULTENCODING, sendChunk.getChunkData(),
				null);
		message.addMessageElement(chunk);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(SendIndex sendIndex, Neighbour receiver) {

		Message message = messageBasis.clone();

		MessageElement name = new StringMessageElement("name", SEND_INDEX, null);
		message.addMessageElement(name);
		//		TextDocumentMessageElement from = new TextDocumentMessageElement("from", (XMLDocument) getChunkfield.sender().getDocument(MimeMediaType.XMLUTF8), null);
		//		message.addMessageElement(from);
		byte[] array = metaFile(Metadata.file);
		MessageElement file = new ByteArrayMessageElement("index",
				MimeMediaType.XML_DEFAULTENCODING, array, null);
		message.addMessageElement(file);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(GetIndex getIndex, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", GET_INDEX, null);
		message.addMessageElement(name);
		//		TextDocumentMessageElement from = new TextDocumentMessageElement("from", (XMLDocument) getChunkfield.sender().getDocument(MimeMediaType.XMLUTF8), null);
		//		message.addMessageElement(from);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(PutIndexAdvertisement putIndex) {

		return communicator.publishAdvertisement(putIndex, communicator.netPeerGroup);
	}

	@Override
	public boolean send(RmIndexAdvertisement rmIndex) {

		return communicator.publishAdvertisement(rmIndex, communicator.netPeerGroup);
	}

	// UTILS

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
