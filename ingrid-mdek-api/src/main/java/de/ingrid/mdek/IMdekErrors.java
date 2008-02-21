package de.ingrid.mdek;


/**
 * Interface defining MdekError Types
 * 
 * @author Martin
 */
public interface IMdekErrors {

	/** Error Codes ! Encapsulated "dbValue" represents error code. */
	public enum MdekError implements IMdekEnum {
		/** Another user changed an entity in between */
		ENTITY_CHANGED_IN_BETWEEN("1"),

		/** No user set in request ! */
		USER_ID_NOT_SET("5"),
		/** There are still running jobs of user */
		USER_HAS_RUNNING_JOBS("6"),
		/** User canceled job */
		USER_CANCELED_JOB("7"),

		UUID_NOT_FOUND("10"),
		FROM_UUID_NOT_FOUND("11"),
		TO_UUID_NOT_FOUND("12"),
		/** No catalog data found (e.g. entity catalog association is missing) */
		CATALOG_NOT_FOUND("13"),

		/** e.g. publish of child not allowed when parent not published */
		PARENT_NOT_PUBLISHED("20"),
		/** e.g. move with unpublished node not allowed  */
		ENTITY_NOT_PUBLISHED("21"),

		/** e.g. move of tree node to subnode not allowed */
		TARGET_IS_SUBNODE_OF_SOURCE("30"),
		/** e.g. then no move allowed ! Also thrown if top node has WorkingCopy ! */
		SUBTREE_HAS_WORKING_COPIES("31"),
		/** e.g. when object is published and sub publication conditions don't fit */
		SUBTREE_HAS_LARGER_PUBLICATION_CONDITION("32"),
		/** e.g. when object is published and publication condition doesn't fit to parent */
		PARENT_HAS_SMALLER_PUBLICATION_CONDITION("33"),

		/** when free address is NOT a root node */
		FREE_ADDRESS_WITH_PARENT("41"),
		/** when free address has subnodes (e.g. copy of node with subnodes to free address) */
		FREE_ADDRESS_WITH_SUBTREE("42");

		MdekError(String errorCode) {
			this.errorCode = errorCode;
		}
		/** represents the error code of this enumeration constant.
		 * @see de.ingrid.mdek.IMdekEnum#getDbValue()
		 */
		public String getDbValue() {
			return errorCode;
		}
		String errorCode;
	}
}
