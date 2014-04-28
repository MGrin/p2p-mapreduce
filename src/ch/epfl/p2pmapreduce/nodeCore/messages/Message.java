package ch.epfl.p2pmapreduce.nodeCore.messages;


public interface Message {
	int sender();
	void visit(MessageReceiver messageReceiver);
}
