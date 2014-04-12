package ch.epfl.p2pmapreduce.exchanger;

public interface MessageStruct {
	void accept(MessageVisitor visitor);
}
