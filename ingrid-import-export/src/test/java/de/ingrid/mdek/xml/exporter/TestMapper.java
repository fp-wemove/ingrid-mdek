package de.ingrid.mdek.xml.exporter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.utils.IngridDocument;

public class TestMapper {
	private static IMdekClientCaller mdekClientCaller;
	private static IMdekCallerObject mdekCallerObject;
	private static IMdekCallerAddress mdekCallerAddress;
	private static IMdekCallerQuery mdekCallerQuery;
	private static IMdekCallerCatalog mdekCallerCatalog;
	private static IMdekCallerSecurity mdekCallerSecurity;

	private static IngridXMLStreamWriter writer; 
	private static OutputStream out;

	private static String plugId;
	private static String userId = "admin";

	private final static int EXPORT_NUM_ADDRESSES = 10;
	private final static int EXPORT_NUM_OBJECTS = 10;

	@BeforeClass
	public static void setup() {
		setupConnection();
		setupWriter();
	}

	@AfterClass
	public static void shutdown() {
		shutdownConnection();
		shutdownWriter();
	}

	private static void shutdownConnection() {
		MdekCaller.shutdown();
	}

	private static void setupConnection() {
		File communicationProperties = new File("src/test/resources/communication.properties");
		if (communicationProperties == null || !(communicationProperties instanceof File) || !communicationProperties.exists()) {
			throw new IllegalStateException(
					"Please specify the location of the communication.properties file via the Property 'mdekClientCaller.properties' in /src/resources/mdek.properties");
		}
		MdekClientCaller.initialize(communicationProperties);
		mdekClientCaller = MdekClientCaller.getInstance();

		MdekCallerObject.initialize(mdekClientCaller);
		MdekCallerAddress.initialize(mdekClientCaller);
		MdekCallerQuery.initialize(mdekClientCaller);
		MdekCallerCatalog.initialize(mdekClientCaller);
		MdekCallerSecurity.initialize(mdekClientCaller);

		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
		mdekCallerQuery = MdekCallerQuery.getInstance();
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
		mdekCallerSecurity = MdekCallerSecurity.getInstance();

		waitForConnection();
	}

	private static void waitForConnection() {
		while (mdekClientCaller.getRegisteredIPlugs().size() == 0) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		plugId = mdekClientCaller.getRegisteredIPlugs().get(0);
	}

	private static void setupWriter() {
		try {
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			out = new BufferedOutputStream(new FileOutputStream("src/test/resources/test.xml", false));
			writer = new IngridXMLStreamWriter(outputFactory.createXMLStreamWriter(out, "UTF-8"));
			writer.writeStartDocument();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void shutdownWriter() {
		try {
			writer.writeEndDocument();
			writer.flush();
			writer.close();
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWriteObjects() throws XMLStreamException {
		String hqlQuery = "from ObjectNode";
		int startHit = 0;
		int numHits = EXPORT_NUM_OBJECTS;
		IngridDocument objectNodesResponse = mdekCallerQuery.queryHQL(plugId, hqlQuery, startHit, numHits, userId);
		IngridDocument objectNodes = mdekClientCaller.getResultFromResponse(objectNodesResponse);

		List<IngridDocument> objEntities = objectNodes.getArrayList(MdekKeys.OBJ_ENTITIES);

		long startTime = System.currentTimeMillis();

		writer.writeStartIngridObjects();
		for (IngridDocument objEntity : objEntities) {
			String uuid = objEntity.getString(MdekKeys.UUID);
			IngridDocument obj = getObject(uuid);

			if (obj != null) {
//				System.out.println("Mapping object with uuid '"+uuid+"': "+obj);
				writer.writeIngridObject(obj);
			}
		}
		writer.writeEndIngridObjects();

		long endTime = System.currentTimeMillis();
		System.out.format("Object Mapping took %d milliseconds.\n", endTime - startTime);
	}

	@Test
	public void testWriteAddresses() throws XMLStreamException {
		String hqlQuery = "from AddressNode";
		int startHit = 0;
		int numHits = EXPORT_NUM_ADDRESSES;
		IngridDocument addressNodesResponse = mdekCallerQuery.queryHQL(plugId, hqlQuery, startHit, numHits, userId);
		IngridDocument addressNodes = mdekClientCaller.getResultFromResponse(addressNodesResponse);

		List<IngridDocument> adrEntities = addressNodes.getArrayList(MdekKeys.ADR_ENTITIES);

		long startTime = System.currentTimeMillis();

		writer.writeStartIngridAddresses();
		for (IngridDocument adrEntity : adrEntities) {
			String uuid = adrEntity.getString(MdekKeys.UUID);
			IngridDocument adr = getAddress(uuid);

			if (adr != null) {
//				System.out.println("Mapping address with uuid '"+uuid+"': "+adr);
				writer.writeIngridAddress(adr);
			}
		}
		writer.writeEndIngridAddresses();

		long endTime = System.currentTimeMillis();
		System.out.format("Address Mapping took %d milliseconds.\n", endTime - startTime);
	}

	@Test
	public void testWriteAdditionalFields() throws XMLStreamException {

		IngridDocument additionalFieldsResponse = mdekCallerCatalog.getSysAdditionalFields(plugId, null, null, userId);
		IngridDocument additionalFields = mdekClientCaller.getResultFromResponse(additionalFieldsResponse);

		long startTime = System.currentTimeMillis();

		writer.writeStartAdditionalFields();
		for (IngridDocument additionalField : (Collection<IngridDocument>) additionalFields.values()) {
			writer.writeAdditionalField(additionalField);
		}
		writer.writeEndAdditionalFields();

		long endTime = System.currentTimeMillis();
		System.out.format("Address Mapping took %d milliseconds.\n", endTime - startTime);
	}

	private IngridDocument getObject(String uuid) {
		IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userId);
		return mdekClientCaller.getResultFromResponse(objDocResponse);
	}
	private IngridDocument getAddress(String uuid) {
		IngridDocument adrDocResponse = mdekCallerAddress.fetchAddress(plugId, uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userId);
		return mdekClientCaller.getResultFromResponse(adrDocResponse);
	}
}