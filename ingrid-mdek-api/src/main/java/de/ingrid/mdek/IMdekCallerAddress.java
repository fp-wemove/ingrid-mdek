package de.ingrid.mdek;

import de.ingrid.mdek.IMdekCallerCommon.Quantity;
import de.ingrid.utils.IngridDocument;


/**
 * Defines the interface to be implemented to communicate with the Mdek backend
 * concerning ADDRESS Manipulation.
 */
public interface IMdekCallerAddress {

	/**
	 * Fetch top addresses.
	 * @param userId
	 * @param onlyFreeAddresses true= only free top addresses, false=only NOT free top addresses
	 * @return response containing result: map containing representations of root addresses
	 */
	IngridDocument fetchTopAddresses(String userId, boolean onlyFreeAddresses);

	/**
	 * Fetch all sub addresses of address with given uuid
	 * @param uuid address uuid
	 * @return response containing result: map containing representations of sub addresses
	 */
	IngridDocument fetchSubAddresses(String uuid,
			String userId);

	/**
	 * Get Path of address in tree starting at root
	 * @param uuid address uuid = end node in path (included in path !)
	 * @return response containing result: map containing path (List of uuids starting at root)
	 */
	IngridDocument getAddressPath(String uuid,
			String userId);

	/**
	 * Fetch single address with given uuid.
	 * @param uuid address uuid
	 * @param howMuch how much data to fetch from address
	 * @return response containing result: map representation of address containing requested data
	 */
	IngridDocument fetchAddress(String uuid, Quantity howMuch,
			String userId);

	/**
	 * Create or store address INTO WORKING COPY !
	 * @param adr map representation of address.
	 * 		If no id/uuid is set address will be created else updated.
	 * @param refetchAfterStore immediately refetch address after store (true)
	 * 		or just store without refetching (false)
	 * @return response containing result: map representation of created/updated address when refetching,
	 * 		otherwise map containing uuid of stored address (was generated when new address)  
	 */
	IngridDocument storeAddress(IngridDocument adr,
			boolean refetchAfterStore,
			String userId);

	/**
	 * Get initial data for a new address. Pass data needed to determine initial data (e.g. uuid of parent).
	 * @param newBasicAddress basic new address with data needed to determine initial data, e.g. parent uuid ...
	 * @return extended newAddress, e.g. containing terms of parent etc.
	 */
	IngridDocument getInitialAddress(IngridDocument newBasicAddress,
			String userId);

	/**
	 * DELETE ONLY WORKING COPY.
	 * Notice: If no published version exists the address is deleted completely, meaning non existent afterwards
	 * (including all subobjects !)
	 * @param uuid object uuid
	 * @return response containing result: map containing info whether address was fully deleted
	 */
	IngridDocument deleteAddressWorkingCopy(String uuid,
			String userId);

	/**
	 * Copy an address to another parent.
	 * @param fromUuid uuid of node to copy
	 * @param toUuid uuid of parent where to copy to (new subnode)
	 * @param copySubtree true=also copy subtree, false=only address without subAddresses
	 * @param userId current user to track jobs of user
	 * @return response containing result: map containing basic data of copied address
	 * and additional info (number of copied addresses ...)
	 */
	IngridDocument copyAddress(String fromUuid, String toUuid, boolean copySubtree,
			String userId);

}
