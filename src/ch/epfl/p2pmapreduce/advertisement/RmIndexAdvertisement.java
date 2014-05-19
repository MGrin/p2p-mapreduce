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
 *the information of a brand new rm to the DFS.
 */
public class RmIndexAdvertisement extends Advertisement {

	public static final String name = "RmIndexAdvertisement";
	public static final String advertisementType = "jxta:RmIndexAdvertisement";

	private ID advertisementID = ID.nullID;

	private final String identifier ="indexAdvertisement:rm";

	//informations of an rm : name of the deleted file, its deletion time
	private String fileName = "";
	private String fileDeletionTime = Long.toString(0);

	private final static String identifierTag = "MyIdentifierTag";
	private final static String idTag = "MyIDTag";
	private final static String fileNameTag = "MyfileNameTag";

	private final static String fileDeletionTimeTag = "MyFileDeletionTimeTag";

	private final static String[] indexableFields = { idTag, identifierTag , fileDeletionTimeTag };

	public RmIndexAdvertisement() {
		// Accepting default values
	}

	public RmIndexAdvertisement(Element root) {

		// Retrieving the elements
		TextElement myTextElement = (TextElement) root;

		Enumeration theElements = myTextElement.getChildren();

		while (theElements.hasMoreElements()) {

			TextElement theElement = (TextElement) theElements.nextElement();

			ProcessElement(theElement);

		}
	}

	public void ProcessElement(TextElement theElement) {

		String theElementName = theElement.getName();
		String theTextValue = theElement.getTextValue();

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

		if (theElementName.compareTo(fileDeletionTimeTag) == 0) {
			fileDeletionTime = theTextValue;
			return;
		}
	}

	public Document getDocument(MimeMediaType theMimeMediaType) {

		// Creating document
		StructuredDocument theResult = StructuredDocumentFactory
				.newStructuredDocument(theMimeMediaType, advertisementType);

		// Adding elements
		Element myTempElement;

		myTempElement = theResult.createElement(fileNameTag, fileName);
		theResult.appendChild(myTempElement);

		myTempElement = theResult.createElement(fileDeletionTimeTag,
				fileDeletionTime);
		theResult.appendChild(myTempElement);
		
		myTempElement = theResult.createElement(identifierTag,
				identifier);
		theResult.appendChild(myTempElement);

		return theResult;
	}

	public void setID(ID theID) {
		advertisementID = theID;
	}

	public ID getID() {
		return advertisementID;
	}

	public String[] getIndexFields() {
		return indexableFields;
	}

	public void setFileName(String newName) {
		fileName = newName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileDeletionTime(long time) {
		fileDeletionTime = Long.toString(time);
	}

	public long getFileDeletionTime() {
		return Long.parseLong(fileDeletionTime);
	}

	@Override
	public RmIndexAdvertisement clone() throws CloneNotSupportedException {

		RmIndexAdvertisement result = (RmIndexAdvertisement) super.clone();

		result.advertisementID = this.advertisementID;
		result.fileName = this.fileName;
		result.fileDeletionTime = this.fileDeletionTime;

		return result;
	}

	@Override
	public String getAdvType() {
		return RmIndexAdvertisement.class.getName();
	}

	public static String getAdvertisementType() {
		return advertisementType;
	}

	public static class Instantiator implements
	AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return RmIndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new RmIndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new RmIndexAdvertisement(root);
		}
	}
}
