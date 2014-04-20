package ch.epfl.p2pmapreduce.exchanger;

import ch.epfl.p2pmapreduce.index.Metadata;


public class Receive implements MessageVisitor {

	@Override
	public void visit(Put put) {
		System.out.println("Visiting put");
		String name = put.getMessageElement("name").getBytes(true).toString();
		String size = put.getMessageElement("size").getBytes(true).toString();
		String date = put.getMessageElement("date").getBytes(true).toString();
		
		String infos = name + "," + size + "," + date;
		
		//Metadata.metaPut("infos");
	}

	@Override
	public void visit(Rm rm) {
		System.out.println("Visiting rm");
		String name = rm.getMessageElement("name").getBytes(true).toString();
		String infos = name;
		//Metadata.metaRm(infos);
	}

	@Override
	public void visit(Connect connect) {
		System.out.println("Visiting connect");
		String name = connect.getMessageElement("name").getBytes(true).toString();
		String infos = name;
		//Metadata.metaConnect(infos);
	}
}
