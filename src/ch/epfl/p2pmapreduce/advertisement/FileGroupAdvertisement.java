package ch.epfl.p2pmapreduce.advertisement;

import net.jxta.protocol.PeerGroupAdvertisement;

/**
 * This class is an extension of a PeerGroupAdvertisement.
 * 
 * It is a PeerGroup advertisement advertising for the PeerGroup corresponding to the file, but it also contains informations such as
 * the number of peers sharing the file ( for other peers to know if they should join or not ) and the chunksize (or not?)
 * 
 * @author Tketa
 *
 */

public class FileGroupAdvertisement extends PeerGroupAdvertisement {

	private int nbMembers;
	private int chunkSize;
	
	// File name can be group id
	
	// Metadata?
	
	@Override
	public String[] getIndexFields() {
		// TODO Auto-generated method stub
		return null;
	}

	public FileGroupAdvertisement() {
		super();
	}
}
