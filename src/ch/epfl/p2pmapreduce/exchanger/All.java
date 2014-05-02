package ch.epfl.p2pmapreduce.exchanger;

import net.jxta.endpoint.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.volume.Index;

public class All extends Message implements MessageStruct {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int from;
	private Index index;
	
	public All(SendIndex sendIndex) {
		super();
		this.name = "ALL";
		this.setFrom(sendIndex.sender());
		this.setIndex(sendIndex.index);
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}


	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visit(this);
	}


	public void setFrom(int from) {
		this.from = from;
	}


	public int getFrom() {
		return from;
	}


	public void setIndex(Index index) {
		this.index = index;
	}


	public Index getIndex() {
		return index;
	}
}