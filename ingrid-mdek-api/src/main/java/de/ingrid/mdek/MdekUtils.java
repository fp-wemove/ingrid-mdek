package de.ingrid.mdek;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Class encapsulating utility methods.
 * 
 * @author Martin
 */
public class MdekUtils {

	private static final Logger LOG = Logger.getLogger(MdekUtils.class);

	private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private final static SimpleDateFormat displayDateFormatter = new SimpleDateFormat("dd.MM.yyyy");
	private final static SimpleDateFormat displayDateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy/HH:mm");

	public final static Integer OBJ_SERV_TYPE_CSW = 1;
	public final static Integer OBJ_SERV_TYPE_WMS = 2;
	public final static Integer OBJ_SERV_TYPE_WFS = 3;
	public final static Integer OBJ_SERV_TYPE_WCTS = 4;

	public final static Integer OBJ_ADR_TYPE_AUSKUNFT_ID = 7;

	public final static Integer COMM_TYPE_EMAIL = 3;

	public final static String YES = "Y";
	public final static String NO = "N";

	/** Type of user operation */
	public enum UserOperation {
		NEW,
		EDITED,
		DELETED;
	}

	public enum MdekSysList implements IMdekEnum {
		FREE_ENTRY(-1),
		OBJ_ADR_TYPE(505),
		OBJ_ADR_TYPE_SPECIAL(2010),
		OBJ_REFERENCE(2000),
		OBJ_GEO_REFERENCESYSTEM(100),
		OBJ_GEO_KEYC(3535),
		OBJ_GEO_SYMC(3555),
		OBJ_LITERATURE_TYPE(3385),
		OBJ_SERV_TYPE(5100),
		OBJ_SERV_OPERATION_CSW(5105),
		OBJ_SERV_OPERATION_WMS(5110),
		OBJ_SERV_OPERATION_WFS(5120),
		OBJ_SERV_OPERATION_WCTS(5130),
		OBJ_SERV_TYPE2(5200),
		INFO_IMPART(1370),
		LEGIST(1350),
		URL_REF_SPECIAL(2000),
		URL_REF_DATATYPE(2240),
		MEDIA_OPTION_MEDIUM(520),
		SPATIAL_REF_VALUE(1100),
		AVAIL_FORMAT(1320),
		ADDRESS_VALUE(4300),
		ADDRESS_TITLE(4305),
		COMM_TYPE(4430),
		OBJ_CONFORMITY(6000),
		OBJ_ACCESS(6010);

		MdekSysList(Integer dbValue) {
			this.dbValue = dbValue;
		}
		/** returns syslist ID */
		public Integer getDbValue() {
			return dbValue;
		}
		Integer dbValue;
	}

	public enum SysGuiBehaviour implements IMdekEnum {
		DEFAULT(-1),
		REMOVED(0),
		MANDATORY(1);

		SysGuiBehaviour(Integer dbValue) {
			this.dbValue = dbValue;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		Integer dbValue;
	}

	/** Type of entities */
	public enum IdcEntityType {
		OBJECT,
		ADDRESS;
	}

	/** Different versions of IDC entities */
	public enum IdcEntityVersion {
		WORKING_VERSION,
		PUBLISHED_VERSION,
		ALL_VERSIONS;
	}

	/** e.g. Which entities to fetch ? */
	public enum IdcEntitySelectionType {
		/** QA: all expired entities (determined by job) */
		QA_EXPIRY_STATE_EXPIRED,
		/** QA: all entities where spatial relations were updated e.g. due to catalog management */
		QA_SPATIAL_RELATIONS_UPDATED,
		/** Statistics: analysis how many entities per class and work state */
		STATISTICS_CLASSES_AND_STATES,
		;
	}

	public enum ExpiryState implements IMdekEnum {
		INITIAL(0),
		TO_BE_EXPIRED(10),
		EXPIRED(20);

		ExpiryState(Integer dbValue) {
			this.dbValue = dbValue;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		/**
		 * Is this expiry state "more urgent" than the passed state ? e.g.<br>
		 * EXPIRED.isHigher(TO_BE_EXPIRED) -> true<br>
		 * EXPIRED.isHigher(INITIAL) -> true<br>
		 * EXPIRED.isHigher(EXPIRED) -> false<br>
		 */
		public boolean isHigher(ExpiryState inState) {
			if (this.getDbValue() > inState.getDbValue()) {
				return true;
			}
			return false;
		}
		Integer dbValue;
	}

