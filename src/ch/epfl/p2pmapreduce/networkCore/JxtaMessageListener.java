package ch.epfl.p2pmapreduce.networkCore;

import net.jxta.endpoint.Message;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageDecoder;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;

public class JxtaMessageListener implements PipeMsgListener{

	private Peer p;
	
	public JxtaMessageListener(Peer p) {
		this.p = p;
	}
	
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		
		 // We received a message
        Message received = event.getMessage();
        
        ch.epfl.p2pmapreduce.nodeCore.messages.Message message = MessageDecoder.decode(received);
        
        p.enqueue(message);
	}
	
}
