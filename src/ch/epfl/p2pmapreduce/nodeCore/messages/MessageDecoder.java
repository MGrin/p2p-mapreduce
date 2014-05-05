package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.MessageElement;
import net.jxta.protocol.PipeAdvertisement;
import ch.epfl.p2pmapreduce.index.Metadata;
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
		String name = jxtaMessage.getMessageElement("name").getBytes(true)
				.toString();
		Message message = null;
		MessageElement messageElement = jxtaMessage.getMessageElement("from",
				"from");

		XMLDocument doc;
		try {
			doc = (XMLDocument) StructuredDocumentFactory
					.newStructuredDocument(messageElement.getMimeType(),
							messageElement.getStream());


		//TODO LOL PipeAdvertisement non instanciable !!
		PipeAdvertisement from = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(doc.getRoot());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	if (name.compareTo("ALL") == 0) {

		byte[] index = jxtaMessage.getMessageElement("index")
				.getBytes(true);
		byte[] newFile = jxtaMessage.getMessageElement("data").getBytes(
				true);
		Metadata.SaveNewVersion(newFile);
		// message = new SendIndex(from, index);

	} else if (name.compareTo("GETCHUNKFIELD") == 0) {

		// message = new GetChunkfield(from);

	} else if (name.compareTo("SENDCHUNKFIELD") == 0) {

		Map<Integer, Chunkfield> chunkfields = convertBytesToMap(jxtaMessage
				.getMessageElement("chunkfield").getBytes(true));
		// message = new SendChunkfield(from, chunkfields);

	} else if (name.compareTo("GETCHUNK") == 0) {

		String fileId = jxtaMessage.getMessageElement("fileId")
				.getBytes(true).toString();
		String chunkId = jxtaMessage.getMessageElement("chunkId")
				.getBytes(true).toString();
		// message = new GetChunk(from, fileId, chunkId);

	} else if (name.compareTo("SENDCHUNK") == 0) {

		String fileId = jxtaMessage.getMessageElement("fileId")
				.getBytes(true).toString();
		String chunkId = jxtaMessage.getMessageElement("chunkId")
				.getBytes(true).toString();
		byte[] chunk = jxtaMessage.getMessageElement("chunk")
				.getBytes(true);
		// message = new SendChunk(from, fileId, chunkId, chunk);

	} else if (name.compareTo("CONNECT") == 0) {

		if (!isConnected) {
			System.out.println("Visiting connect");
			isConnected = true;
			Metadata.metaConnect();
		}
		// message = new GetIndex(from);
	}

	// Mishell.p.getMessageHandler().receive(message);
	return null;
	}

	public static Map<Integer, Chunkfield> convertBytesToMap(byte[] bytes) {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o;
		Map<Integer, Chunkfield> map = null;
		try {
			o = new ObjectInputStream(b);
			map = (Map<Integer, Chunkfield>) o.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return map;
	}

	// UTILS

	public static int convertByteToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < b.length; i++)
			value = (value << 8) | b[i];
		return value;
	}

	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	public static String byteArrayToString(byte[] b) {
		String s = null;
		try {
			s = new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

}
