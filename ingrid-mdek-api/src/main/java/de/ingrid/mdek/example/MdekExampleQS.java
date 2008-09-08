package de.ingrid.mdek.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekClient;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.utils.IngridDocument;

public class MdekExampleQS {

	private static Map readParameters(String[] args) {
		Map<String, String> argumentMap = new HashMap<String, String>();
		for (int i = 0; i < args.length; i = i + 2) {
			argumentMap.put(args[i], args[i + 1]);
		}
		return argumentMap;
	}

	private static void printUsage() {
		System.err.println("Usage: " + MdekClient.class.getName()
				+ "--descriptor <communication.properties> [--threads 1]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Map map = readParameters(args);
		if (map.size() < 1) {
			printUsage();
		}

		// read passed Parameters
		System.out.println("\n###### PARAMS ######");
		Integer numThreads = 1;
		if (map.get("--threads") != null) {
			numThreads = new Integer((String) map.get("--threads"));
			if (numThreads < 1) {
				numThreads = 1;
			}
		}
		System.out.println("THREADS: " + numThreads);

		// INITIALIZE CENTRAL MDEK CALLER !
		System.out.println("\n###### start mdek iBus ######\n");
		MdekCaller.initialize(new File((String) map.get("--descriptor")));
		IMdekCaller mdekCaller = MdekCaller.getInstance();

		// wait till iPlug registered !
		System.out.println("\n###### waiting for mdek iPlug to register ######\n");
		boolean plugRegistered = false;
		while (!plugRegistered) {
			List<String> iPlugs = mdekCaller.getRegisteredIPlugs();
			if (iPlugs.size() > 0) {
				plugRegistered = true;
				System.out.println("Registered iPlugs: " + iPlugs);
			} else {
				System.out.println("wait ...");
				Thread.sleep(2000);
			}
		}

		// start threads calling job
		System.out.println("\n###### OUTPUT THREADS ######\n");
		MdekExampleQSThread[] threads = new MdekExampleQSThread[numThreads];
		// initialize
		for (int i=0; i<numThreads; i++) {
			threads[i] = new MdekExampleQSThread(i+1);
		}
		// fire
		for (int i=0; i<numThreads; i++) {
			threads[i].start();
		}

		// wait till all threads are finished
		boolean threadsFinished = false;
		while (!threadsFinished) {
			threadsFinished = true;
			for (int i=0; i<numThreads; i++) {
				if (threads[i].isRunning()) {
					threadsFinished = false;
					Thread.sleep(500);
					break;
				}
			}
		}

		// shutdown mdek
		MdekCaller.shutdown();
/*
		System.out.println("END OF EXAMPLE (end of main())");

		System.out.println(Thread.activeCount());
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		for (StackTraceElement[] st : allStackTraces.values()) {
			for (StackTraceElement stackTraceElement : st) {
		        System.out.println(stackTraceElement);
            }
            System.out.println("===============");
		}

//		System.exit(0);
//		return;
*/
	}
}

class MdekExampleQSThread extends Thread {

	private int threadNumber;
	private boolean isRunning = false;

	private MdekExampleSupertool supertool;

	public MdekExampleQSThread(int threadNumber)
	{
		this.threadNumber = threadNumber;
		
		supertool = new MdekExampleSupertool("mdek-iplug-idctest", "EXAMPLE_USER_" + threadNumber);
}

