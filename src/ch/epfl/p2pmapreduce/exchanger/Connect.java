package ch.epfl.p2pmapreduce.exchanger;

import net.jxta.endpoint.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;

public class Connect extends Message implements MessageStruct {
	private static final long serialVersionUID = 1L;
	private String name;
	private int from;
	
	public Connect(GetIndex getIndex) {
		super();
		this.name = "CONNECT";
		this.from = getIndex.sender();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getFrom() {
		return from;
	}
	
	public void setName(int from) {
		this.from = from;
	}

	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visit(this);
	}
}
