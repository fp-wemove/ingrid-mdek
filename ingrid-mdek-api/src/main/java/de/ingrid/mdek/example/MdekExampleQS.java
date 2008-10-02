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
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.MdekUtils.IdcEntitySelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
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
		String topObjUuid = "3866463B-B449-11D2-9A86-080000507261";
		// underneath upper top node
		// 3866463B-B449-11D2-9A86-080000507261
		//  38664688-B449-11D2-9A86-080000507261
		//   15C69C20-FE15-11D2-AF34-0060084A4596
		//    2C997C68-2247-11D3-AF51-0060084A4596
		String objUuid = "2C997C68-2247-11D3-AF51-0060084A4596";
		// all further top nodes (5 top nodes at all)
		String topObjUuid2 = "79297FDD-729B-4BC5-BF40-C1F3FB53D2F2";
//		String topObjUuid3 = "38665183-B449-11D2-9A86-080000507261";
//		String topObjUuid4 = "7937CA1A-3F3A-4D36-9EBA-E2F55190811A";
//		String topObjUuid5 = "3892B136-D1F3-4E45-9E5F-E1CEF117AA74";

		// ADDRESSES
		// TOP ADDRESS
		String topAddrUuid = "3761E246-69E7-11D3-BB32-1C7607C10000";
		// PARENT ADDRESS (sub address of topUuid)
		String parentAddrUuid = "C5FEA801-6AB2-11D3-BB32-1C7607C10000";
		// PERSON ADDRESS (sub address of parentUuid)
		String personAddrUuid = "012CBA17-87F6-11D4-89C7-C1AAE1E96727";
		// further non free top addresses (110 top nodes at all)
		String topAddrUuid2 = "386644BF-B449-11D2-9A86-080000507261";
