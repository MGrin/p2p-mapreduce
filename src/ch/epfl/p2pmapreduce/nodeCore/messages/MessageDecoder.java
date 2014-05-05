package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.io.UnsupportedEncodingException;

import ch.epfl.p2pmapreduce.exchanger.All;
import ch.epfl.p2pmapreduce.exchanger.ChunkGetter;
import ch.epfl.p2pmapreduce.exchanger.ChunkSender;
import ch.epfl.p2pmapreduce.exchanger.ChunkfieldGetter;
import ch.epfl.p2pmapreduce.exchanger.ChunkfieldSender;
import ch.epfl.p2pmapreduce.exchanger.Connect;
import ch.epfl.p2pmapreduce.exchanger.MessageStruct;
import ch.epfl.p2pmapreduce.exchanger.Put;
import ch.epfl.p2pmapreduce.exchanger.Receive;
import ch.epfl.p2pmapreduce.exchanger.Rm;

public class MessageDecoder {

	/**
	 * Static method to turn a JXTA message into our Message abstraction.
	 * 
	 * TODO: Implement. (David & Alban ?)
	 * 
	 * @param jxtaMessage
	 * @return
	 */
	public static Message decode(net.jxta.endpoint.Message jxtaMessage) {
		String name = jxtaMessage.getMessageElement("name").getBytes(true)
				.toString();
		if (name.compareTo("ALL") == 0) {
			((All) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("CHUNKFIELDGETTER") == 0) {
			((ChunkfieldGetter) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("CHUNKFIELDSENDER") == 0) {
			((ChunkfieldSender) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("CHUNKGETTER") == 0) {
			((ChunkGetter) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("CHUNKSENDER") == 0) {
			((ChunkSender) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("CONNECT") == 0) {
			((Connect) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("PUT") == 0) {
			((Put) jxtaMessage).accept(new Receive());
		} else if (name.compareTo("RM") == 0) {
			((Rm) jxtaMessage).accept(new Receive());
		}
		return null;
	}

	// UTILS

	public static int convertByteToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < b.length; i++)
			value = (value << 8) | b[i];
		return value;
	}

	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24),
				(byte) (value >>> 16),
				(byte) (value >>> 8),
				(byte) value };
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
