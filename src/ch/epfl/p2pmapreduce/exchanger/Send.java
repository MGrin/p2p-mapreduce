package ch.epfl.p2pmapreduce.exchanger;

import java.io.File;
import java.text.SimpleDateFormat;
import org.jdom2.*;
import org.jdom2.output.*;

import ch.epfl.p2pmapreduce.index.Metadata;


public class Send {
	public static void put(String localFileName, String DFSFileName){
		File file = new File(localFileName);
		if (file.exists()){
			long fileSize = file.length()/1024; //bytes to kb 
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String fileDate = sdf.format(file.lastModified());
			System.out.println(file.getName() + ", " + fileSize + ", " + fileDate);
			Metadata.metaPut();
			//envoyer message avec infos
		}
	}
	public static void rm(String fileName){
		Metadata.metaRm();
		//envoyer message avec infos
	}
}