//		String topAddrUuid3 = "4E9DD4F5-BC14-11D2-A63A-444553540000";


		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		doc = supertool.getCatalogAdmin();
		Long catalogAdminId = (Long) doc.get(MdekKeysSecurity.IDC_USER_ID);
		String catalogAdminUuid = doc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- ENABLE WORKFLOW in catalog -----");
		IngridDocument catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.YES);
		catDoc = supertool.storeCatalog(catDoc, true);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- backend version -----");
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

		System.out.println("\n\n=========================");
		System.out.println("QS Set up User/Group structure for testing");
		System.out.println("=========================");

		System.out.println("\n\n=============================================");
		System.out.println("----- create new GROUP_QA -----");
		IngridDocument grpQADoc = new IngridDocument();
		grpQADoc.put(MdekKeys.NAME, "TEST Gruppe1 QA");
		grpQADoc = supertool.createGroup(grpQADoc, true);
		Long grpQAId = (Long) grpQADoc.get(MdekKeysSecurity.IDC_GROUP_ID);

		System.out.println("\n--- Add Permissions to GROUP_QA -----");
		supertool.addUserPermissionToGroupDoc(grpQADoc, MdekUtilsSecurity.IdcPermission.QUALITY_ASSURANCE);
		supertool.addObjPermissionToGroupDoc(grpQADoc, topObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addObjPermissionToGroupDoc(grpQADoc, topObjUuid2, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(grpQADoc, topAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addAddrPermissionToGroupDoc(grpQADoc, topAddrUuid2, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		grpQADoc = supertool.storeGroup(grpQADoc, true, false);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR' in 'GROUP_QA' -----");
		IngridDocument usrGrpQADoc = new IngridDocument();
		usrGrpQADoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "38664584-B449-11D2-9A86-080000507261");
		usrGrpQADoc.put(MdekKeysSecurity.IDC_GROUP_ID, grpQAId);
		usrGrpQADoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		usrGrpQADoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		usrGrpQADoc = supertool.createUser(usrGrpQADoc, true);
		Long usrGrpQAId = (Long) usrGrpQADoc.get(MdekKeysSecurity.IDC_USER_ID);
		String usrGrpQAUuid = usrGrpQADoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

		System.out.println("\n\n=============================================");
		System.out.println("----- create new GROUP_NO_QA -----");
		IngridDocument grpNoQADoc = new IngridDocument();
		grpNoQADoc.put(MdekKeys.NAME, "TEST Gruppe2 NO QA");
		grpNoQADoc = supertool.createGroup(grpNoQADoc, true);
		Long grpNoQAId = (Long) grpNoQADoc.get(MdekKeysSecurity.IDC_GROUP_ID);

		System.out.println("\n--- Add Permissions to GROUP_NO_QA -----");
		supertool.addObjPermissionToGroupDoc(grpNoQADoc, topObjUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addObjPermissionToGroupDoc(grpNoQADoc, topObjUuid2, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		supertool.addAddrPermissionToGroupDoc(grpNoQADoc, topAddrUuid, MdekUtilsSecurity.IdcPermission.WRITE_TREE);
		supertool.addAddrPermissionToGroupDoc(grpNoQADoc, topAddrUuid2, MdekUtilsSecurity.IdcPermission.WRITE_SINGLE);
		grpNoQADoc = supertool.storeGroup(grpNoQADoc, true, false);

		System.out.println("\n----- create new user 'MD_ADMINISTRATOR' in 'GROUP_NO_QA' -----");
		IngridDocument usrGrpNoQADoc = new IngridDocument();
		usrGrpNoQADoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "10646604-D21F-11D2-BB32-006097FE70B1");
		usrGrpNoQADoc.put(MdekKeysSecurity.IDC_GROUP_ID, grpNoQAId);
		usrGrpNoQADoc.put(MdekKeysSecurity.IDC_ROLE, MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue());
		usrGrpNoQADoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, catalogAdminId);
		usrGrpNoQADoc = supertool.createUser(usrGrpNoQADoc, true);
		Long usrGrpNoQAId = (Long) usrGrpNoQADoc.get(MdekKeysSecurity.IDC_USER_ID);
		String usrGrpNoQAUuid = usrGrpNoQADoc.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);


		System.out.println("\n\n=========================");
		System.out.println("QS OBJECT");
		System.out.println("=========================");

		supertool.setFullOutput(false);

		System.out.println("\n\n=============================================");
		System.out.println("----- search expired=INITIAL objects and extract data by hql to MAP -----");
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

		System.out.println("\n\n=============================================");
		System.out.println("----- ASSIGN EXISTING OBJECT TO QA -----");

		System.out.println("\n----- object details -----");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));

		System.out.println("\n----- assign to QA -> working copy with status Q ! -----");
		doc = supertool.assignObjectToQA(doc, true);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));

		supertool.setFullOutput(true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add comment and store again -> still status Q with comment in working copy ! -----");
		supertool.addComment(doc, "TEST COMMENT QA");
		doc = supertool.storeObject(doc, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- CHECK PERMISSIONS ON Q-OBJECT -----");
		
		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as catalog admin -> all permissions -----");

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as QA user -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as NON QA user with write tree -> only subtree permission -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> only subtree permission !!! -----");
		supertool.getObjectPermissions(objUuid, true);

		supertool.setFullOutput(false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);


		System.out.println("\n\n=============================================");
		System.out.println("----- REASSIGN QA OBJECT TO Author -----");

		System.out.println("\n----- object details -----");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		System.out.println("  REASSIGNER_UUID: " + doc.get(MdekKeys.REASSIGNER_UUID));
		System.out.println("  REASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.REASSIGN_TIME)));

		System.out.println("\n----- reassign to Author -> working copy with status R ! -----");
		doc = supertool.reassignObjectToAuthor(doc, true);
		System.out.println("  REASSIGNER_UUID: " + doc.get(MdekKeys.REASSIGNER_UUID));
		System.out.println("  REASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.REASSIGN_TIME)));

		System.out.println("\n----- store again -> still status R ! -----");
		doc = supertool.storeObject(doc, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- CHECK PERMISSIONS ON R-OBJECT -----");
		
		supertool.setFullOutput(true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as catalog admin -> all permissions -----");

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as QA user -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as NON QA user with write tree -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getObjectPermissions(objUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions !!! -----");
		supertool.getObjectPermissions(objUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add comment as AUTHOR and store again -> still status R with comment in working copy ! -----");
		supertool.addComment(doc, "TEST COMMENT AUTHOR");
		doc = supertool.storeObject(doc, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- TEST TAKE OVER OF COMMENTS -----");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- discard changes -> back to published version  -> TOOK OVER COMMENTS FROM WORKING VERSION -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		
		System.out.println("\n----- store WITHOUT comments again -> working version WITHOUT comments -----");
		doc.put(MdekKeys.COMMENT_LIST, null);
		doc = supertool.storeObject(doc, true);

		System.out.println("\n----- discard changes -> back to published version  -> TOOK OVER EMPTY COMMENTS FROM WORKING VERSION -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);

		supertool.setFullOutput(false);


		System.out.println("\n\n=============================================");
		System.out.println("----- ASSIGN NEW OBJECT (subobject of object in group!) TO QA -----");

		System.out.println("\n----- create new object and assign to QA -> working copy ! -----");
		newDoc = supertool.newObjectDoc(objUuid);
		doc = supertool.assignObjectToQA(newDoc, true);
		String newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));

		
		System.out.println("\n\n=============================================");
		System.out.println("----- GET OBJECTS WHERE USER IS QA -> fetch different object states -----");

		System.out.println("\n----- create working copy \"in Bearbeitung\" and \"EXPIRED\" -----");
		doc = supertool.fetchObject(objUuid, Quantity.DETAIL_ENTITY);
		doc.put(MdekKeys.EXPIRY_STATE, ExpiryState.EXPIRED.getDbValue());
		supertool.storeObject(doc, true);
		
		// IF CATADMIN -> ALL OBJECTS !!!
		System.out.println("\n---------------------------------------------");
		System.out.println("----- CATADMIN IS QA FOR ALL OBJECTS -> getQAObjects delivers ALL ENTITIES -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);
		
		int maxNum = 50;

		supertool.getQAObjects(null, null, maxNum);
		supertool.getQAObjects(null, null, 3);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAObjects(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- USER WITH QA -> getQAObjects delivers ALL ENTITIES OF GROUP -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		supertool.getQAObjects(null, null, maxNum);
		supertool.getQAObjects(null, null, 2);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAObjects(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- USER NO_QA -> getQAObjects delivers NO ENTITIES -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		supertool.getQAObjects(null, null, maxNum);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAObjects(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAObjects(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);


		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n---------------------------------------------");
		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(objUuid, true);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteObjectWorkingCopy(newUuid, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- DELETE / PUBLISH OBJECT -----");

		System.out.println("\n-------------------------------------");
		System.out.println("----- DELETE NEW OBJECT POSSIBLE AS NON QA -----");
		
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- CREATE NEW OBJECT (subobject of object in group!) and store -> ONLY working copy ! -----");
		newDoc = supertool.newObjectDoc(objUuid);
		doc = supertool.storeObject(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("\n----- DELETE -> possible as non QA user because NOT PUBLISHED ! -----");
		supertool.deleteObject(newUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- PUBLISH OF NEW OBJECT NOT POSSIBLE AS NON QA -----");
		
		System.out.println("\n----- CREATE NEW OBJECT AGAIN and store -> ONLY working copy ! -----");
		newDoc = supertool.newObjectDoc(objUuid);
		doc = supertool.storeObject(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("\n----- try to publish -> ERROR: not QA -----");
		supertool.publishObject(doc, true, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- QA ASSIGNED OBJECT CANNOT BE DELETED as NON QA -----");

		System.out.println("\n----- assign to QA (still not published) and try to delete -> ERROR: not QA (Permission error) -----");
		doc = supertool.assignObjectToQA(doc, true);
		supertool.deleteObjectWorkingCopy(newUuid, false);
		supertool.deleteObject(newUuid, false);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- PUBLISH QA ASSIGNED OBJECT as QA -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);		
		System.out.println("\n----- publish -> OK as QA user ! -----");
		doc = supertool.publishObject(doc, true, true);
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE PUBLISHED OBJECT as NON QA -> mark deleted and assigned to QA -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- DELETE published object as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteObject(newUuid, true);
		doc = supertool.fetchObject(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- REASSIGN MARK DELETED OBJECT BACK TO AUTHOR as QA -> MARK DELETED REMOVED ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- Reassign object back to Author -> MARK DELETED REMOVED ! -----");
		doc = supertool.reassignObjectToAuthor(doc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DISCARD REASSIGNED OBJECT as NON QA -> published version ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- discard changes -> OK, back to published version ! -----");
		supertool.deleteObjectWorkingCopy(newUuid, false);
		doc = supertool.fetchObject(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE published object as non QA and publish it as QA -> mark deleted gone -----");

		System.out.println("\n----- DELETE published object as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteObject(newUuid, true);
		doc = supertool.fetchObject(newUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- publish as QA -> all changes published, NOT MARKED DELETED ! -----");
		doc = supertool.publishObject(doc, true, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE published object as non QA and COPY it as QA -> mark deleted gone in result -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- DELETE published object as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteObject(newUuid, true);
		doc = supertool.fetchObject(newUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- COPY as QA -> new object, NOT MARKED DELETED ! -----");
		doc = supertool.copyObject(newUuid, objUuid, false);
		String copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchObject(copiedUuid, Quantity.DETAIL_ENTITY);
		System.out.println("----- DELETE copy of new object as QA -> FULL DELETE -----");
		supertool.deleteObject(copiedUuid, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- MOVE assigned object as QA -> STILL mark deleted -----");

		System.out.println("\n----- MOVE as QA -> STILL MARKED DELETED ! -----");
		doc = supertool.moveObject(newUuid, objUuid, true);
		supertool.fetchObject(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE new object as QA -> FULL DELETE -----");
		supertool.deleteObject(newUuid, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("QS ADDRESS");
		System.out.println("=========================");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- search expired=INITIAL addresses and extract data by hql to MAP -----");
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

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- ASSIGN EXISTING ADDRESS TO QA -----");

		System.out.println("\n----- address details -----");
		doc = supertool.fetchAddress(personAddrUuid, Quantity.DETAIL_ENTITY);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));

		System.out.println("\n----- assign to QA -> working copy with status Q ! -----");
		doc = supertool.assignAddressToQA(doc, true);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));

		supertool.setFullOutput(true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add comment and store again -> still status Q with comment in working copy ! -----");
		supertool.addComment(doc, "TEST COMMENT QA");
		doc = supertool.storeAddress(doc, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- CHECK PERMISSIONS ON Q-ADDRESS -----");
		
		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as catalog admin -> all permissions -----");

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as QA user -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as NON QA user with write tree -> only subtree permission -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> only subtree permission !!! -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		supertool.setFullOutput(false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);


		System.out.println("\n\n=============================================");
		System.out.println("----- REASSIGN QA ADDRESS TO AUTHOR -----");

		System.out.println("\n----- address details -----");
		doc = supertool.fetchAddress(personAddrUuid, Quantity.DETAIL_ENTITY);
		System.out.println("  REASSIGNER_UUID: " + doc.get(MdekKeys.REASSIGNER_UUID));
		System.out.println("  REASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.REASSIGN_TIME)));

		System.out.println("\n----- reassign to Author -> working copy with status R ! -----");
		doc = supertool.reassignAddressToAuthor(doc, true);
		System.out.println("  REASSIGNER_UUID: " + doc.get(MdekKeys.REASSIGNER_UUID));
		System.out.println("  REASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.REASSIGN_TIME)));

		System.out.println("\n----- store again -> still status R ! -----");
		doc = supertool.storeAddress(doc, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- CHECK PERMISSIONS ON R-ADDRESS -----");
		
		supertool.setFullOutput(true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as catalog admin -> all permissions -----");

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as QA user -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- permissions as NON QA user with write tree -> all permissions -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		System.out.println("\n----- permissions WITHOUT workflow -> all permissions -----");
		supertool.getAddressPermissions(personAddrUuid, false);
		System.out.println("\n----- permissions WITH workflow -> all permissions !!! -----");
		supertool.getAddressPermissions(personAddrUuid, true);

		System.out.println("\n-------------------------------------");
		System.out.println("----- add comment as AUTHOR and store again -> still status R with comment in working copy ! -----");
		supertool.addComment(doc, "TEST COMMENT AUTHOR");
		doc = supertool.storeAddress(doc, true);

		
		System.out.println("\n\n=============================================");
		System.out.println("----- TEST TAKE OVER OF COMMENTS -----");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n-------------------------------------");
		System.out.println("----- discard changes -> back to published version  -> TOOK OVER COMMENTS FROM WORKING VERSION -----");
		supertool.deleteAddressWorkingCopy(personAddrUuid, true);
		doc = supertool.fetchAddress(personAddrUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n----- store WITHOUT comments again -> working version WITHOUT comments -----");
		doc.put(MdekKeys.COMMENT_LIST, null);
		doc = supertool.storeAddress(doc, true);

		System.out.println("\n----- discard changes -> back to published version -> TOOK OVER EMPTY COMMENTS FROM WORKING VERSION -----");
		supertool.deleteAddressWorkingCopy(personAddrUuid, true);
		doc = supertool.fetchAddress(personAddrUuid, Quantity.DETAIL_ENTITY);

		supertool.setFullOutput(false);

		
		System.out.println("\n\n=============================================");
		System.out.println("----- ASSIGN NEW ADDRESS TO QA -----");

		System.out.println("\n----- create new address and assign to QA -> working copy ! -----");
		newDoc = supertool.newAddressDoc(parentAddrUuid, AddressType.EINHEIT);
		doc = supertool.assignAddressToQA(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("  ASSIGNER_UUID: " + doc.get(MdekKeys.ASSIGNER_UUID));
		System.out.println("  ASSIGN_TIME: " + MdekUtils.timestampToDisplayDate(doc.getString(MdekKeys.ASSIGN_TIME)));


		System.out.println("\n\n=============================================");
		System.out.println("----- GET ADDRESSES WHERE USER IS QA -> fetch different address states -----");

		System.out.println("\n----- create working copy \"in Bearbeitung\" and \"EXPIRED\" -----");
		doc = supertool.fetchAddress(personAddrUuid, Quantity.DETAIL_ENTITY);
		doc.put(MdekKeys.EXPIRY_STATE, ExpiryState.EXPIRED.getDbValue());
		supertool.storeAddress(doc, true);
		
		// IF CATADMIN -> ALL OBJECTS !!!
		System.out.println("\n---------------------------------------------");
		System.out.println("----- CATADMIN IS QA FOR ALL ADDRESSES -> getQAAddresses delivers ALL ENTITIES -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);
		
		supertool.getQAAddresses(null, null, maxNum);
		supertool.getQAAddresses(null, null, 3);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAAddresses(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- USER WITH QA -> getQAAddresses delivers ALL ENTITIES OF GROUP -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);

		supertool.getQAAddresses(null, null, maxNum);
		supertool.getQAAddresses(null, null, 2);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAAddresses(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- USER NO_QA -> getQAAddresses delivers NO ENTITIES -----");
		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);

		supertool.getQAAddresses(null, null, maxNum);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, null, maxNum);
		supertool.getQAAddresses(null, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.IN_BEARBEITUNG, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, null, maxNum);
		supertool.getQAAddresses(WorkState.QS_UEBERWIESEN, IdcEntitySelectionType.QA_EXPIRY_STATE_EXPIRED, maxNum);


		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n---------------------------------------------");
		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(personAddrUuid, true);

		System.out.println("\n----- discard changes -> back to published version -----");
		supertool.deleteAddressWorkingCopy(newUuid, true);


		System.out.println("\n\n=============================================");
		System.out.println("----- DELETE / PUBLISH ADDRESS -----");

		System.out.println("\n-------------------------------------");
		System.out.println("----- DELETE NEW ADDRESS POSSIBLE AS NON QA -----");
		
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- CREATE NEW ADDRESS and store -> ONLY working copy ! -----");
		newDoc = supertool.newAddressDoc(parentAddrUuid, AddressType.EINHEIT);
		doc = supertool.storeAddress(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("\n----- DELETE -> possible as non QA user because NOT PUBLISHED ! -----");
		supertool.deleteAddress(newUuid, false);

		System.out.println("\n-------------------------------------");
		System.out.println("----- PUBLISH OF NEW ADDRESS NOT POSSIBLE AS NON QA -----");
		
		System.out.println("\n----- CREATE NEW ADDRESS AGAIN and store -> ONLY working copy ! -----");
		newDoc = supertool.newAddressDoc(parentAddrUuid, AddressType.EINHEIT);
		doc = supertool.storeAddress(newDoc, true);
		newUuid = doc.getString(MdekKeys.UUID);
		System.out.println("\n----- try to publish -> ERROR: not QA -----");
		supertool.publishAddress(doc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- QA ASSIGNED ADDRESS CANNOT BE DELETED as NON QA -----");

		System.out.println("\n----- assign to QA (still not published) and try to delete -> ERROR: not QA (Permission error) -----");
		doc = supertool.assignAddressToQA(doc, true);
		supertool.deleteAddressWorkingCopy(newUuid, false);
		supertool.deleteAddress(newUuid, false);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- PUBLISH QA ASSIGNED ADDRESS as QA -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- publish -> OK as QA user ! -----");
		doc = supertool.publishAddress(doc, true);
		
		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE PUBLISHED ADDRESS as NON QA -> mark deleted and assigned to QA -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- DELETE published address as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteAddress(newUuid, true);
		doc = supertool.fetchAddress(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- REASSIGN MARK DELETED ADDRESS BACK TO AUTHOR as QA -> MARK DELETED REMOVED ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- Reassign address back to Author -> MARK DELETED REMOVED ! -----");
		doc = supertool.reassignAddressToAuthor(doc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DISCARD REASSIGNED ADDRESS as NON QA -> published version ! -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- discard changes -> OK, back to published version ! -----");
		supertool.deleteAddressWorkingCopy(newUuid, false);
		doc = supertool.fetchAddress(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE published address as non QA and publish it as QA -> mark deleted gone -----");

		System.out.println("\n----- DELETE published address as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteAddress(newUuid, true);
		doc = supertool.fetchAddress(newUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- publish as QA -> all changes published, NOT MARKED DELETED ! -----");
		doc = supertool.publishAddress(doc, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE published address as non QA and COPY it as QA -> mark deleted gone in result -----");

		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO NON QA user -----");
		supertool.setCallingUser(usrGrpNoQAUuid);
		System.out.println("\n----- DELETE published address as non QA -> is MARKED AS DELETED and assigned to QA !-----");
		supertool.deleteAddress(newUuid, true);
		doc = supertool.fetchAddress(newUuid, Quantity.DETAIL_ENTITY);
		System.out.println("\n----- !!! SWITCH \"CALLING USER\" TO QA user -----");
		supertool.setCallingUser(usrGrpQAUuid);
		System.out.println("\n----- COPY as QA -> new address, NOT MARKED DELETED ! -----");
		doc = supertool.copyAddress(newUuid, parentAddrUuid, false, false);
		copiedUuid = doc.getString(MdekKeys.UUID);
		supertool.fetchAddress(copiedUuid, Quantity.DETAIL_ENTITY);
		System.out.println("----- DELETE copy of new object as QA -> FULL DELETE -----");
		supertool.deleteAddress(copiedUuid, true);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- MOVE assigned address as QA -> STILL mark deleted -----");

		System.out.println("\n----- MOVE as QA -> STILL MARKED DELETED ! -----");
		doc = supertool.moveAddress(newUuid, parentAddrUuid, false);
		supertool.fetchAddress(newUuid, Quantity.DETAIL_ENTITY);

		System.out.println("\n---------------------------------------------");
		System.out.println("----- DELETE new address as QA -> FULL DELETE -----");
		supertool.deleteAddress(newUuid, true);

// -----------------------------------

		System.out.println("\n\n=========================");
		System.out.println("CLEAN UP");
		System.out.println("=========================");

		System.out.println("\n-------------------------------------");
		System.out.println("----- !!! SWITCH \"CALLING USER\" TO CATALOG ADMIN (all permissions) -----");
		supertool.setCallingUser(catalogAdminUuid);

		System.out.println("\n----- delete groups, WITH FORCE DELETE WHEN HAVING USERS -> returns 'groupless' users of deleted group -----");
		supertool.deleteGroup(grpQAId, true);
		supertool.deleteGroup(grpNoQAId, true);

		System.out.println("\n----- delete users -----");
		supertool.deleteUser(usrGrpQAId);
		supertool.deleteUser(usrGrpNoQAId);

		System.out.println("\n\n---------------------------------------------");
		System.out.println("----- DISABLE WORKFLOW in catalog -----");
		catDoc = supertool.getCatalog();
		catDoc.put(MdekKeys.WORKFLOW_CONTROL, MdekUtils.NO);
		catDoc = supertool.storeCatalog(catDoc, true);

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
