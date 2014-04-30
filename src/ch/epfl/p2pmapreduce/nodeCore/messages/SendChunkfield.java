package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;


public class SendChunkfield implements Message {
	
	private int from;
	private Map<Integer, Chunkfield> chunkfields;
	
	public SendChunkfield(int from, Map<Integer, Chunkfield> chunkfields) {
		this.from = from;
		this.chunkfields = chunkfields;
	}
	
	public int sender() { return from; }
	
	public Map<Integer, Chunkfield> chunkfields() { return chunkfields; }
	
	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}

	public String toString() {
		return "send chunkfield message from peer " + from;
	}
}