	/** WorkState of entities */
	public enum WorkState implements IMdekEnum {
		VEROEFFENTLICHT("V", "ver\u00f6ffentlicht"),
		IN_BEARBEITUNG("B", "in Bearbeitung"),
		QS_UEBERWIESEN("Q", "an Qualit\u00e4tssicherung zugewiesen"),
		QS_RUECKUEBERWIESEN("R", "von Qualit\u00e4tssicherung r\u00fcck\u00fcberwiesen");

		WorkState(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public String getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		String dbValue;
		String description;
	}

	/** Type of spatial reference */
	public enum SpatialReferenceType implements IMdekEnum {
		FREI("F", "Freier Raumbezug"),
		GEO_THESAURUS("G", "Geo-Thesaurus");

		SpatialReferenceType(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public String getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		String dbValue;
		String description;
	}

	/** Type of searchterm */
	public enum SearchtermType implements IMdekEnum {
		FREI("F", "Freier Term"),
		THESAURUS("T", "Thesaurus");

		SearchtermType(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public String getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		String dbValue;
		String description;
	}

	/** Publish condition */
	public enum PublishType implements IMdekEnum {
		INTERNET(1, "Internet"),
		INTRANET(2, "Intranet"),
		AMTSINTERN(3, "amtsintern");

		PublishType(Integer dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		/**
		 * Is this type "broader" or "equal" the passed type ? e.g.<br>
		 * INTERNET.includes(INTERNET) -> true<br>
		 * INTERNET.includes(INTRANET) -> true<br>
		 * INTRANET.includes(INTERNET) -> false<br>
		 */
		public boolean includes(PublishType inType) {
			if (this.getDbValue() <= inType.getDbValue()) {
				return true;
			}
			return false;
		}
		Integer dbValue;
		String description;
	}

	/** Type of object (object class) */
	public enum ObjectType implements IMdekEnum {
		ORGANISATION(0, "Organisationseinheit/Fachaufgabe"),
		GEO_INFORMATION(1, "Geo-Information/Karte"),
		DOKUMENT(2, "Dokument/Bericht/Literatur"),
		DIENST(3, "Dienst/Anwendung/Informationssystem"),
		VORHABEN(4, "Vorhaben/Projekt/Programm"),
		DATENSAMMLUNG(5, "Datensammlung/Datenbank");

		ObjectType(Integer dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		Integer dbValue;
		String description;
	}

	/** Type of addresses (address class) */
	public enum AddressType implements IMdekEnum {
		INSTITUTION(0, "Institution"),
		EINHEIT(1, "Einheit"),
		PERSON(2, "Person"),
		FREI(3, "Freie Adresse");

		AddressType(Integer dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		Integer dbValue;
		String description;
	}

/*
	private static MdekUtils myInstance;

	public static synchronized MdekUtils getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekUtils();
	      }
		return myInstance;
	}

	private MdekUtils() {}
*/

	/** Format database timestamp to display date. */
	public static String timestampToDisplayDate(String yyyyMMddHHmmssSSS) {
		try {
			Date in = timestampFormatter.parse(yyyyMMddHHmmssSSS);
			String out = displayDateFormatter.format(in);
			return out;
		} catch (Exception ex){
			if (yyyyMMddHHmmssSSS != null && yyyyMMddHHmmssSSS.length() > 0) {
				LOG.warn("Problems parsing timestamp from database: " + yyyyMMddHHmmssSSS, ex);				
			}
			return "";
		}
	}
	/** Format date to database timestamp. */
	public static String dateToTimestamp(Date date) {
		try {
			String out = timestampFormatter.format(date);
			return out;
		} catch (Exception ex){
			LOG.warn("Problems formating date to timestamp: " + date, ex);
			return "";
		}
	}
	/** Format milliseconds since January 1, 1970, 00:00:00 GMT to display date/time. */
	public static String millisecToDisplayDateTime(String millisec) {
		try {
			Date in = new Date(Long.valueOf(millisec));
			String out = displayDateTimeFormatter.format(in);
			return out;
		} catch (Exception ex){
			if (millisec != null && millisec.length() > 0) {
				LOG.warn("Problems parsing millisec: " + millisec, ex);
			}
			return "";
		}
	}

	/**
	 * Processes given string. Returns null if String is empty or only whitespaces etc.
	 * @param param the string to process
	 * @return the processed string
	 */
	public static String processStringParameter(String param) {
		if (param != null) {
			param = param.trim();

			// replace special characters inside string
			param = param.replaceAll ("\\n", " ");
			param = param.replaceAll ("\\t", " ");

			if (param.length() == 0) {
				param = null;
			}
		}
		
		return param;
	}
}
