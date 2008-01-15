package de.ingrid.mdek;

import de.ingrid.utils.IngridDocument;

/**
 * Defines the interface to be implemented to communicate with the Mdek backend.
 *
 * @author Martin
 */
public interface IMdekCaller {

	/** How much data to fetch from requested entity ? */
	public enum Quantity {
		DETAIL_ENTITY // client: edit dialogue -> request maximum data
	}

//	IngridDocument testMdekEntity(int threadNumber);

	/** Returns a map containing the lists for the UI select boxes */
	IngridDocument getUiListValues();

	/**
	 * Fetch single object with given uuid.
	 * @param uuid object uuid
	 * @param howMuch how much data to fetch from object
	 * @return response containing result: map representation of object containing requested data
	 */
	IngridDocument fetchObject(String uuid, Quantity howMuch);

	/**
	 * Create or Store object.
	 * @param obj map representation of object. 
	 * 		If no id/uuid is set object will be created else updated.
	 * @param refetchAfterStore true=fetch and return Object after store, false=no fetch, just store
	 * @return response containing result: map representation of created/updated object when refetching,
	 * 		otherwise map containing uuid of stored object (was generated when new object)  
	 */
	IngridDocument storeObject(IngridDocument obj, boolean refetchAfterStore);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the object is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 * @param uuid object uuid
	 * @return response containing result: map containing info whether object was fully deleted
	 */
	IngridDocument deleteObjectWorkingCopy(String uuid);

	/**
	 * FULL DELETE: working copy and published version are removed INCLUDING subobjects !
	 * Object non existent afterwards !
	 * @param uuid object uuid
	 * @return response containing result: map containing info about success
	 */
	IngridDocument deleteObject(String uuid);

	/**
	 * Fetch all top objects of tree.
	 * @return response containing result: map containing representations of all root objects
	 */
	IngridDocument fetchTopObjects();

	/**
	 * Fetch all sub objects of of object with given uuid
	 * @param uuid object uuid
	 * @return response containing result: map containing representations of all sub objects
	 */
	IngridDocument fetchSubObjects(String uuid);

	/**
	 * Get Path of object in tree starting at root
	 * @param uuid object uuid
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getObjectPath(String uuid);

	/**
	 * Check whether operations with the subtree of the given object (uuid)
	 * are permitted or prohibited (e.g. no rights, subtree in process ...)
	 * @param uuid object uuid of top node
	 * @return response containing result: map containing info about examination
	 */
	IngridDocument checkObjectSubTree(String uuid);

	/**
	 * Copy an object with its subtree to another object.
	 * All objects will be copied and will have new id/uuid.  
	 * @param fromUuid uuid of top object of tree to copy (this one will also be copied)
	 * @param toUuid uuid of object where to add copied tree
	 * @param performCheck true=perform check whether sourcetree can be operated on, 
	 * 		false=do not perform check
	 * @return response containing result: map containing basic data of top object of new tree
	 */
	IngridDocument copyObjectSubTree(String fromUuid, String toUuid, boolean performCheck);

	/**
	 * Cut an object with its subtree and move it to another object.
	 * @param fromUuid uuid of top object of tree to cut (this one will be removed from its parent)
	 * @param toUuid uuid of object where to move tree to
	 * @param performCheck true=perform check whether sourcetree can be operated on, 
	 * 		false=do not perform check
	 * @return response containing result: map containing info about success
	 */
	IngridDocument cutObjectSubTree(String fromUuid, String toUuid, boolean performCheck);


	// uncomment when needed ! then recheck functionality and implementation !
//	IngridDocument fetchTopAddresses();
//	IngridDocument fetchSubAddresses(String adrUuid);

	/**
	 * Get pure requested result data from response of mdek call (without "protocol" overhead in response).
	 * @param mdekResponse response from mdek call
	 * @return null if errors occured otherwise IngridDocument containing results
	 */
	IngridDocument getResultFromResponse(IngridDocument mdekResponse);

	/**
	 * Get Error Message from response of mdek call.
	 * @param mdekResponse response from mdek call
	 * @return null if no error message set in response
	 */
	String getErrorMsgFromResponse(IngridDocument mdekResponse);
}
