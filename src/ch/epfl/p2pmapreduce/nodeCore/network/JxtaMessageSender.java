package ch.epfl.p2pmapreduce.nodeCore.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	// types of messages
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
				(XMLDocument) senderPipeAdvertisement
					.getDocument(MimeMediaType.XMLUTF8), null);

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
		MessageElement name = new StringMessageElement("name", SEND_CHUNKFIELD,
				null);
		message.addMessageElement(name);

		System.out.println("map to string is : " + convertMapToString(sendChunkfield.chunkfields()));
		
		MessageElement chunkField = new StringMessageElement("chunkfield",
				convertMapToString(sendChunkfield.chunkfields()), null);
		
		message.addMessageElement(chunkField);

		communicator.sendMessage(message, receiver);

		return false;
	}

	@Override
	public boolean send(GetChunk getChunk, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", GET_CHUNK, null);
		message.addMessageElement(name);
		MessageElement fileName = new StringMessageElement("fName",
				getChunk.fName(), null);
		message.addMessageElement(fileName);
		MessageElement chunkId = new StringMessageElement("chunkId",
				Integer.toString(getChunk.chunkId()), null);
		message.addMessageElement(chunkId);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(SendChunk sendChunk, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", SEND_CHUNK, null);
		message.addMessageElement(name);
		MessageElement fileName = new StringMessageElement("fName",
				sendChunk.fName(), null);
		message.addMessageElement(fileName);
		MessageElement chunkId = new StringMessageElement("chunkId",
				Integer.toString(sendChunk.chunkId()), null);
		message.addMessageElement(chunkId);
		MessageElement chunk = new ByteArrayMessageElement("chunk",
				MimeMediaType.AOS, sendChunk.getChunkData(), null);
		message.addMessageElement(chunk);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(SendIndex sendIndex, Neighbour receiver) {

		Message message = messageBasis.clone();

		MessageElement name = new StringMessageElement("name", SEND_INDEX, null);
		message.addMessageElement(name);
		byte[] array = getRawFile(Metadata.file);
		MessageElement file = new ByteArrayMessageElement("index",
				MimeMediaType.AOS, array, null);
		message.addMessageElement(file);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(GetIndex getIndex, Neighbour receiver) {

		Message message = messageBasis.clone();
		MessageElement name = new StringMessageElement("name", GET_INDEX, null);
		message.addMessageElement(name);

		communicator.sendMessage(message, receiver);

		return true;
	}

	@Override
	public boolean send(PutIndexAdvertisement putIndex) {

		return communicator.publishAdvertisement(putIndex,
				communicator.netPeerGroup);
	}

	@Override
	public boolean send(RmIndexAdvertisement rmIndex) {

		return communicator.publishAdvertisement(rmIndex,
				communicator.netPeerGroup);
	}

	// UTILS

	private String convertMapToString(Map<String, Chunkfield> map) {
		StringBuilder builder = new StringBuilder();

		for (String s : map.keySet())
			builder.append(s + ":" + map.get(s).toBitString() + "/");

		return builder.toString();
	}


	public static byte[] getRawFile(File fileToSend) {
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
