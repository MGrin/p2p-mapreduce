package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.MessageElement;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.networkCore.JxtaCommunicator;
import ch.epfl.p2pmapreduce.nodeCore.network.JxtaMessageSender;
import ch.epfl.p2pmapreduce.nodeCore.utils.UidGenerator;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.Index;

public class MessageDecoder {

	private static boolean isConnected = false;

	/**
	 * Static method to turn a JXTA message into our Message abstraction.
	 * 
	 * @param jxtaMessage
	 * @return
	 */
	
	public static Message decode(net.jxta.endpoint.Message jxtaMessage) {
		String name = new String(jxtaMessage.getMessageElement("name").getBytes(true));
		Message message = null;
		MessageElement messageElement = jxtaMessage.getMessageElement("from");
		PipeAdvertisement from = getPipeAdvertisement(messageElement);
		
		int intFrom = JxtaCommunicator.getIdForPipeAdv(from);
		System.out.println("this peer id is " + intFrom);
		if(intFrom == -1) {
			System.out.println("we don't know this peer yet.. let's add it to our neighbours");
			intFrom = UidGenerator.freshId();
			JxtaCommunicator.putPipeAdvertisement(intFrom, from);
		}
		
		if (name.compareTo(JxtaMessageSender.SEND_INDEX) == 0) {
			byte[] newFile = jxtaMessage.getMessageElement("index").getBytes(true);
			Metadata.SaveNewVersion(newFile);
			
			Index index = new Index();
			for (File f : Metadata.toFiles()) {
				index.put(f, false);
			}
			
			message = new SendIndex(intFrom, index);

		} else if (name.compareTo(JxtaMessageSender.GET_CHUNKFIELD) == 0) {
			message = new GetChunkfield(intFrom);

		} else if (name.compareTo(JxtaMessageSender.SEND_CHUNKFIELD) == 0) {
			
			Iterator<String> nameSpaceIterator = jxtaMessage.getMessageNamespaces();
			
			while( nameSpaceIterator.hasNext() ) {
				System.out.println("SENDINDEX NameSpace : " + nameSpaceIterator.next());
			}
			
			Map<String, Chunkfield> chunkfields = convertStringToMap(new String(jxtaMessage
					.getMessageElement("chunkfield").getBytes(true)));
			
			message = new SendChunkfield(intFrom, chunkfields);

		} else if (name.compareTo(JxtaMessageSender.GET_CHUNK) == 0) {
			String fileName = new String(jxtaMessage.getMessageElement("fName")
					.getBytes(true));
			int chunkId = Integer.parseInt(new String(jxtaMessage.getMessageElement("chunkId")
					.getBytes(true)));
			
			message = new GetChunk(intFrom, fileName, chunkId);

		} else if (name.compareTo(JxtaMessageSender.SEND_CHUNK) == 0) {
			String fileName = new String(jxtaMessage.getMessageElement("fName")
					.getBytes(true));
			int chunkId = Integer.parseInt(new String(jxtaMessage.getMessageElement("chunkId")
					.getBytes(true)));
			
			byte[] chunk = jxtaMessage.getMessageElement("chunk").getBytes(true);
					
			message = new SendChunk(intFrom, fileName, chunkId, chunk);

		} else if (name.compareTo(JxtaMessageSender.GET_INDEX) == 0) {
			
			//TODO: What is this piece of commented code for?
//			if (!isConnected) {
//				System.out.println("Visiting connect");
//				isConnected = true;
//				Metadata.metaConnect();
				
				message = new GetIndex(intFrom);
//			}
		}
		
		return message;
	}

	@SuppressWarnings("rawtypes")
	private static PipeAdvertisement getPipeAdvertisement(
			MessageElement messageElement) {
		PipeAdvertisement from = null;
		XMLDocument doc;
		try {
			doc = (XMLDocument) StructuredDocumentFactory
					.newStructuredDocument(messageElement.getMimeType(),
							messageElement.getStream());

			from = (PipeAdvertisement) AdvertisementFactory
					.newAdvertisement(doc.getRoot());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return from;
	}

	private static Map<String, Chunkfield> convertStringToMap(String text) {

		Map<String, Chunkfield> map = new HashMap<String, Chunkfield>();
		String[] elements = text.split("/");
		
		System.out.println("Text : " + text);

		for (int i = 0; i < elements.length; i++) {
			String[] keyValue = elements[i].split(":");
			System.out.println("Elements[" + i + "] : " + elements[i]);

			String key = keyValue[0];

			boolean[] chunkField = new boolean[keyValue[1].length()];

			for (int j = 0; j < keyValue[1].length(); j++) {
				chunkField[j] = (keyValue[1].charAt(j) == '1');
			}

			map.put(key, new Chunkfield(chunkField));
		}
		return map;
	}

	// UTILS

//	public static int convertByteToInt(byte[] b) {
//		int value = 0;
//		for (int i = 0; i < b.length; i++)
//			value = (value << 8) | b[i];
//		return value;
//	}
//
//	public static byte[] intToByteArray(int value) {
//		return new byte[] { 
//				(byte) (value >>> 24),
//				(byte) (value >>> 16),
//				(byte) (value >>> 8),
//				(byte) value };
//	}
//
//	public static String byteArrayToString(byte[] b) {
//		String s = null;
//		try {
//			s = new String(b, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return s;
//	}
}
