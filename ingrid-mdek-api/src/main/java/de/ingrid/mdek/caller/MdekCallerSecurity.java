package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.utils.IngridDocument;



/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning SECURITY / USER MANAGEMENT.
 */
public class MdekCallerSecurity extends MdekCallerAbstract implements IMdekCallerSecurity {

	private final static Logger log = Logger.getLogger(MdekCallerSecurity.class);

	private static MdekCallerSecurity myInstance;
	private IMdekCaller mdekCaller;

	// Jobs
	private static String MDEK_IDC_SECURITY_JOB_ID = "de.ingrid.mdek.job.MdekIdcSecurityJob";

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 * @param communicationProperties props specifying communication
	 */
	public static synchronized void initialize(IMdekCaller mdekCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerSecurity(mdekCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

    private MdekCallerSecurity() {}

    private MdekCallerSecurity(IMdekCaller mdekCaller) {
    	this.mdekCaller = mdekCaller;
    }

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerSecurity getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerSecurity.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument getGroups(String plugId,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getGroups", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getGroupDetails(String plugId,
			String name,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.NAME, name);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getGroupDetails", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument createGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId) {
		groupDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		groupDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("createGroup", groupDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument storeGroup(String plugId,
			IngridDocument groupDoc,
			boolean refetchAfterStore,
			String userId) {
		groupDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		groupDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeGroup", groupDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument createUser(String plugId, IngridDocument userDoc, boolean refetchAfterStore, String userId) {
		userDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		userDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("createUser", userDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument storeUser(String plugId, IngridDocument userDoc, boolean refetchAfterStore, String userId) {
		userDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		userDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("storeUser", userDoc);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getCatalogAdmin(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = mdekCaller.setUpJobMethod("getCatalogAdmin", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument deleteGroup(String plugId, Long idcGroupId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_GROUP_ID, idcGroupId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteGroup", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument deleteUser(String plugId, Long idcUserId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_USER_ID, idcUserId);
		List jobMethods = mdekCaller.setUpJobMethod("deleteUser", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}

	public IngridDocument getUserDetails(String plugId, String addrUuid, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		jobParams.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, addrUuid);
		List jobMethods = mdekCaller.setUpJobMethod("getUserDetails", jobParams);
		return mdekCaller.callJob(plugId, MDEK_IDC_SECURITY_JOB_ID, jobMethods);
	}
}
