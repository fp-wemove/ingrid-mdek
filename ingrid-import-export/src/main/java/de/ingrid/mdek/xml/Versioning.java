package de.ingrid.mdek.xml;

/**
 * Helper class encapsulating versioning stuff of Export/Import Format.<br/>
 */
public class Versioning extends de.ingrid.mdek.Versioning {
	/** Current Version of XML Exchange Format */
	public static final String CURRENT_IMPORT_EXPORT_VERSION = "3.0.0";

	/** Current Mapper for importing current format ! */
	public static final Class CURRENT_IMPORT_MAPPER_CLASS =
		de.ingrid.mdek.xml.importer.mapper.version230.IngridXMLMapperImpl.class;
	// TODO MM
//		de.ingrid.mdek.xml.importer.mapper.version300.IngridXMLMapperImpl.class;
}