	public void run() {
		isRunning = true;

		long exampleStartTime = System.currentTimeMillis();

		boolean alwaysTrue = true;

		IngridDocument doc;
		IngridDocument newDoc;
		List<IngridDocument> docList;

		// NI catalog

		// OBJECTS
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";

		// ADDRESSES
		// TOP ADDRESS
		String topAddressUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		// PARENT ADDRESS (sub address of topUuid)
		String parentAddressUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		// PERSON ADDRESS (sub address of parentUuid)
		String personAddressUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";


		System.out.println("\n\n----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n\n----- ENABLE WORKFLOW in catalog -----");
		IngridDocument catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n\n----- backend version -----");
		supertool.getVersion();


// ====================
// test single stuff
// -----------------------------------
/*
		// Test new METADATA TABLE
		// -----------------------

		// ============== OBJECTS =====================

		System.out.println("\n\n----- Test METADATA EXISTING OBJECT -----");
		System.out.println("\n----- object details -----");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change and store existing object EXPIRY STATE etc. -> working copy ! -----");
		doc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.TO_BE_EXPIRED.getDbValue());
		doc.put(MdekKeys.LASTEXPORT_TIME, MdekUtils.dateToTimestamp(new Date()));
		doc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
		supertool.storeObject(doc, true);

		System.out.println("\n----- update object part EXPIRY STATE in working copy (exists!) -----");
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, objUuid);
		doc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.EXPIRED.getDbValue());
		supertool.updateObjectPart(doc, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- verify update  -----");
		supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, false);

		System.out.println("\n----- verify original object details again -----");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		
		// -----------------------------------
		System.out.println("\n\n----- Test METADATA NEW OBJECT -----");
		System.out.println("\n----- load initial data from parent " + objUuid + " -----");
		newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, objUuid);
		newDoc = supertool.getInitialObject(newDoc);

		System.out.println("\n----- extend initial object and store -> DEFAULT METADATA -----");
		// extend initial object with own data !
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		doc = supertool.storeObject(newDoc, true);
		String newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(newUuid, true);

		System.out.println("\n----- publish new object immediately -> DEFAULT METADATA -----");
		doc = supertool.publishObject(newDoc, true, true);
		newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- DELETE new object -----");
		supertool.deleteObject(newUuid, true);

		System.out.println("\n----- publish new object with INDIVIDUAL METADATA immediately -> INDIVIDUAL METADATA -----");
		newDoc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.TO_BE_EXPIRED.getDbValue());
		newDoc.put(MdekKeys.LASTEXPORT_TIME, MdekUtils.dateToTimestamp(new Date()));
		newDoc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
		doc = supertool.publishObject(newDoc, true, true);
		newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- copy new object -> copy with INDIVIDUAL METADATA -----");
		doc = supertool.copyObject(newUuid, null, false);
		String copyUuid = doc.getString(MdekKeys.UUID);
		doc = supertool.fetchObject(copyUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- DELETE new copy object -----");
		supertool.deleteObject(copyUuid, true);

		System.out.println("\n----- DELETE new object -----");
		supertool.deleteObject(newUuid, true);


		// ============== ADDRESSES =====================

		System.out.println("\n\n----- Test METADATA EXISTING ADDRESS -----");
		System.out.println("\n----- address details -----");
		doc = supertool.fetchAddress(personAddressUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- change and store existing address EXPIRY STATE etc. -> working copy ! -----");
		doc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.TO_BE_EXPIRED.getDbValue());
		doc.put(MdekKeys.LASTEXPORT_TIME, MdekUtils.dateToTimestamp(new Date()));
		doc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
		doc = supertool.storeAddress(doc, true);

		System.out.println("\n----- update address part EXPIRY STATE in working copy (exists!) -----");
		doc = new IngridDocument();
		doc.put(MdekKeys.UUID, personAddressUuid);
		doc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.EXPIRED.getDbValue());
		supertool.updateAddressPart(doc, IdcEntityVersion.WORKING_VERSION);

		System.out.println("\n----- verify update  -----");
		supertool.fetchAddress(personAddressUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(personAddressUuid, false);

		System.out.println("\n----- original address details again -----");
		doc = supertool.fetchAddress(personAddressUuid, Quantity.DETAIL_ENTITY);
		
		// ===================================
		System.out.println("\n\n----- Test METADATA NEW ADDRESS -----");
		System.out.println("\n----- load initial data from parent " + parentAddressUuid + " -----");
		newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, parentAddressUuid);
		newDoc = supertool.getInitialAddress(newDoc);

		System.out.println("\n----- extend initial address and store -> DEFAULT METADATA -----");
		newDoc.put(MdekKeys.NAME, "testNAME");
		newDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.EINHEIT.getDbValue());
		// email has to exist !
		docList = (List<IngridDocument>) newDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newDoc.put(MdekKeys.COMMUNICATION, docList);
		doc = supertool.storeAddress(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(newUuid, true);

		System.out.println("\n----- publish new address immediately -> DEFAULT METADATA -----");
		doc = supertool.publishAddress(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- DELETE new address -----");
		supertool.deleteAddress(newUuid, true);

		System.out.println("\n----- publish new address with INDIVIDUAL METADATA immediately -> INDIVIDUAL METADATA -----");
		newDoc.put(MdekKeys.EXPIRY_STATE, MdekUtils.ExpiryState.TO_BE_EXPIRED.getDbValue());
		newDoc.put(MdekKeys.LASTEXPORT_TIME, MdekUtils.dateToTimestamp(new Date()));
		newDoc.put(MdekKeys.MARK_DELETED, MdekUtils.YES);
		doc = supertool.publishAddress(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);

		System.out.println("\n----- copy new address -> copy with INDIVIDUAL METADATA -----");
		doc = supertool.copyAddress(newUuid, parentAddressUuid, false, false);
		copyUuid = doc.getString(MdekKeys.UUID);
		doc = supertool.fetchAddress(copyUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- DELETE new copy address -----");
		supertool.deleteAddress(copyUuid, true);

		System.out.println("\n----- DELETE new address -----");
		supertool.deleteAddress(newUuid, true);

		// -----------------------------------

		if (alwaysTrue) {
			isRunning = false;
			return;
		}
*/
// ===================================

		supertool.setFullOutput(true);

		System.out.println("\n\n=========================");
		System.out.println("QS OBJECT");
		System.out.println("=========================");
		
		// -----------------------------------
		System.out.println("\n\n----- search expired=INITIAL objects and extract various data by hql to MAP -----");
		String hqlQuery = "select obj.id, obj.objUuid, " +
			"oMeta.id, oMeta.expiryState, oMeta.lastexportTime, " +
			"addr.id, addr.adrUuid, " +
			"comm.adrId, comm.commValue " +
		"from ObjectNode oNode " +
			"inner join oNode.t01ObjectPublished obj " +
			"inner join obj.objectMetadata oMeta, " +
			"AddressNode as aNode " +
			"inner join aNode.t02AddressPublished addr " +
			"inner join addr.t021Communications comm " +
		"where " +
			"oMeta.expiryState = " + ExpiryState.INITIAL.getDbValue() +
			" and obj.responsibleUuid = aNode.addrUuid " +
			" and comm.commtypeKey = " + MdekUtils.COMM_TYPE_EMAIL;
		doc = supertool.queryHQLToMap(hqlQuery, 10);
		
		List<IngridDocument> hits = (List<IngridDocument>) doc.get(MdekKeys.OBJ_ENTITIES);
		for (IngridDocument hit : hits) {
			// get enum const from database value.
			ExpiryState stateEnumConst =
				EnumUtil.mapDatabaseToEnumConst(ExpiryState.class, hit.get("oMeta.expiryState"));
			System.out.println("  expiryState: " + stateEnumConst + " email: " + hit.get("comm.commValue"));
		}

		// -----------------------------------
		System.out.println("\n\n=========================");
		System.out.println("QS ADDRESS");
		System.out.println("=========================");

		// -----------------------------------
		System.out.println("\n\n----- search expired=INITIAL addresses and extract various data by hql to MAP -----");
		hqlQuery = "select a.id, a.adrUuid, " +
			"aMeta.id, aMeta.expiryState, aMeta.lastexportTime, " +
			"aResp.id, aResp.adrUuid, " +
			"comm.adrId, comm.commValue " +
		"from AddressNode aNode " +
			"inner join aNode.t02AddressPublished a " +
			"inner join a.addressMetadata aMeta, " +
			"AddressNode as aRespNode " +
			"inner join aRespNode.t02AddressPublished aResp " +
			"inner join aResp.t021Communications comm " +
		"where " +
			"aMeta.expiryState = " + ExpiryState.INITIAL.getDbValue() +
			" and a.responsibleUuid = aRespNode.addrUuid " +
			" and comm.commtypeKey = " + MdekUtils.COMM_TYPE_EMAIL;
		doc = supertool.queryHQLToMap(hqlQuery, 10);
		
		hits = (List<IngridDocument>) doc.get(MdekKeys.ADR_ENTITIES);
		for (IngridDocument hit : hits) {
			// get enum const from database value.
			ExpiryState stateEnumConst =
				EnumUtil.mapDatabaseToEnumConst(ExpiryState.class, hit.get("aMeta.expiryState"));
			System.out.println("  expiryState: " + stateEnumConst + " email: " + hit.get("comm.commValue"));
		}

// ===================================

		long exampleEndTime = System.currentTimeMillis();
		long exampleNeededTime = exampleEndTime - exampleStartTime;
		System.out.println("\n----------");
		System.out.println("EXAMPLE EXECUTION TIME: " + exampleNeededTime + " ms");

		isRunning = false;
	}

	public void start() {
		this.isRunning = true;
		super.start();
	}

	public boolean isRunning() {
		return isRunning;
	}
}
