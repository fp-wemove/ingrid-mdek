package de.ingrid.mdek;


/**
 * Class encapsulating error types/ methods.
 * 
 * @author Martin
 */
public class MdekErrors {

	/** Error Codes ! Encapsulated "dbValue" represents error code. */
	public enum MdekError implements IMdekEnum {
		// e.g. another user changed an object in between
		ENTITY_CHANGED_IN_BETWEEN("1"),
		UUID_NOT_FOUND("10"),
		FROM_UUID_NOT_FOUND("11"),
		TO_UUID_NOT_FOUND("12"),
		// e.g. publish of child not allowed when parent not published
		PARENT_NOT_PUBLISHED("20"),
		// e.g. move of tree node to subnode not allowed
		TO_UUID_SUBNODE_OF_FROM_UUID("30");

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
