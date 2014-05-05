package ch.epfl.p2pmapreduce.exchanger;

import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import net.jxta.endpoint.Message;

public class ChunkfieldSender extends Message implements MessageStruct {
	private static final long serialVersionUID = 1L;
	
	String name;
	private int from;
	private Map<Integer, Chunkfield> chunkfields;
	
	public ChunkfieldSender(SendChunkfield sendChunkfield) {
		this.name = "SENDCHUNKFIELD";
		this.from = sendChunkfield.sender();
		this.chunkfields = sendChunkfield.chunkfields();
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	
	public void setFrom(int from) {
		this.from = from;
	}

	public int getFrom() {
		return from;
	}

	public void setChunkfields(Map<Integer, Chunkfield> chunkfields) {
		this.chunkfields = chunkfields;
	}

	public Map<Integer, Chunkfield> getChunkfields() {
		return chunkfields;
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visit(this);
	}

}
