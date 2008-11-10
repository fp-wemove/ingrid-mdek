package de.ingrid.mdek.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.caller.IMdekCaller;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.IMdekCallerAbstract.Quantity;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates common example methods ...
 */
public class MdekExampleSupertool {

	private IMdekCaller mdekCaller;
	private IMdekCallerSecurity mdekCallerSecurity;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerAddress mdekCallerAddress;
	private IMdekCallerCatalog mdekCallerCatalog;
	private IMdekCallerQuery mdekCallerQuery;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertool(String plugIdToCall,
			String callingUserUuid)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;

		mdekCaller = MdekCaller.getInstance();
		
		// and our specific job caller !
		MdekCallerSecurity.initialize(mdekCaller);
		mdekCallerSecurity = MdekCallerSecurity.getInstance();
		MdekCallerObject.initialize(mdekCaller);
		mdekCallerObject = MdekCallerObject.getInstance();
		MdekCallerAddress.initialize(mdekCaller);
		mdekCallerAddress = MdekCallerAddress.getInstance();
		MdekCallerCatalog.initialize(mdekCaller);
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
		MdekCallerQuery.initialize(mdekCaller);
		mdekCallerQuery = MdekCallerQuery.getInstance();
	}

	public void setPlugIdToCall(String plugIdToCall)
	{
		this.plugId = plugIdToCall;
	}

	public void setCallingUser(String callingUserUuid)
	{
		this.myUserUuid = callingUserUuid;
		System.out.println("\n###### NEW CALLING USER = " + callingUserUuid + " ######");		
	}
	public String getCallingUserUuid()
	{
		return myUserUuid;
	}

	public void setFullOutput(boolean doFullOutput)
	{
		this.doFullOutput = doFullOutput;
	}

	public void getVersion() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getVersion ######");
		startTime = System.currentTimeMillis();
		// ACHTUNG: ist DIREKT result ! sollte nie null sein (hoechstens leer)
		response = mdekCaller.getVersion(plugId);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");

		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("All entries in Map: ");
			Set<Map.Entry> entries = result.entrySet();
			for (Map.Entry entry : entries) {
				System.out.println("  " + entry);
			}
			System.out.println("Explicit read of entries: ");
			System.out.println("  API_BUILD_NAME: " + result.get(MdekKeys.API_BUILD_NAME));
			System.out.println("  API_BUILD_VERSION: " + result.get(MdekKeys.API_BUILD_VERSION));
			System.out.println("  API_BUILD_NUMBER: " + result.get(MdekKeys.API_BUILD_NUMBER));
			System.out.println("  API_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.API_BUILD_TIMESTAMP)));
			System.out.println("  SERVER_BUILD_NAME: " + result.get(MdekKeys.SERVER_BUILD_NAME));
			System.out.println("  SERVER_BUILD_VERSION: " + result.get(MdekKeys.SERVER_BUILD_VERSION));
			System.out.println("  SERVER_BUILD_NUMBER: " + result.get(MdekKeys.SERVER_BUILD_NUMBER));
			System.out.println("  SERVER_BUILD_TIMESTAMP (converted): " + MdekUtils.millisecToDisplayDateTime(result.getString(MdekKeys.SERVER_BUILD_TIMESTAMP)));

		} else {
			handleError(response);
		}
	}

	public IngridDocument getCatalog() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchCatalog ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.fetchCatalog(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugCatalogDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeCatalog(IngridDocument catDocIn,
			boolean refetchCatalog) {
		// check whether we have an address
		if (catDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetchCatalog) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeCatalog " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeCatalog(plugId, catDocIn, refetchCatalog, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugCatalogDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	/** Pass null if all gui elements requested */
	public IngridDocument getSysGuis(String[] guiIds) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysGuis ######");
		System.out.println("requested guiIds: " + guiIds);
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysGuis(plugId, guiIds, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			Set entries = result.entrySet();
			System.out.println("SUCCESS: " + entries.size() + " gui elements");
			for (Object entry : entries) {
				System.out.println("  " + entry);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeSysGuis(List<IngridDocument> sysGuis,
			boolean refetch) {
		// check whether we have data
		if (sysGuis == null || sysGuis.size() == 0) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeSysGuis " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.storeSysGuis(plugId, sysGuis, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			if (refetch) {
				Set entries = result.entrySet();
				System.out.println("SUCCESS: " + entries.size() + " gui elements");
				for (Object entry : entries) {
					System.out.println("  " + entry);
				}
			} else {
				System.out.println("SUCCESS: ");				
			}
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument getCatalogAdmin() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getCatalogAdmin ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getCatalogAdmin(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getGroups(boolean includeCatAdminGroup) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoCatAdminGroup = (includeCatAdminGroup) ? "WITH CatAdmin group" : "WITHOUT CatAdmin group";
		System.out.println("\n###### INVOKE getGroups " + infoCatAdminGroup + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroups(plugId, myUserUuid, includeCatAdminGroup);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.GROUPS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugGroupDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getGroupDetails(String grpName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getGroupDetails of group '" + grpName + "' ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getGroupDetails(plugId, grpName, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersOfGroup(String grpName) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUsersOfGroup of group '" + grpName + "' ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersOfGroup(plugId, grpName, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				debugUserDoc((IngridDocument)o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	private IngridDocument getUserDetails(String addrUuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserDetails ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserDetails(plugId, addrUuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}
	
	public IngridDocument getSubUsers(Long parentUserId) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSubUsers ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getSubUsers(plugId, parentUserId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				debugUserDoc((IngridDocument)o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersWithWritePermissionForObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForObject " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForObject(plugId, objUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				debugUserDoc((IngridDocument)o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUsersWithWritePermissionForAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String infoDetailedPermissions = (getDetailedPermissions) ? "WITH detailed permissions" : "WITHOUT detailed permissions";
		System.out.println("\n###### INVOKE getUsersWithWritePermissionForAddress " + infoDetailedPermissions + " ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUsersWithWritePermissionForAddress(plugId, addrUuid, myUserUuid,
				checkWorkflow,
				getDetailedPermissions);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeysSecurity.IDC_USERS);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				debugUserDoc((IngridDocument)o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectPermissions(String objUuid, boolean checkWorkflow) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPermissions ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getObjectPermissions(plugId, objUuid, myUserUuid, checkWorkflow);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressPermissions(String addrUuid, boolean checkWorkflow) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPermissions ######");
		System.out.println("  checkWorkflow: " + checkWorkflow);
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getAddressPermissions(plugId, addrUuid, myUserUuid, checkWorkflow);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getUserPermissions() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getUserPermissions ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.getUserPermissions(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugPermissionsDoc(result, "");
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getSysLists(Integer[] listIds, String language) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getSysLists, language: " + language + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerCatalog.getSysLists(plugId, listIds, language, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			Set<String> listKeys = result.keySet();
			System.out.println("SUCCESS: " + listKeys.size() + " sys-lists");
			for (String listKey : listKeys) {
				IngridDocument listDoc = (IngridDocument) result.get(listKey);
				List<IngridDocument> entryDocs =
					(List<IngridDocument>) listDoc.get(MdekKeys.LST_ENTRY_LIST);
				System.out.println("  " + listKey + ": " + entryDocs.size() + " entries");
				System.out.println("    " + entryDocs);				
			}
			
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectPath(plugId, uuidIn, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<String> uuidList = (List<String>) result.get(MdekKeys.PATH);
			System.out.println("SUCCESS: " + uuidList.size() + " levels");
			String indent = " ";
			for (String uuid : uuidList) {
				System.out.println(indent + uuid);
				indent += " ";
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressPath(String uuidIn) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressPath ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressPath(plugId, uuidIn, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<String> uuidList = (List<String>) result.get(MdekKeys.PATH);
			System.out.println("SUCCESS: " + uuidList.size() + " levels");
			String indent = " ";
			for (String uuid : uuidList) {
				System.out.println(indent + uuid);
				indent += " ";
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getInitialObject(IngridDocument newBasicObject) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialObject ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getInitialObject(plugId, newBasicObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getInitialAddress(IngridDocument newBasicAddress) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getInitialAddress ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getInitialAddress(plugId, newBasicAddress, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument createGroup(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE createGroup " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.createGroup(plugId, docIn, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument createUser(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE createUser " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.createUser(plugId, docIn, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}	

	public IngridDocument fetchTopObjects() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchTopObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchTopObjects(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String onlyFreeAddressesInfo = (onlyFreeAddresses) ? "ONLY FREE ADDRESSES" : "ONLY NO FREE ADDRESSES";
		System.out.println("\n###### INVOKE fetchTopAddresses " + onlyFreeAddressesInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchTopAddresses(plugId, myUserUuid, onlyFreeAddresses);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			if (!doFullOutput) {
				System.out.println("  " + l);				
			} else {
				for (Object o : l) {
					doFullOutput = false;
					debugAddressDoc((IngridDocument)o);
					doFullOutput = true;
				}				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchSubObjects(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubObjects ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchSubObjects(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				System.out.println(o);
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchSubAddresses(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchSubAddresses ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchSubAddresses(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities");
			for (Object o : l) {
				doFullOutput = false;
				debugAddressDoc((IngridDocument)o);
				doFullOutput = true;
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	/** Fetches WORKING VERSION of object ! */
	public IngridDocument fetchObject(String uuid, Quantity howMuch) {
		return fetchObject(uuid, howMuch, IdcEntityVersion.WORKING_VERSION);
		
	}

	public IngridDocument fetchObject(String uuid, Quantity howMuch, IdcEntityVersion whichVersion) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchObject (Details) ######");
		System.out.println("  fetch entity version:" + whichVersion);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.fetchObject(plugId, uuid, howMuch, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	/** Fetches WORKING VERSION of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, Quantity howMuch) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION, 0, 50);
	}

	/** Fetches requested version of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, Quantity howMuch, IdcEntityVersion whichVersion) {
		return fetchAddress(uuid, howMuch, whichVersion, 0, 50);
	}

	/** Fetches WORKING VERSION of address ! */
	public IngridDocument fetchAddress(String uuid, Quantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION,
				objRefsStartIndex, objRefsMaxNum);
	}

	public IngridDocument fetchAddress(String uuid, Quantity howMuch, IdcEntityVersion whichVersion,
			int objRefsStartIndex, int objRefsMaxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddress (Details) / fetch objRefs: start=" + objRefsStartIndex +
				", maxNum=" + objRefsMaxNum +" ######");
		System.out.println("  fetch entity version:" + whichVersion);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddress(plugId, uuid, howMuch, whichVersion,
				objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument fetchAddressObjectReferences(String uuid, int objRefsStartIndex, int objRefsMaxNum) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE fetchAddressObjectReferences / startIndex:" + objRefsStartIndex +
				", maxNum:" + objRefsMaxNum + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.fetchAddressObjectReferences(plugId, uuid, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			boolean formerFullOutput = doFullOutput;
			setFullOutput(true);
			debugAddressDoc(result);
			setFullOutput(formerFullOutput);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument checkObjectSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkObjectSubTree ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.checkObjectSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument checkAddressSubTree(String uuid) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE checkAddressSubTree ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.checkAddressSubTree(plugId, uuid, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument storeObject(IngridDocument oDocIn,
			boolean refetchObject) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchObjectInfo = (refetchObject) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeObject " + refetchObjectInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.storeObject(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument updateObjectPart(IngridDocument oPartDocIn, IdcEntityVersion whichVersion) {
		// check whether we have an object
		if (oPartDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateObjectPart (in object version: " + whichVersion + ") ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.updateObjectPart(plugId, oPartDocIn, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS");
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument assignObjectToQA(IngridDocument oDocIn,
			boolean refetchObject) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchObjectInfo = (refetchObject) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE assignObjectToQA " + refetchObjectInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.assignObjectToQA(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument reassignObjectToAuthor(IngridDocument oDocIn,
			boolean refetchObject) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchObjectInfo = (refetchObject) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE reassignObjectToAuthor " + refetchObjectInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.reassignObjectToAuthor(plugId, oDocIn, refetchObject, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugObjectDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
		return storeAddress(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchAddressInfo = (refetchAddress) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeAddress " + refetchAddressInfo + " / fetch objRefs: start=" + objRefsStartIndex +
			", maxNum=" + objRefsMaxNum +" ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.storeAddress(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument updateAddressPart(IngridDocument aPartDocIn, IdcEntityVersion whichVersion) {
		// check whether we have an object
		if (aPartDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE updateAddressPart (in address version: " + whichVersion + ") ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.updateAddressPart(plugId, aPartDocIn, whichVersion, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS");
		} else {
			handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress) {
		return assignAddressToQA(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchAddressInfo = (refetchAddress) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE assignAddressToQA " + refetchAddressInfo + " / fetch objRefs: start=" + objRefsStartIndex +
			", maxNum=" + objRefsMaxNum +" ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.assignAddressToQA(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress) {
		return reassignAddressToAuthor(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		// check whether we have an address
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchAddressInfo = (refetchAddress) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE reassignAddressToAuthor " + refetchAddressInfo + " / fetch objRefs: start=" + objRefsStartIndex +
			", maxNum=" + objRefsMaxNum +" ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.reassignAddressToAuthor(plugId, aDocIn, refetchAddress, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			debugAddressDoc(result);
			
		} else {
			handleError(response);
		}

		return result;
	}

	/** ALWAYS ADDS QA user-permission to group to avoid conflicts when workflow is enabled !!! */
	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch) {
		return storeGroup(docIn, refetch, true);
	}

	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch,
			boolean alwaysAddQA) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeGroup " + refetchInfo + " ######");

		System.out.println("  ADD QA: " + alwaysAddQA);
		if (alwaysAddQA) {
			addUserPermissionToGroupDoc(docIn, MdekUtilsSecurity.IdcPermission.QUALITY_ASSURANCE);
		}
		
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.storeGroup(plugId, docIn, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugGroupDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument storeUser(IngridDocument docIn,
			boolean refetch) {
		if (docIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String refetchInfo = (refetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE storeUser " + refetchInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.storeUser(plugId, docIn, refetch, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugUserDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}	

	public IngridDocument publishObject(IngridDocument oDocIn,
			boolean withRefetch,
			boolean forcePublicationCondition) {
		// check whether we have an object
		if (oDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE publishObject ######");
		System.out.println("publishObject -> " +
				"refetchObject: " + withRefetch +
				", forcePublicationCondition: " + forcePublicationCondition);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.publishObject(plugId, oDocIn, withRefetch, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuidStoredObject = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuidStoredObject);
			if (withRefetch) {
				debugObjectDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
		return publishAddress(aDocIn, refetchAddress, 0, 50);
	}

	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch, int objRefsStartIndex, int objRefsMaxNum) {
		if (aDocIn == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String withRefetchInfo = (withRefetch) ? "WITH REFETCH" : "WITHOUT REFETCH";
		System.out.println("\n###### INVOKE publishAddress  " + withRefetchInfo + " / fetch objRefs: start=" + objRefsStartIndex +
				", maxNum=" + objRefsMaxNum +" ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.publishAddress(plugId, aDocIn, withRefetch, objRefsStartIndex, objRefsMaxNum, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			System.out.println("SUCCESS: ");
			String uuid = (String) result.get(MdekKeys.UUID);
			System.out.println("uuid = " + uuid);
			if (withRefetch) {
				debugAddressDoc(result);
			}
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument moveObject(String fromUuid, String toUuid,
			boolean forcePublicationCondition) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String forcePubCondInfo = (forcePublicationCondition) ? "WITH FORCE publicationCondition" 
				: "WITHOUT FORCE publicationCondition";
		System.out.println("\n###### INVOKE moveObject " + forcePubCondInfo + "######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.moveObject(plugId, fromUuid, toUuid, forcePublicationCondition, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress)
	{
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String moveToFreeAddressInfo = (moveToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
		System.out.println("\n###### INVOKE moveAddress " + moveToFreeAddressInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.moveAddress(plugId, fromUuid, toUuid, moveToFreeAddress, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " moved !");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
		System.out.println("\n###### INVOKE copyObject " + copySubtreeInfo + " ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.copyObject(plugId, fromUuid, toUuid, copySubtree, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
			System.out.println("Root Copy: " + result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument copyAddress(String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress)
		{
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			String copySubtreeInfo = (copySubtree) ? "WITH SUBTREE" : "WITHOUT SUBTREE";
			String copyToFreeAddressInfo = (copyToFreeAddress) ? " / TARGET: FREE ADDRESS" : " / TARGET: NOT FREE ADDRESS";
			System.out.println("\n###### INVOKE copyAddress " + copySubtreeInfo + copyToFreeAddressInfo + " ######");
			startTime = System.currentTimeMillis();
			response = mdekCallerAddress.copyAddress(plugId, fromUuid, toUuid, copySubtree, copyToFreeAddress, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				System.out.println("SUCCESS: " + result.get(MdekKeys.RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES) + " copied !");
				System.out.println("Copy Node (rudimentary): ");
				debugAddressDoc(result);
			} else {
				handleError(response);
			}
			
			return result;
		}

	public IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObjectWorkingCopy " + deleteRefsInfo + " ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObjectWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));

		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteAddressWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddressWorkingCopy " + deleteRefsInfo + " ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddressWorkingCopy(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteObject " + deleteRefsInfo + " ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.deleteObject(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteAddress(String uuid,
			boolean forceDeleteReferences) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String deleteRefsInfo = (forceDeleteReferences) ? "WITH DELETE REFERENCES" : "WITHOUT DELETE REFERENCES";
		System.out.println("\n###### INVOKE deleteAddress " + deleteRefsInfo + " ######");
		System.out.println("- uuid: " + uuid);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.deleteAddress(plugId, uuid, forceDeleteReferences, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS");
			System.out.println("was fully deleted: " + result.get(MdekKeys.RESULTINFO_WAS_FULLY_DELETED));
			System.out.println("was marked deleted: " + result.get(MdekKeys.RESULTINFO_WAS_MARKED_DELETED));
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument deleteUser(Long idcUserId) {
		if (idcUserId == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE deleteUser ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.deleteUser(plugId, idcUserId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
		} else {
			handleError(response);
		}

		return result;
	}	

	public IngridDocument deleteGroup(Long idcGroupId,
			boolean forceDeleteGroupWhenUsers) {
		if (idcGroupId == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		String forceDeleteInfo = (forceDeleteGroupWhenUsers) ? "WITH " : "NO ";
		System.out.println("\n###### INVOKE deleteGroup " + forceDeleteInfo + " FORCE DELETE WHEN USERS ######");
		startTime = System.currentTimeMillis();
		response = mdekCallerSecurity.deleteGroup(plugId, idcGroupId, forceDeleteGroupWhenUsers, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			debugIdcUsersDoc(result);
		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument searchAddress(IngridDocument searchParams,
			int startHit, int numHits) {
		if (searchParams == null) {
			return null;
		}

		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE searchAddress ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.searchAddresses(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);

		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + l.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument a : l) {
				debugAddressDoc(a);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return result;
	}

	public List<IngridDocument> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchTerm:" + searchTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsFullText(plugId, searchTerm, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryObjectsExtended ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryObjectsExtended(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugObjectDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesFullText(String queryTerm,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesFullText ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- queryTerm:" + queryTerm);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesFullText(plugId, queryTerm, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAddressesThesaurusTerm ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- termSnsId:" + termSnsId);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesThesaurusTerm(plugId, termSnsId, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}

	public List<IngridDocument> queryAddressesExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryAdressesExtended ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- searchParams:" + searchParams);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryAddressesExtended(plugId, searchParams, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		List<IngridDocument> hits = null;
		if (result != null) {
			hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				debugAddressDoc(hit);
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}

		return hits;
	}	
	
	public void queryHQL(String qString,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryHQL ######");
		System.out.println("- startHit:" + startHit);
		System.out.println("- numHits:" + numHits);
		System.out.println("- query:" + qString);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryHQL(plugId, qString, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM_PAGING);
			IdcEntityType type = IdcEntityType.OBJECT;
			List<IngridDocument> hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
				type = IdcEntityType.ADDRESS;				
			}
			System.out.println("SUCCESS: " + hits.size() + " Entities out of " + totalNumHits);
			doFullOutput = false;
			for (IngridDocument hit : hits) {
				if (IdcEntityType.OBJECT.equals(type)) {
					debugObjectDoc(hit);
				} else {
					debugAddressDoc(hit);
				}
			}
			doFullOutput = true;
		} else {
			handleError(response);
		}
	}

	public void queryHQLToCsv(String qString) {
		try {
			long startTime;
			long endTime;
			long neededTime;
			IngridDocument response;
			IngridDocument result;

			System.out.println("\n###### INVOKE queryHQLToCsv ######");
			System.out.println("- query:" + qString);
			startTime = System.currentTimeMillis();
			response = mdekCallerQuery.queryHQLToCsv(plugId, qString, myUserUuid);
			endTime = System.currentTimeMillis();
			neededTime = endTime - startTime;
			System.out.println("EXECUTION TIME: " + neededTime + " ms");
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				Long totalNumHits = (Long) result.get(MdekKeys.TOTAL_NUM);
				System.out.println("SUCCESS: " + totalNumHits + " csvLines returned (and additional title-line)");
				String csvResult = result.getString(MdekKeys.CSV_RESULT);			
//				if (doFullOutput) {
//					System.out.println(csvResult);
//				} else {
					if (csvResult.length() > 5000) {
						int endIndex = csvResult.indexOf("\n", 3000);
						System.out.print(csvResult.substring(0, endIndex));					
						System.out.println("...");					
					} else {
						System.out.println(csvResult);					
					}
//				}

			} else {
				handleError(response);
			}			
		} catch (Throwable t) {
			System.out.println("\nCatched Throwable in Example queryHQLToCsv:");
			printThrowable(t);
		}
	}

	public IngridDocument queryHQLToMap(String qString, Integer maxNumHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE queryHQLToMap ######");
		System.out.println("- query:" + qString);
		System.out.println("- maxNumHits:" + maxNumHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerQuery.queryHQLToMap(plugId, qString, maxNumHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> hits = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			if (hits == null) {
				hits = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			}
			Long numHits = (Long) result.get(MdekKeys.TOTAL_NUM);
			if (numHits != hits.size()) {
				throw new MdekException(
					"Returned listsize of entities (" +	hits.size() + ") != returned numHits (" + numHits + "");
			}
			System.out.println("SUCCESS: " + numHits + " Entities");
			for (IngridDocument hit : hits) {
				System.out.println("  " + hit);
			}

		} else {
			handleError(response);
		}

		return result;
	}

	public IngridDocument getWorkObjects(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getWorkObjects ######");
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getWorkObjects(plugId,
				selectionType, orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
				System.out.println("  - total num QA:  assigned=" + result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED) + ", " +
				" reassigned=" + result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED));
			}
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				debugObjectDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getWorkAddresses(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getWorkAddresses ######");
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getWorkAddresses(plugId,
				selectionType, orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
				System.out.println("  - total num QA:  assigned=" + result.get(MdekKeys.TOTAL_NUM_QA_ASSIGNED) + ", " +
				" reassigned=" + result.get(MdekKeys.TOTAL_NUM_QA_REASSIGNED));
			}
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				debugAddressDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			handleError(response);
		}
		
		return result;
	}

	/**
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAObjects(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getQAObjects ######");
		System.out.println("- work state: " + whichWorkState);
		System.out.println("- selection type: " + selectionType);
		System.out.println("- order by: " + orderBy + ", ASC: " + orderAsc);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getQAObjects(plugId, 
				whichWorkState, selectionType, 
				orderBy, orderAsc, 
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List<IngridDocument> l = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			boolean tmpOutput = this.doFullOutput;
			setFullOutput(false);
			for (IngridDocument oDoc : l) {
				debugObjectDoc(oDoc);
			}
			setFullOutput(tmpOutput);
		} else {
			handleError(response);
		}
		
		return result;
	}

	/**
	 * @param whichWorkState only return addresses in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all addresses
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAAddresses(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getQAAddresses ######");
		System.out.println("- work state: " + whichWorkState);
		System.out.println("- selection type: " + selectionType);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getQAAddresses(plugId, whichWorkState, selectionType,
				startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			List l = (List) result.get(MdekKeys.ADR_ENTITIES);
			System.out.println("SUCCESS: " + l.size() + " Entities of total num: " + result.get(MdekKeys.TOTAL_NUM_PAGING));
			for (Object o : l) {
				System.out.println(o);				
			}
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getObjectStatistics(String uuidIn,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getObjectStatistics: " + whichType + " ######");
		System.out.println("- top node of branch:" + uuidIn);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerObject.getObjectStatistics(plugId, uuidIn, whichType, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public IngridDocument getAddressStatistics(String uuidIn, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getAddressStatistics: " + whichType + " ######");
		System.out.println("- top node of branch:" + uuidIn);
		System.out.println("- only free addresses:" + onlyFreeAddresses);
		System.out.println("- paging from:" + startHit);
		System.out.println("- paging num:" + numHits);
		startTime = System.currentTimeMillis();
		response = mdekCallerAddress.getAddressStatistics(plugId, uuidIn, onlyFreeAddresses,
				whichType, startHit, numHits, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public void trackRunningJob(int sleepTimeMillis, boolean doCancel) {
		IngridDocument response;
		IngridDocument result;
		System.out.println("\n###### INVOKE getRunningJobInfo ######");

		boolean jobIsRunning = true;
		int counter = 0;
		while (jobIsRunning) {
			if (doCancel && counter > 4) {
				cancelRunningJob();
				return;
			}

			response = mdekCaller.getRunningJobInfo(plugId, myUserUuid);
			result = mdekCaller.getResultFromResponse(response);
			if (result != null) {
				String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
				Integer numObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
				Integer total = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);
				if (jobDescr == null) {
					// job finished !
					jobIsRunning = false;					
					System.out.println("JOB FINISHED\n");
				} else {
					System.out.println("job:" + jobDescr + ", entities:" + numObjs + ", total:" + total);
				}
			} else {
				handleError(response);
				jobIsRunning = false;
			}
			
			try {
				Thread.sleep(sleepTimeMillis);				
			} catch(Exception ex) {
				System.out.println(ex);
			}
			counter++;
		}
	}

	public void cancelRunningJob() {
		System.out.println("\n###### INVOKE cancelRunningJob ######");

		IngridDocument response = mdekCaller.cancelRunningJob(plugId, myUserUuid);
		IngridDocument result = mdekCaller.getResultFromResponse(response);
		if (result != null) {
			String jobDescr = result.getString(MdekKeys.RUNNINGJOB_DESCRIPTION);
			if (jobDescr == null) {
				System.out.println("JOB FINISHED\n");
			} else {
				System.out.println("JOB CANCELED: " + result);
			}
		} else {
			handleError(response);
		}
	}

	public String extractUserData(IngridDocument inDoc) {
		if (inDoc == null) {
			return null; 
		}

		String user = inDoc.getString(MdekKeys.UUID);
		if (inDoc.get(MdekKeys.NAME) != null) {
			user += " " + inDoc.get(MdekKeys.NAME);
		}
		if (inDoc.get(MdekKeys.GIVEN_NAME) != null) {
			user += " " + inDoc.get(MdekKeys.GIVEN_NAME);
		}
		if (inDoc.get(MdekKeys.ORGANISATION) != null) {
			user += " " + inDoc.get(MdekKeys.ORGANISATION);
		}
		
		return user;
	}

	private void debugCatalogDoc(IngridDocument c) {
		System.out.println("Catalog: " + c.get(MdekKeysSecurity.CATALOG_NAME) 
			+ ", partner: " + c.get(MdekKeys.PARTNER_NAME)
			+ ", provider: " + c.get(MdekKeys.PROVIDER_NAME)
			+ ", country: " + c.get(MdekKeys.COUNTRY)
			+ ", language: " + c.get(MdekKeys.LANGUAGE)
		);
		System.out.println("         "
			+ ", workflow: " + c.get(MdekKeys.WORKFLOW_CONTROL)
			+ ", expiry: " + c.get(MdekKeys.EXPIRY_DURATION)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)c.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)c.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + extractUserData((IngridDocument)c.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + c);

		System.out.println("  Location: " + c.get(MdekKeys.CATALOG_LOCATION));
	}
	
	private void debugUserDoc(IngridDocument u) {
		System.out.println("User: " + u.get(MdekKeysSecurity.IDC_USER_ID) 
			+ ", " + u.get(MdekKeysSecurity.IDC_USER_ADDR_UUID)
			+ ", name: " + u.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + u.get(MdekKeys.GIVEN_NAME)
			+ " " + u.get(MdekKeys.NAME)
			+ ", organisation: " + u.get(MdekKeys.ORGANISATION)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + extractUserData((IngridDocument)u.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + u);

		debugPermissionsDoc(u, "  ");
	}
	
	private void debugGroupDoc(IngridDocument g) {
		System.out.println("Group: " + g.get(MdekKeysSecurity.IDC_GROUP_ID) 
			+ ", " + g.get(MdekKeys.NAME)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + extractUserData((IngridDocument)g.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + g);

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  User Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}

	private void debugPermissionsDoc(IngridDocument p, String indent) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println(indent + "Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println(indent + "  " + doc);								
			}			
		} else {
			System.out.println(indent + "No Permissions");			
		}
	}
	
	public void debugPermissionsDocBoolean(IngridDocument p) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		System.out.println("HAS_WRITE_ACCESS: " + MdekUtilsSecurity.hasWritePermission(docList));
		System.out.println("HAS_WRITE_TREE_ACCESS: " + MdekUtilsSecurity.hasWriteTreePermission(docList));
		System.out.println("HAS_WRITE_SINGLE_ACCESS: " + MdekUtilsSecurity.hasWriteSinglePermission(docList));
	}
	
	private void debugIdcUsersDoc(IngridDocument u) {
		List<IngridDocument> docList = (List<IngridDocument>) u.get(MdekKeysSecurity.IDC_USERS);
		if (docList != null && docList.size() > 0) {
			System.out.println("Users: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}
	
	private void debugObjectDoc(IngridDocument o) {
		System.out.println("Object: " + o.get(MdekKeys.ID) 
			+ ", " + o.get(MdekKeys.UUID)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(ObjectType.class, o.get(MdekKeys.CLASS))
			+ ", " + o.get(MdekKeys.TITLE)
			+ ", marked deleted: " + o.get(MdekKeys.MARK_DELETED)
		);
		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
			+ ", modUser: " + extractUserData((IngridDocument)o.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)o.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)o.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.get(MdekKeys.PUBLICATION_CONDITION))
//			+ ", cat_id: " + o.get(MdekKeys.CATALOGUE_IDENTIFIER)
		);

		System.out.println("  " + o);

		if (!doFullOutput) {
			return;
		}

		debugPermissionsDoc(o, "  ");

		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			for (IngridDocument a : docList) {
				System.out.println("   " + a.get(MdekKeys.UUID) + ": " + a);								
				List<IngridDocument> coms = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
				if (coms != null) {
					System.out.println("    Communication: " + coms.size() + " Entities");
					for (IngridDocument c : coms) {
						System.out.println("     " + c);
					}					
				}
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LOCATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LINKAGES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) o.get(MdekKeys.EXPORTS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.LEGISLATIONS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATA_FORMATS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) o.get(MdekKeys.ENV_CATEGORIES);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Categories: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.ENV_TOPICS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) o.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null && intList.size() > 0) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (myDoc != null) {
			System.out.println("  technical domain MAP:");
			System.out.println("    " + myDoc);								
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.KEY_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - key catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - publication scales (Erstellungsma�stab): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SYMBOL_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - symbol catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			strList = (List<String>) myDoc.get(MdekKeys.FEATURE_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - feature types: " + strList.size() + " entries");
				for (String str : strList) {
					System.out.println("     " + str);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.GEO_VECTOR_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - vector formats, geo vector list: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			intList = (List<Integer>) myDoc.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - spatial rep types: " + intList.size() + " entries");
				for (Integer i : intList) {
					System.out.println("     " + i);								
				}			
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (myDoc != null) {
			System.out.println("  technical domain DOCUMENT:");
			System.out.println("    " + myDoc);								
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (myDoc != null) {
			System.out.println("  technical domain SERVICE:");
			System.out.println("    " + myDoc);								
			strList = (List<String>) myDoc.get(MdekKeys.SERVICE_VERSION_LIST);
			if (strList != null && strList.size() > 0) {
				System.out.println("    SERVICE - versions: " + strList.size() + " entries");
				System.out.println("     " + strList);
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_TYPE2_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - types (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - publication scales = Erstellungsma�stab (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_OPERATION_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - operations: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
					strList = (List<String>) doc.get(MdekKeys.PLATFORM_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - platforms: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.DEPENDS_ON_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - dependsOns: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.CONNECT_POINT_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - connectPoints: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					List<IngridDocument> docList2 = (List<IngridDocument>) doc.get(MdekKeys.PARAMETER_LIST);
					if (docList2 != null) {
						System.out.println("      SERVICE - operation - parameters: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
					}
				}
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (myDoc != null) {
			System.out.println("  technical domain PROJECT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (myDoc != null) {
			System.out.println("  technical domain DATASET:");
			System.out.println("    " + myDoc);								
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
				System.out.println("    created by user: " + doc.get(MdekKeys.CREATE_USER));
			}
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Additional Fields: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.CONFORMITY_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object conformity (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ACCESS_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object access (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}

		myDoc = (IngridDocument) o.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
	}

	private void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", marked deleted: " + a.get(MdekKeys.MARK_DELETED)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.TITLE_OR_FUNCTION_KEY)			
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
		);
		System.out.println("         "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", modUser: " + extractUserData((IngridDocument)a.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)a.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)a.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + a);

		debugPermissionsDoc(a, "  ");

		IngridDocument myDoc;
		List<IngridDocument> docList;
		List<String> strList;

		docList = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Communication: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}

		// objects referencing the address !
		Integer objsFromStartIndex = (Integer) a.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
		if (objsFromStartIndex != null) {
			Integer objsFromTotalNum = (Integer) a.get(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM);
			System.out.println("  Objects FROM (Querverweise): PAGING RESULT ! startIndex=" + objsFromStartIndex +
					", totalNum=" + objsFromTotalNum);
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
				System.out.println("    created by user: " + doc.get(MdekKeys.CREATE_USER));
			}
		}
		myDoc = (IngridDocument) a.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
		strList = (List<String>) a.get(MdekKeys.PATH);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Path: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.PATH_ORGANISATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Path Organisations: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}			
		}
	}

	private void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekCaller.getErrorMsgFromResponse(response));			

		if (!doFullOutput) {
			return;
		}

		// detailed output  
		List<MdekError> errors = mdekCaller.getErrorsFromResponse(response);
		doFullOutput = false;
		for (MdekError err : errors) {
			IngridDocument info = err.getErrorInfo();

			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				// referenced entity (object or address)
				if (info.get(MdekKeys.TITLE) != null) {
					System.out.println("    referenced Object:");
					debugObjectDoc(info);
				} else {
					System.out.println("    referenced Address:");
					debugAddressDoc(info);
				}
				// objects referencing
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					System.out.println("    Referencing objects: " + oDocs.size() + " objects!");
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}

			} else if (err.getErrorType().equals(MdekErrorType.ADDRESS_IS_AUSKUNFT)) {
				// objects referencing address as auskunft
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					System.out.println("    Referencing objects: " + oDocs.size() + " objects!");
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}

			} else if (err.getErrorType().equals(MdekErrorType.GROUP_HAS_USERS)) {
				debugIdcUsersDoc(info);
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT)) {
				System.out.println("    Object with multiple Permissions: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS)) {
				System.out.println("    Address with multiple Permissions: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with TREE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with TREE Permission: " + addrs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with SINGLE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with SINGLE Permission: " + addrs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_REMOVE_OBJECT_PERMISSION)) {
				System.out.println("    No right to remove object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_REMOVE_ADDRESS_PERMISSION)) {
				System.out.println("    No right to remove address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_ADD_OBJECT_PERMISSION)) {
				System.out.println("    No right to add object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_ADD_ADDRESS_PERMISSION)) {
				System.out.println("    No right to add address: " + info.get(MdekKeys.ADR_ENTITIES));
			}
		}
		doFullOutput = true;
	}

	private void printThrowable(Throwable t) {
		System.out.println(t);
		System.out.println("   Stack Trace:");
		StackTraceElement[] st = t.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	        System.out.println(stackTraceElement);
        }
		Throwable cause = t.getCause();
		if (cause != null) {
			System.out.println("   Cause:");
			printThrowable(cause);			
		}
	}

	public void addUserPermissionToGroupDoc(IngridDocument groupDoc, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, perms);
		}
		// check whether permission already present !
		boolean addPerm = true;
		for (IngridDocument perm : perms) {
			if (idcPerm.getDbValue().equals(perm.getString(MdekKeysSecurity.IDC_PERMISSION))) {
				addPerm = false;
				break;
			}
		}
		
		if (addPerm) {
			IngridDocument newPerm = new IngridDocument();
			newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
			perms.add(newPerm);			
		}
	}

	public void addObjPermissionToGroupDoc(IngridDocument groupDoc, String objUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, objUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	public void addAddrPermissionToGroupDoc(IngridDocument groupDoc, String addrUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, addrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	public void addComment(IngridDocument entityDoc, String comment) {
		List<IngridDocument> docList = (List<IngridDocument>) entityDoc.get(MdekKeys.COMMENT_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument tmpDoc = new IngridDocument();
		tmpDoc.put(MdekKeys.COMMENT, comment);
		tmpDoc.put(MdekKeys.CREATE_TIME, MdekUtils.dateToTimestamp(new Date()));
		IngridDocument createUserDoc = new IngridDocument();
		createUserDoc.put(MdekKeys.UUID, getCallingUserUuid());
		tmpDoc.put(MdekKeys.CREATE_USER, createUserDoc);
		docList.add(tmpDoc);
		entityDoc.put(MdekKeys.COMMENT_LIST, docList);
	}
	
	public void setResponsibleUser(IngridDocument entityDoc, String userUuid) {
		IngridDocument userDoc = (IngridDocument) entityDoc.get(MdekKeys.RESPONSIBLE_USER);
		if (userDoc == null) {
			userDoc = new IngridDocument();
			entityDoc.put(MdekKeys.RESPONSIBLE_USER, userDoc);
		}
		userDoc.put(MdekKeys.UUID, userUuid);
	}
	
	/** Creates default new Object Document including required default data */
	public IngridDocument newObjectDoc(String parentObjUuid) {
		IngridDocument newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, parentObjUuid);
		newDoc = getInitialObject(newDoc);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		
		return newDoc;
	}

	/** Creates default new Address Document including required default data */
	public IngridDocument newAddressDoc(String parentAddrUuid, AddressType whichType) {
		IngridDocument newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, parentAddrUuid);
		newDoc = getInitialAddress(newDoc);

		newDoc.put(MdekKeys.NAME, "testNAME");
		newDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newDoc.put(MdekKeys.ORGANISATION, "testORGANISATION");
		newDoc.put(MdekKeys.CLASS, whichType.getDbValue());
		// email has to exist !
		List<IngridDocument> docList = (List<IngridDocument>) newDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newDoc.put(MdekKeys.COMMUNICATION, docList);
		
		return newDoc;
	}
}
