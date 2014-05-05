package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

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
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class MessageDecoder {

	private static boolean isConnected = false;
	
	/**
	 * Static method to turn a JXTA message into our Message abstraction.
	 * 
	 * TODO: Implement. (David & Alban ?)
	 * 
	 * @param jxtaMessage
	 * @return
	 */
	public static Message decode(net.jxta.endpoint.Message jxtaMessage) {
			String name = jxtaMessage.getMessageElement("name").getBytes(true).toString();
			Message message = null;
			//from = ...
			
			if (name.compareTo("ALL") == 0){
				
				byte[] index = jxtaMessage.getMessageElement("index").getBytes(true);
				byte[] newFile = jxtaMessage.getMessageElement("data").getBytes(true);
				Metadata.SaveNewVersion(newFile);
				//message = new SendIndex(from, index);
				
			} else if (name.compareTo("GETCHUNKFIELD") == 0){
				
				//message = new GetChunkfield(from);
				
			} else if (name.compareTo("SENDCHUNKFIELD") == 0){
				
				Map<Integer, Chunkfield> chunkfields = convertBytesToMap(jxtaMessage.getMessageElement("chunkfield").getBytes(true));
				//message = new SendChunkfield(from, chunkfields);
				
				
			} else if (name.compareTo("GETCHUNK") == 0){
								
				String fileId = jxtaMessage.getMessageElement("fileId").getBytes(true).toString();
				String chunkId = jxtaMessage.getMessageElement("chunkId").getBytes(true).toString();
				//message = new GetChunk(from, fileId, chunkId);
				
			} else if (name.compareTo("SENDCHUNK") == 0){
				
				
				String fileId = jxtaMessage.getMessageElement("fileId").getBytes(true).toString();
				String chunkId = jxtaMessage.getMessageElement("chunkId").getBytes(true).toString();
				byte[] chunk = jxtaMessage.getMessageElement("chunk").getBytes(true); 
				//message = new SendChunk(from, fileId, chunkId, chunk);
				
			} else if (name.compareTo("CONNECT") == 0){
				
				if (!isConnected) {
					System.out.println("Visiting connect");
					isConnected = true;
					Metadata.metaConnect();
				}
				//message = new GetIndex(from);
			}
			
			//Mishell.p.getMessageHandler().receive(message);
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
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} 
		return map;
	}
}
