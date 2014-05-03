package ch.epfl.p2pmapreduce.nodeCore.network;

import net.jxta.protocol.PipeAdvertisement;

public class JxtaNeighbour extends Neighbour {

	private PipeAdvertisement pipeAdvertisement;
	
	public JxtaNeighbour(int peerId, PipeAdvertisement pipeAdvertisement) {
		super(peerId);
		this.pipeAdvertisement = pipeAdvertisement;
	}
	
	public PipeAdvertisement getPipeAdvertisement() {
		return getPipeAdvertisement();
	}

	
	
}
