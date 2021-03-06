package ch.epfl.p2pmapreduce.advertisement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;

/**
 *Class representing an IndexAdvertisement.
 *it is broadcasted to all the neighbors in order for them to receive
 *the information of a brand new put to the DFS.
 */
public class PutIndexAdvertisement extends Advertisement {
	public static final String name = "PutIndexAdvertisement";
	public final static String advertisementType = "jxta:PutIndexAdvertisement";

	private ID advertisementID = ID.nullID;
	
	//informations with a put : name of the added file, its size, its creation date
	private final String identifier = "indexAdvertisement:put";
	private String fileName = "";
	private String fileSize = "";
	private String fileCreationTime = "";
	private String chunkCount = "";

	private final static String identifierTag = "MyIdentifierTag";
	private final static String fileNameTag = "MyFileNameTag";
	private final static String idTag = "MyIDTag";
	private final static String dateTag = "MyDateTag";
	private final static String sizeTag = "MySizeTag";
	private final static String chunkCountTag ="chunkCountTag";

	private final static String[] indexableFields = { identifierTag, idTag, dateTag };

	public PutIndexAdvertisement(Element Root) {

		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;

		Enumeration TheElements = MyTextElement.getChildren();

		while (TheElements.hasMoreElements()) {

			TextElement TheElement = (TextElement) TheElements.nextElement();

			processElement(TheElement);
		}
	}

	public PutIndexAdvertisement() {

		// Accepting default values
	}

	private void processElement(TextElement TheElement) {
		String theElementName = TheElement.getName();
		String theTextValue = TheElement.getTextValue();

		if (theElementName.compareTo(idTag) == 0) {
			try {

				URI ReadID = new URI(theTextValue);
				advertisementID = IDFactory.fromURI(ReadID);
				return;
			} catch (URISyntaxException Ex) {

				// Issue with ID format
				Ex.printStackTrace();
			} catch (ClassCastException Ex) {

				// Issue with ID type
				Ex.printStackTrace();
			}
		}

		if (theElementName.compareTo(fileNameTag) == 0) {
			fileName = theTextValue;
			return;
		}

		if (theElementName.compareTo(dateTag) == 0) {
			fileCreationTime = theTextValue;
			return;
		}

		if (theElementName.compareTo(sizeTag) == 0) {
			fileSize = theTextValue;
			return;
		}
		
		if(theElementName.compareTo(chunkCountTag) == 0) {
			chunkCount = theTextValue;
			return;
		}
		
	}

	@Override
	public Document getDocument(MimeMediaType theMimeMediaType) {

		// Creating document
		StructuredDocument theResult = StructuredDocumentFactory
				.newStructuredDocument(theMimeMediaType, advertisementType);

		// Adding elements
		Element myTempElement;

		myTempElement = theResult.createElement(dateTag, fileCreationTime);
		theResult.appendChild(myTempElement);

		myTempElement = theResult.createElement(sizeTag,
				fileSize);
		theResult.appendChild(myTempElement);

		myTempElement = theResult.createElement(fileNameTag, fileName);
		theResult.appendChild(myTempElement);
		
		myTempElement = theResult.createElement(identifierTag, identifier);
		theResult.appendChild(myTempElement);
		
		myTempElement = theResult.createElement(chunkCountTag, chunkCount);
		theResult.appendChild(myTempElement);

		return theResult;
	}

	public void setID(ID theID) {
		advertisementID = theID;
	}

	@Override
	public ID getID() {
		return advertisementID;
	}

	@Override
	public String[] getIndexFields() {
		return indexableFields;
	}

	public static String getAdvertisementType() {
		return advertisementType;
	}

	public long getFileSize() {
		return Long.parseLong(fileSize);
	}

	public void setFileSize(long size) {
		fileSize = Long.toString(size);
	}

	public long getFileCreationTime() {
		return Long.parseLong(fileCreationTime);
	}

	public void setFileCreationTime(long time) {
		fileCreationTime = Long.toString(time);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String name) {
		fileName = name;
	}
	
	public void setChunkCount(int intChunkCount) {
		this.chunkCount = Integer.toString(intChunkCount);
	}

	public int getChunkCount() {
		return Integer.parseInt(this.chunkCount);
	}
	
	public static class Instantiator implements
			AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return PutIndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new PutIndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new PutIndexAdvertisement(root);
		}
	}

	@Override
	public PutIndexAdvertisement clone() throws CloneNotSupportedException {
		PutIndexAdvertisement result = (PutIndexAdvertisement) super.clone();

		result.advertisementID = this.advertisementID;
		result.fileName = this.fileName;
		result.fileCreationTime = this.fileCreationTime;
		result.fileSize = this.fileSize;
		result.chunkCount = this.chunkCount;

		return result;
	}

	@Override
	public String getAdvType() {
		return PutIndexAdvertisement.class.getName();
	}
}
