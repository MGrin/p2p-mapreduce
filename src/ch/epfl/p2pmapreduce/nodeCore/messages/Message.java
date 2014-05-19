package ch.epfl.p2pmapreduce.nodeCore.messages;

/**
 * Interface for a message
 *
 */
public interface Message {
	int sender();
	void visit(MessageReceiver messageReceiver);
}
