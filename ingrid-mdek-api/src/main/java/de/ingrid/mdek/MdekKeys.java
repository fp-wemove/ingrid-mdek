package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument.
 * 
 * @author Martin
 */
public class MdekKeys {

    /** Value: List of IngridDocs */
    public final static String OBJ_ENTITIES = "objEntities";
    /** Value: List of IngridDocs */
    public final static String ADR_ENTITIES = "adrEntities";
    /** Value: List of IngridDocs */
    public final static String OBJ_REFERENCES_FROM = "objReferencesFrom";
    /** Value: List of IngridDocs */
    public final static String OBJ_REFERENCES_TO = "objReferencesTo";
    /** Value: List of IngridDocs */
    public final static String ADR_REFERENCES_TO = "adrReferencesTo";

    // COMMON DATA
    // -----------

    /** Value: Long */
    public final static String ID = "id";
    /** the uuid of the entity (object or address)<br>
     *	Value: String */
    public final static String UUID = "uuid";
    /** the uuid of the parent (object or address)<br>
     *	Value: String */
    public final static String PARENT_UUID = "parent-uuid";
    /** the class of the entity (object or address)<br>
     *	Value: Integer */
    public final static String CLASS = "class";
    /** Value: Boolean */
    public final static String HAS_CHILD = "hasChild";
    /** database column "type"<br>
     *  Value: Integer */
    public final static String RELATION_TYPE_ID = "relation-type-id";
    /** database column "special_name"<br> 
     * Value: String */
    public final static String RELATION_TYPE_NAME = "relation-type-name";
    /** database column "special_ref"<br>
     *  Value: Integer */
    public final static String RELATION_TYPE_REF = "relation-type-ref";
    /** Value: String */
    public final static String DATE_OF_CREATION = "date-of-creation";
    /** Value: String */
    public final static String DATE_OF_LAST_MODIFICATION = "date-of-last-modification";
    /** Value: String */
    public final static String WORK_STATE = "work-state";

    // OBJECT DATA
    // -----------

    /** Value: String */
    public final static String TITLE = "title";
    /** Value: String */
    public final static String ABSTRACT = "abstract";
    /** description of obj-obj association<br>
     * Value: String */
    public final static String RELATION_DESCRIPTION = "relation-description";
    /** Value: String */
    public final static String DATASET_ALTERNATE_NAME = "dataset-alternate-name";
    /** Value: Double */
    public final static String VERTICAL_EXTENT_MINIMUM = "vertical-extent-minimum";
    /** Value: Double */
    public final static String VERTICAL_EXTENT_MAXIMUM = "vertical-extent-maximum";
    /** Value: Integer */
    public final static String VERTICAL_EXTENT_UNIT = "vertical-extent-unit";
    /** Value: Integer */
    public final static String VERTICAL_EXTENT_VDATUM = "vertical-extent-vdatum";
    /** Value: String */
    public final static String DESCRIPTION_OF_SPATIAL_DOMAIN = "description-of-spatial-domain";
    /** Value: String (Auswahllisten Eintrag, nicht ID) */
    public final static String TIME_TYPE = "time-type";
    /** Value: String */
    public final static String BEGINNING_DATE = "beginning-date";
    /** Value: String */
    public final static String ENDING_DATE = "ending-date";
    /** Value: Integer */
    public final static String TIME_STATUS = "time-status";
    /** Value: Integer */
    public final static String TIME_PERIOD = "time-period";
    /** Value: String */
    public final static String TIME_STEP = "time-step";
    /** Value: String */
    public final static String TIME_SCALE = "time-scale";
    /** Value: String */
    public final static String DESCRIPTION_OF_TEMPORAL_DOMAIN = "description-of-temporal-domain";
    /** Value: String */
    public final static String METADATA_LANGUAGE = "metadata-language";
    /** Value: String */
    public final static String DATA_LANGUAGE = "data-language";
    /** Value: Integer */
    public final static String PUBLICATION_CONDITION = "publication-condition";
    /** Value: String */
    public final static String DATASET_INTENSIONS = "dataset-intensions";
    /** Value: String */
    public final static String DATASET_USAGE = "dataset-usage";
    /** Value: String */
    public final static String ORDERING_INSTRUCTIONS = "ordering-instructions";
    /** Value: String */
    public final static String USE_CONSTRAINTS = "use-constraints";
    /** Value: String */
    public final static String FEES = "fees";

    
    // ADDRESS DATA
    // ------------

    /** Value: String */
    public final static String ORGANISATION = "organisation";
    /** Value: String */
    public final static String NAME = "name";
    /** Value: String */
    public final static String GIVEN_NAME = "given-name";
    /** Value: String */
    public final static String NAME_FORM = "name-form";
    /** Value: String */
    public final static String TITLE_OR_FUNCTION = "title-or-function";
    /** Value: String */
    public final static String STREET = "street";
    /** Value: String */
    public final static String POSTAL_CODE_OF_COUNTRY = "postal-code-of-country";
    /** Value: String */
    public final static String CITY = "city";
    /** Value: String */
    public final static String POST_BOX_POSTAL_CODE = "post-box-postal-code";
    /** Value: String */
    public final static String POST_BOX = "post-box";
    /** Value: String */
    public final static String FUNCTION = "function";
    /** Value: String */
    public final static String ADDRESS_DESCRIPTION = "address-description";

    /** Value: List of IngridDocs */
    public final static String COMMUNICATION = "communication";
    /** Value: String */
    public final static String COMMUNICATION_MEDIUM = "communication-medium";
    /** Value: String */
    public final static String COMMUNICATION_VALUE = "communication-value";
    /** Value: String */
    public final static String COMMUNICATION_DESCRIPTION = "communication-description";


}
