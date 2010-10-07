package de.ingrid.mdek.xml;

/**
 * Helper class encapsulating versioning stuff of Export/Import Format.<br/>
 */
public class Versioning extends de.ingrid.mdek.Versioning {
	/** Current Version of XML Exchange Format */
	public static final String CURRENT_IMPORT_EXPORT_VERSION = "1.0.9";

	/** Current Mapper for importing current format ! */
	public static final Class CURRENT_IMPORT_MAPPER_CLASS =
		de.ingrid.mdek.xml.importer.mapper.version109.IngridXMLMapperImpl.class;
}
