package ch.epfl.p2pmapreduce.exchanger;

public interface MessageVisitor {
	void visit(ChunkfieldGetter chunkfieldGetter);
	void visit(ChunkfieldSender chunkfieldSender);
	void visit(ChunkGetter chunkGetter);
	void visit(ChunkSender chunkSender);
	void visit(Put put);
	void visit(Rm rm);
	void visit(Connect connect);
	void visit(All all);
}
