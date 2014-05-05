package ch.epfl.p2pmapreduce.exchanger;

import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import net.jxta.endpoint.Message;

public class ChunkfieldGetter extends Message implements MessageStruct {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private int from;
	
	public ChunkfieldGetter(GetChunkfield getChunkField){
		super();
		this.setName("CHUNKFIELDGETTER");
		this.setFrom(from);
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visit(this);
		
	}
}
