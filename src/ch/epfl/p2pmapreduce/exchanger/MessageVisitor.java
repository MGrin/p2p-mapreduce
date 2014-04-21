package ch.epfl.p2pmapreduce.exchanger;

public interface MessageVisitor {
	void visit(Put put);
	void visit(Rm rm);
	void visit(Connect connect);
	void visit(All all);
}
