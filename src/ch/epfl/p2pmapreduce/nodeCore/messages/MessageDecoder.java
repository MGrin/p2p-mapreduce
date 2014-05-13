package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.MessageElement;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.network.JxtaMessageSender;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;

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
		
		if (name.compareTo(JxtaMessageSender.SEND_INDEX) == 0) {
			byte[] newFile = jxtaMessage.getMessageElement("index").getBytes(true);
			Metadata.SaveNewVersion(newFile);
			
			//message = new SendIndex(from, newFile);

		} else if (name.compareTo(JxtaMessageSender.GET_CHUNKFIELD) == 0) {
			// message = new GetChunkfield(from);

		} else if (name.compareTo(JxtaMessageSender.SEND_CHUNKFIELD) == 0) {
			Map<Integer, Chunkfield> chunkfields = JxtaMessageSender.convertStringToMap(new String(jxtaMessage
					.getMessageElement("chunkfield").getBytes(true)));
			
			// message = new SendChunkfield(from, chunkfields);

		} else if (name.compareTo(JxtaMessageSender.GET_CHUNK) == 0) {
			String fileName = new String(jxtaMessage.getMessageElement("fName")
					.getBytes(true));
			String chunkId = new String(jxtaMessage.getMessageElement("chunkId")
					.getBytes(true));
			// message = new GetChunk(from, fileName, chunkId);

		} else if (name.compareTo(JxtaMessageSender.SEND_CHUNK) == 0) {
			String fileName = new String(jxtaMessage.getMessageElement("fName")
					.getBytes(true));
			String chunkId = new String(jxtaMessage.getMessageElement("chunkId")
					.getBytes(true));
			byte[] chunk = jxtaMessage.getMessageElement("chunk").getBytes(true);
					
			// message = new SendChunk(from, fileName, chunkId, chunk);

		} else if (name.compareTo(JxtaMessageSender.GET_INDEX) == 0) {
			if (!isConnected) {
				System.out.println("Visiting connect");
				isConnected = true;
				Metadata.metaConnect();
			}
		}
		// message = new GetIndex(from);
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

	public static Map<Integer, Chunkfield> convertBytesToMap(byte[] bytes) {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o;
		Map<Integer, Chunkfield> map = null;
		try {
			o = new ObjectInputStream(b);
			map = (Map<Integer, Chunkfield>) o.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
