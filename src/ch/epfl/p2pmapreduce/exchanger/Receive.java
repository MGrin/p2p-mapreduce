package ch.epfl.p2pmapreduce.exchanger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import ch.epfl.p2pmapreduce.CLI.Mishell;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;


public class Receive implements MessageVisitor {
	private boolean isConnected = false;

	@Override
	public void visit(Put put) {
		System.out.println("Visiting put");
		String name = put.getMessageElement("name").getBytes(true).toString();
		String size = put.getMessageElement("size").getBytes(true).toString();
		String date = put.getMessageElement("date").getBytes(true).toString();
		
		String infos = name + "," + size + "," + date;
		
		Metadata.metaPut(infos);
		//Mishell.p.getMessageHandler().receive()
	}

	@Override
	public void visit(Rm rm) {
		System.out.println("Visiting rm");
		String name = rm.getMessageElement("name").getBytes(true).toString();
		String infos = name;
		Metadata.metaRm(infos);
		//Mishell.p.getMessageHandler().receive()
	}

	@Override
	public void visit(Connect connect) {
		//String name = connect.getMessageElement("name").getBytes(true).toString();
		//int from = Integer.parseInt(connect.getMessageElement("from").getBytes(true).toString());
//		if (!isConnected) {
//			System.out.println("Visiting connect");
//			isConnected = true;
//			Metadata.metaConnect();
//		}
		//Mishell.p.getMessageHandler().receive()
	}
	public void visit(All all){
		//String name = all.getMessageElement("name").getBytes(true).toString();
		//int from = Integer.parseInt(all.getMessageElement("from").getBytes(true).toString());
		//byte[] index = all.getMessageElement("index").getBytes(true);
		//System.out.println("Visiting all");
		//byte[] newFile = all.getMessageElement("data").getBytes(true);
		//Metadata.SaveNewVersion(newFile);
		
		//Mishell.p.getMessageHandler().receive()
	}

	@Override
	public void visit(ChunkfieldGetter chunkfieldGetter) {
		//System.out.println("Visiting ChunkfieldGetter");
		//String name = chunkfieldGetter.getMessageElement("name").getBytes(true).toString();
		//int from = Integer.parseInt(chunkfieldGetter.getMessageElement("from").getBytes(true).toString());
		
		//Mishell.p.getMessageHandler().receive(new GetChunkfield(from));
	}

	@Override
	public void visit(ChunkfieldSender chunkfieldSender) {
		System.out.println("Visiting ChunkfieldGetter");
		//String name = chunkfieldSender.getMessageElement("name").getBytes(true).toString();
		//int from = Integer.parseInt(chunkfieldSender.getMessageElement("from").getBytes(true).toString());
		//Map<Integer, Chunkfield> chunkfields = convertBytesToMap(chunkfieldSender.getMessageElement("chunkfield").getBytes(true));
		
		//Mishell.p.getMessageHandler().receive(new SendChunkfield(from, chunkfields));
	}

	//public Map<Integer, Chunkfield> convertBytesToMap(byte[] bytes) {
		//ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		//ObjectInputStream o;
		//Map<Integer, Chunkfield> map = null;
		//try {
			//o = new ObjectInputStream(b);
			//map = (Map<Integer, Chunkfield>) o.readObject();
		//} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//} catch (ClassNotFoundException e){
		//	e.printStackTrace();
		//} 
		//return map;
	//}

	@Override
	public void visit(ChunkGetter chunkGetter) {
		System.out.println("Visiting ChunkGetter");
//		String name = chunkGetter.getMessageElement("name").getBytes(true).toString();
//		String from = chunkGetter.getMessageElement("from").getBytes(true).toString();
//		String fileId = chunkGetter.getMessageElement("fileId").getBytes(true).toString();
//		String chunkId = chunkGetter.getMessageElement("chunkId").getBytes(true).toString();
//		
//		//Mishell.p.getMessageHandler().receive(new GetChunk(from, fileId, chunkId));
	}

	@Override
	public void visit(ChunkSender chunkSender) {
//		System.out.println("Visiting ChunkSender");
//		String name = chunkSender.getMessageElement("name").getBytes(true).toString();
//		String from = chunkSender.getMessageElement("from").getBytes(true).toString();
//		String fileId = chunkSender.getMessageElement("fileId").getBytes(true).toString();
//		String chunkId = chunkSender.getMessageElement("chunkId").getBytes(true).toString();
//		byte[] chunk = chunkSender.getMessageElement("chunk").getBytes(true); //FIXME
//		
//		//Mishell.p.getMessageHandler().receive(new SendChunk(from, fileId, chunkId));
	}
}
