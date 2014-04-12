package ch.epfl.p2pmapreduce.exchanger;

import net.jxta.endpoint.Message;

public class Rm extends Message implements MessageStruct {
	private static final long serialVersionUID = 1L;
	private String name;
	
	public Rm(String name) {
		super();
		this.name = name;
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
