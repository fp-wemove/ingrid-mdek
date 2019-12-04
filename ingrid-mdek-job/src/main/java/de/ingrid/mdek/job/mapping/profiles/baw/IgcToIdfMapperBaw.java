package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DOMUtils.IdfElement;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.iplug.dsc.utils.TransformationUtils;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static de.ingrid.mdek.job.mapping.profiles.baw.BawConstants.*;

@Order(2)
public class IgcToIdfMapperBaw implements IIdfMapper {

    private static final Logger LOG = Logger.getLogger(IgcToIdfMapperBaw.class);

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private static final String CODELIST_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#";
    private static final String UDUNITS_CODESPACE_VALUE = "https://www.unidata.ucar.edu/software/udunits/";

    private static final String BAW_DEFAULT_KEYWORD_TYPE = "discipline";
    private static final String BAW_MODEL_THESAURUS_TITLE_PREFIX = "de.baw.codelist.model.";
    private static final String BAW_KEYWORD_CATALOGUE_TITLE = "BAW-Schlagwortkatalog";
    private static final String BAW_MODEL_THESAURUS_DATE = "2017-01-17";
    private static final String BAW_KEYWORD_CATALOGUE_DATE = "2012-01-01";
    private static final String BAW_DEFAULT_THESAURUS_DATE_TYPE = "publication";

    private static final String VV_WSV_1103_TITLE = "VV-WSV 1103";
    private static final String VV_WSV_1103_DATE = "2019-05-29";
    private static final String VV_WSV_1103_DATE_TYPE = "publication";

    private static final String GCO_CHARACTER_STRING_QNAME = "gco:CharacterString";
    private static final String VALUE_UNIT_ID_PREFIX = "valueUnit_";

    private DOMUtils domUtil;
    private SQLUtils sqlUtils;
    private TransformationUtils trafoUtil;

    private static final List<String> MD_METADATA_CHILDREN = Arrays.asList(
            "gmd:fileIdentifier",
            "gmd:language",
            "gmd:characterSet",
            "gmd:parentIdentifier",
            "gmd:hierarchyLevel",
            "gmd:hierarchyLevelName",
            "gmd:contact",
            "gmd:dateStamp",
            "gmd:metadataStandardName",
            "gmd:metadataStandardVersion",
            "gmd:dataSetURI",
            "gmd:locale",
            "gmd:spatialRepresentationInfo",
            "gmd:referenceSystemInfo",
            "gmd:metadataExtensionInfo",
            "gmd:identificationInfo",
            "gmd:contentInfo",
            "gmd:distributionInfo",
            "gmd:dataQualityInfo",
            "gmd:portrayalCatalogueInfo",
            "gmd:metadataConstraints",
            "gmd:applicationSchemaInfo",
            "gmd:metadataMaintenance",
            "gmd:series",
            "gmd:describes",
            "gmd:propertyType",
            "gmd:featureType",
            "gmd:featureAttribute"
    );
    private static final List<String> MD_IDENTIFICATION_CHILDREN = Arrays.asList(
            "gmd:citation",
            "gmd:abstract",
            "gmd:purpose",
            "gmd:credit",
            "gmd:status",
            "gmd:pointOfContact",
            "gmd:resourceMaintenance",
            "gmd:graphicOverview",
            "gmd:resourceFormat",
            "gmd:descriptiveKeywords",
            "gmd:resourceSpecificUsage",
            "gmd:resourceConstraints",
            "gmd:aggregationInfo",
            "gmd:spatialRepresentationType",
            "gmd:spatialResolution",
            "gmd:language",
            "gmd:characterSet",
            "gmd:topicCategory",
            "gmd:environmentDescription",
            "gmd:extent",
            "gmd:supplementalInformation"
    );

    @Override
    public void map(SourceRecord sourceRecord, Document target) throws Exception {
        if (!(sourceRecord instanceof DatabaseSourceRecord)) {
            throw new IllegalArgumentException("Record is no DatabaseRecord!");
        }

        LOG.debug("Additional BAW specific mapping from source record to idf document: " + sourceRecord.toString());

        domUtil = new DOMUtils(target, XPATH);
        domUtil.addNS("idf", "http://www.portalu.de/IDF/1.0");
        domUtil.addNS("gmd", Csw202NamespaceContext.NAMESPACE_URI_GMD);
        domUtil.addNS("gco", Csw202NamespaceContext.NAMESPACE_URI_GCO);
        domUtil.addNS("gml", Csw202NamespaceContext.NAMESPACE_URI_GML);
        domUtil.addNS("xlink", Csw202NamespaceContext.NAMESPACE_URI_XLINK);

        try {
            Connection connection = (Connection) sourceRecord.get(DatabaseSourceRecord.CONNECTION);
            sqlUtils = new SQLUtils(connection);
            trafoUtil = new TransformationUtils(sqlUtils);

            // Fetch elements for use later on
            Element mdMetadata = (Element) XPATH.getNode(target, "/idf:html/idf:body/idf:idfMdMetadata");

            String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification|./gmd:identificationInfo/srv:SV_ServiceIdentification";
            IdfElement mdIdentification = domUtil.getElement(mdMetadata, xpath);

            @SuppressWarnings("unchecked")
            Map<String, String> idxDoc = (Map<String, String>) sourceRecord.get("idxDoc");

            // ===== Operations that don't require a database data =====
            logMissingMetadataContact(mdMetadata);
            addWaterwayInformation(mdMetadata, idxDoc);

            // ===== Operations that require a database data =====

            // id is primary key and cannot be duplicate. Fetch the only record from the database
            Long objId = Long.parseLong((String) sourceRecord.get("id"));
            Map<String, String> objRow = sqlUtils.first("SELECT * FROM t01_object WHERE id=?", new Object[]{objId});
            if (objRow == null || objRow.isEmpty()) {
                LOG.info("No database record found in table t01_object for id: " + objId);
                return;
            }

            setHierarchyLevelName(mdMetadata, objId);
            addAuftragsInfos(mdIdentification, objId);
            addBWaStrIdentifiers(mdIdentification, objId);
            addBawKewordCatalogeKeywords(mdIdentification, objId);
            addSimSpatialDimensionKeyword(mdIdentification, objId);
            addSimModelMethodKeyword(mdIdentification, objId);
            addSimModelTypeKeywords(mdIdentification, objId);
            addTimestepSizeElement(mdMetadata, objId);
            addDgsValues(mdMetadata, objId);
            changeMetadataDateAsDateTime(mdMetadata, objRow.get("mod_time"));
        } catch (Exception e) {
            LOG.error("Error mapping source record to idf document.", e);
            throw e;
        }
    }

    private void logMissingMetadataContact(Node mdMetadata) {
        if (!XPATH.nodeExists(mdMetadata, "gmd:contact")) {
            LOG.error("No responsible party for metadata found!");
        }
    }

    private void setHierarchyLevelName(Element mdMetadata, Long objId) throws SQLException {
        String hlName = getFirstAdditionalFieldValue(objId, "bawHierarchyLevelName");
        if (hlName == null || hlName.trim().isEmpty()) return;

        String hlNameQname = "gmd:hierarchyLevelName";
        String hlNamePath = hlNameQname + '/' + GCO_CHARACTER_STRING_QNAME;

        Element existingNode = (Element) XPATH.getNode(mdMetadata, hlNameQname);
        if (existingNode == null) {
            IdfElement previousSibling = findPreviousSibling(hlNameQname, mdMetadata, MD_METADATA_CHILDREN);
            IdfElement hlNameElement;
            if (previousSibling == null) {
                hlNameElement = domUtil.addElement(mdMetadata, hlNamePath);
            } else {
                hlNameElement = previousSibling.addElementAsSibling(hlNamePath);
            }
            hlNameElement.addText(hlName);
        } else {
            domUtil.addText(existingNode, hlName);
        }
    }

    private void addAuftragsInfos(IdfElement mdIdentification, Long objId) throws SQLException {
        String number = getFirstAdditionalFieldValue(objId, "bawAuftragsnummer");
        String title = getFirstAdditionalFieldValue(objId, "bawAuftragstitel");


        if (number == null && title != null) {
            LOG.error("Auftragstitel is defined but no Auftragsnummer found for object with id: " + objId);
        }
        if (number != null && title == null) {
            LOG.error("Auftragsnummer is defined but no Auftragstitel found for object with id: " + objId);
        }
        if (number == null || title == null) return;

        String aggInfoQname = "gmd:aggregationInfo";
        IdfElement previousSibling = findPreviousSibling(aggInfoQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

        String aggInfoCitationPath = aggInfoQname + "/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation";
        IdfElement aggInfoCitationElement;
        if (previousSibling == null) {
            aggInfoCitationElement = mdIdentification.addElement(aggInfoCitationPath);
        } else {
            aggInfoCitationElement = previousSibling.addElementAsSibling(aggInfoCitationPath);
        }

        aggInfoCitationElement.addElement("gmd:title/gco:CharacterString")
                .addText(title);
        aggInfoCitationElement.addElement("gmd:date")
                .addAttribute("gco:nilReason", "unknown");
        aggInfoCitationElement.addElement("gmd:identifier/gmd:MD_Identifier/gmd:code")
                .addText(number);
    }

    private void addBWaStrIdentifiers(IdfElement mdIdentification, Long objId) throws SQLException {
        Map<Integer, Map<String, String>> rows = getOrderedAdditionalFieldDataTableRows(objId, "bwastrTable");

        String extentQname = "gmd:extent";
        IdfElement previousSibling = findPreviousSibling(extentQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

        for(Map.Entry<Integer, Map<String, String>> entry: rows.entrySet()) { // Sorted by index of table row
            Map<String, String> currentRow = entry.getValue();
            LOG.debug("Current BWaStr. Table Row: " + currentRow);

            String bwastrIdString = currentRow.get("bwastr_name");
            String bwastrKmStart = currentRow.get("bwastr_km_start");
            String bwastrKmEnd = currentRow.get("bwastr_km_end");


            int entryId = Integer.parseInt(bwastrIdString);
            String identifier = trafoUtil.getIGCSyslistEntryName(VV_1103_CODELIST_ID, entryId);
            if (identifier != null && bwastrKmStart != null && bwastrKmEnd != null) {
                previousSibling = addBWaStrExtentElement(mdIdentification, previousSibling, String.format("%04d-%s-%s", entryId, bwastrKmStart, bwastrKmEnd));
            } else if (identifier != null) {
                previousSibling = addBWaStrExtentElement(mdIdentification, previousSibling, identifier);
            }

        }
    }

    private IdfElement addBWaStrExtentElement(IdfElement mdIdentification, IdfElement previousSibling, String identifier) {
        String extentQname = "gmd:extent";
        IdfElement extentElement;
        if (previousSibling == null) {
            extentElement = mdIdentification.addElement(extentQname);
        } else {
            extentElement = previousSibling.addElementAsSibling(extentQname);
        }

        IdfElement exGeographicExtentElement = extentElement.addElement("gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription");
        exGeographicExtentElement.addElement("gmd:extentTypeCode/gco:Boolean")
                .addText("true");

        IdfElement mdIdentifierElement = exGeographicExtentElement.addElement("gmd:geographicIdentifier/gmd:MD_Identifier");

        IdfElement ciCitationElement = mdIdentifierElement.addElement("gmd:authority/gmd:CI_Citation");
        ciCitationElement.addElement("gmd:title/gco:CharacterString")
                .addText(VV_WSV_1103_TITLE);

        IdfElement ciDateElement = ciCitationElement.addElement("gmd:date/gmd:CI_Date");
        ciDateElement.addElement("gmd:date/gco:Date")
                .addText(VV_WSV_1103_DATE);
        ciDateElement.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                .addAttribute("codeList", CODELIST_URL + "CI_DateTypeCode")
                .addAttribute("codeListValue", VV_WSV_1103_DATE_TYPE);

        exGeographicExtentElement.addElement("gmd:code/gco:CharacterString")
                .addText(identifier);

        return extentElement;
    }

    private void addBawKewordCatalogeKeywords(IdfElement mdIdentification, Long objId) throws SQLException {
        List<Map<String, String>> rows = getOrderedAdditionalFieldDataTableRowData(objId, "bawKeywordCatalogueEntry");

        // Collect keywords to add them to the same descriptiveKeywords element
        List<String> allValues = new ArrayList<>(rows.size());
        for(Map<String, String> row: rows) {
            String entryId = row.get("data");
            if (entryId == null) continue;

            String value = trafoUtil.getIGCSyslistEntryName(BAW_KEYWORD_CATALOGUE_CODELIST_ID, Integer.parseInt(entryId));
            allValues.add(value);
        }

        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                BAW_KEYWORD_CATALOGUE_TITLE,
                BAW_KEYWORD_CATALOGUE_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                allValues.toArray(new String[0])
        );
    }

    private void addSimSpatialDimensionKeyword(IdfElement mdIdentification, Long objId) throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "simSpatialDimension");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation spatial dimensionality keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "dimensionality";

        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                value);
    }

    private void addSimModelMethodKeyword(IdfElement mdIdentification, Long objId) throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "simProcess");
        if (value == null) return; // There's nothing to do if there is no value

        LOG.debug("Adding BAW simulation modelling method keyword. Value found is: " + value);
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "method";

        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                value);
    }

    private void addSimModelTypeKeywords(IdfElement mdIdentification, Long objId) throws SQLException {
        List<Map<String, String>> rows = getOrderedAdditionalFieldDataTableRowData(objId, "simModelType");
        if (rows.isEmpty()) return;

        // Collect keywords to add them to the same descriptiveKeywords element
        String thesaurusTitle = BAW_MODEL_THESAURUS_TITLE_PREFIX + "type";
        List<String> allValues = new ArrayList<>(rows.size());
        for(Map<String, String> row: rows) {
            String entryId = row.get("data");
            if (entryId == null) continue;

            String value = trafoUtil.getIGCSyslistEntryName(BAW_MODEL_TYPE_CODELIST_ID, Integer.parseInt(entryId));
            LOG.debug("Adding BAW simulation model type keyword. Value found is: " + value);

            allValues.add(value);
        }
        addKeyword(
                mdIdentification,
                BAW_DEFAULT_KEYWORD_TYPE,
                thesaurusTitle,
                BAW_MODEL_THESAURUS_DATE,
                BAW_DEFAULT_THESAURUS_DATE_TYPE,
                allValues.toArray(new String[allValues.size()]));
    }

    private void changeMetadataDateAsDateTime(Node mdMetadata, String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            LOG.info("Database entry doesn't have a modified time.");
            return;
        }

        // We assume that a dateStamp node has been created by the previous mapper
        Element dateStampNode = (Element) XPATH.getNode(mdMetadata, "gmd:dateStamp");
        String isoDate = trafoUtil.getISODateFromIGCDate(dateString);
        if (isoDate != null && isoDate.contains("T")) { // Replacing Date to DateTime is really necessary
            XPATH.removeElementAtXPath(dateStampNode, "gco:Date");
            domUtil.addElement(dateStampNode, "gco:DateTime")
                    .addText(isoDate);
        }
    }

    private void addTimestepSizeElement(Element mdMetadata, Long objId) throws SQLException {
        String value = getFirstAdditionalFieldValue(objId, "dqAccTimeMeas");
        if (value == null) return; // There's nothing to do if there is no value

        IdfElement dqElement = modelScopedDqDataQualityElement(mdMetadata);

        IdfElement dqQuantitativeResult = dqElement.addElement("gmd:report/gmd:DQ_AccuracyOfATimeMeasurement/gmd:result/gmd:DQ_QuantitativeResult");
        addElementWithUnits(mdMetadata, dqQuantitativeResult, "gmd:valueUnit", "s");

        dqQuantitativeResult.addElement("gmd:value/gco:Record")
                .addAttribute("xsi:type", "xs:double")
                .addText(String.format("%.1f", Double.parseDouble(value)));
    }

    private void addDgsValues(Element mdMetadata, Long objId) throws SQLException {
        Map<Integer, Map<String, String>> groupedRows = getOrderedAdditionalFieldDataTableRows(objId, "simParamTable");

        for(Integer sort: groupedRows.keySet()) { // Sorted by index of table row
            Map<String, String> row = groupedRows.get(sort);
            boolean areValuesDiscrete = Boolean.parseBoolean(row.get("simParamHasDiscreteValues"));
            String name = row.get("simParamName");
            String type = trafoUtil.getIGCSyslistEntryName(BAW_SIMULATION_PARAMETER_TYPE_CODELIST_ID, Integer.parseInt(row.get("simParamType")));
            String units = row.get("simParamUnit");

            String query = "SELECT obj.data FROM additional_field_data obj " +
                    "JOIN additional_field_data obj_p ON obj_p.id = obj.parent_field_id " +
                    "JOIN additional_field_data obj_gp ON obj_gp.id = obj_p.parent_field_id " +
                    "WHERE obj_gp.obj_id=? AND obj_p.sort=? AND obj.field_key=? " +
                    "ORDER BY obj.sort";
            List<Map<String, String>> valueRows = sqlUtils.all(query, new Object[]{objId, sort, "simParamValue"});

            List<String> values = valueRows.stream()
                    .map(e -> e.get("data"))
                    .collect(Collectors.toList());

            IdfElement dqElement = modelScopedDqDataQualityElement(mdMetadata);
            IdfElement resultElement = dqElement.addElement("gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result");

            resultElement.addElement("gmd:DQ_QuantitativeResult/gmd:valueType/gco:TypeName/gco:aName/gco:CharacterString")
                    .addText(name);

            addElementWithUnits(mdMetadata, resultElement, "gmd:valueUnit", units);

            boolean valuesAreDoubles = false;
            if (areValuesDiscrete) {
                String typeAttr;
                if (areValuesIntegers(values)) {
                    typeAttr = "xs:integer";
                } else if (areValuesDoubles(values)) {
                    typeAttr = "xs:double";
                    valuesAreDoubles = true;
                } else {
                    typeAttr = "xs:string";
                }
                for(String val: values) {
                    if (valuesAreDoubles) {
                        val = String.format("%.1f", Double.parseDouble(val));
                    }
                    resultElement.addElement("gmd:value/gco:Record")
                            .addAttribute("xsi:type", typeAttr)
                            .addText(val);
                }
            } else {
                String typeAttr;
                if (areValuesIntegers(values)) {
                    typeAttr = "gml:integerList";
                } else {
                    typeAttr = "gml:doubleList";
                }
                String val = String.format("%s %s", values.get(0), values.get(1));
                resultElement.addElement("gmd:value/gco:Record")
                        .addAttribute("xsi:type", typeAttr)
                        .addText(val);
            }

            dqElement.addElement("gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description")
                    .addText(type);
        }
    }

    private IdfElement modelScopedDqDataQualityElement(Element mdMetadata) {
        if (domUtil.getNS("xs") == null) {
            domUtil.addNS("xs", "http://www.w3.org/2001/XMLSchema");
        }

        String dqInfoQname = "gmd:dataQualityInfo";
        String dqInfoPath = dqInfoQname + "/gmd:DQ_DataQuality";

        IdfElement previousSibling = findPreviousSibling(dqInfoQname, mdMetadata, MD_METADATA_CHILDREN);

        IdfElement dqElement;
        if (previousSibling == null) {
            dqElement = domUtil.addElement(mdMetadata, dqInfoPath);
        } else {
            dqElement = previousSibling.addElementAsSibling(dqInfoPath);
        }

        dqElement.addElement("gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode")
                .addAttribute("codeList", CODELIST_URL + "MD_ScopeCode")
                .addAttribute("codeListValue", "model");

        return dqElement;
    }

    private boolean areValuesIntegers(List<String> values) {
        for(String val: values) {
            try {
                Integer.parseInt(val);
            } catch (NumberFormatException unused) {
                return false;
            }
        }
        return true;
    }

    private boolean areValuesDoubles(List<String> values) {
        for(String val: values) {
            try {
                Double.parseDouble(val);
            } catch (NumberFormatException unused) {
                return false;
            }
        }
        return true;
    }

    private void addWaterwayInformation(Element mdMetadata, Map<String, String> idxDoc) {
        IdfElement additionalDataSection = domUtil.addElement(mdMetadata, "idf:additionalDataSection")
                .addAttribute("id", "bawDmqsAdditionalFields");
        additionalDataSection.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("BAW DMQS Zusatzfelder");

        // bwstr-bwastr_name (Bundeswasserstrassen Name)
        IdfElement field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-bwastr_name");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Bwstr Name");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-bwastr_name"));

        // bwstr-strecken_name (Streckenname des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-strecken_name");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Bwstr Streckenname");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-strecken_name"));

        // bwstr-center-lon (Longitude des Zentrums des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-center-lon");
        field.addElement("idf:title")
                .addAttribute("lang", "de")
                .addText("Longitude des Zentrums des Abschnitts");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-center-lon"));

        // bwstr-center-lat (Latitude des Zentrums des Abschnitts)
        field = additionalDataSection.addElement("idf:additionalDataField")
                .addAttribute("id", "bwstr-center-lat");
        field.addElement("idf:title").addAttribute("lang", "de")
                .addText("Latitude des Zentrums des Abschnitts");
        field.addElement("idf:data")
                .addText(idxDoc.get("bwstr-center-lat"));
    }

    private void addKeyword(
            IdfElement mdIdentification,
            String keywordType,
            String thesuarusName,
            String thesaurusDate,
            String thesaurusDateType,
            String... keywords) {
        String keywordQname = "gmd:descriptiveKeywords";

        IdfElement previousSibling = findPreviousSibling(keywordQname, mdIdentification.getElement(), MD_IDENTIFICATION_CHILDREN);

        IdfElement keywordElement;
        if (previousSibling == null) {
            keywordElement = mdIdentification.addElement(keywordQname);
        } else {
            keywordElement = previousSibling.addElementAsSibling(keywordQname);
        }

        IdfElement mdKeywordElement = keywordElement.addElement("gmd:MD_Keywords");
        for(String keyword: keywords) {
            mdKeywordElement.addElement("gmd:keyword/gco:CharacterString")
                    .addText(keyword);
        }
        mdKeywordElement.addElement("gmd:type/gmd:MD_KeywordTypeCode")
                .addAttribute("codeList", CODELIST_URL + "MD_KeywordTypeCode")
                .addAttribute("codeListValue", keywordType);

        IdfElement thesaurusElement = keywordElement.addElement("gmd:thesaurusName/gmd:CI_Citation");
        thesaurusElement.addElement("gmd:title/gco:CharacterString")
                .addText(thesuarusName);

        IdfElement thesaurusDateElement = thesaurusElement.addElement("gmd:date/gmd:CI_Date");
        thesaurusDateElement.addElement("gmd:date/gco:Date")
                .addText(thesaurusDate);
        thesaurusDateElement.addElement("gmd:dateType/gmd:CI_DateTypeCode")
                .addAttribute("codeList", CODELIST_URL + "CI_DateTypeCode")
                .addAttribute("codeListValue", thesaurusDateType);
    }

    private String getFirstAdditionalFieldValue(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj WHERE obj.obj_id=? AND obj.field_key=?";
        Map<String, String> row = sqlUtils.first(query, new Object[]{objId, fieldKey});
        if (row == null) {
            return null;
        } else {
            return row.get("data");
        }
    }

    private List<Map<String, String>> getOrderedAdditionalFieldDataTableRowData(Long objId, String fieldKey) throws SQLException {
        String query = "SELECT obj.data FROM additional_field_data obj " +
                "JOIN additional_field_data obj_parent ON obj_parent.id = obj.parent_field_id " +
                "WHERE obj_parent.obj_id=? AND obj.field_key=? " +
                "ORDER BY obj_parent.sort";
        List<Map<String, String>> result = sqlUtils.all(query, new Object[]{objId, fieldKey});
        return result == null ? Collections.emptyList() : result;
    }

    private Map<Integer, Map<String, String>> getOrderedAdditionalFieldDataTableRows(Long objId, String fieldKey) throws SQLException {
        String query;
        query = "SELECT obj.sort, obj.field_key, obj.data FROM additional_field_data obj " +
                "JOIN additional_field_data obj_parent ON obj_parent.id = obj.parent_field_id " +
                "WHERE obj_parent.obj_id=? AND obj_parent.field_key=? " +
                "ORDER BY obj.sort";
        List<Map<String, String>> allRows = sqlUtils.all(query, new Object[]{objId, fieldKey});
        if (allRows == null) return Collections.emptyMap();

        Map<Integer, Map<String, String>> groupedRows = new TreeMap<>(); // Keys are sorted
        for(Map<String, String> row: allRows) {
            Integer sort = Integer.valueOf(row.get("sort"));
            groupedRows.putIfAbsent(sort, new HashMap<>());
            groupedRows.get(sort).put(row.get("field_key"), row.get("data"));
        }
        return groupedRows;
    }

    private void addElementWithUnits(Element mdMetadata, IdfElement parent, String qname, String units) {
        if (units == null || units.trim().isEmpty()) {
            parent.addElement(qname)
                    .addAttribute("gco:nilReason", "inapplicable");
            return;
        }

        String unitsIdentifierText = units
                .replaceAll("μ", "mu")
                .replaceAll("Ω", "OMEGA")
                .replaceAll("°", "degrees")
                .replaceAll("′", "arc_minutes")
                .replaceAll("″", "arc_seconds")
                .replaceAll("%", "percent")
                .replaceAll("‰", "per_mille")
                .replaceAll(" +", "_");
        String unitsGmlId = VALUE_UNIT_ID_PREFIX + unitsIdentifierText;
        boolean nodeExists = XPATH.nodeExists(mdMetadata, "//*[@id='" + unitsGmlId + "']");

        if (nodeExists) {
            parent.addElement(qname)
                    .addAttribute("xlink:href", "#" + unitsGmlId);
        } else {
            IdfElement unitDefinitionElement = parent.addElement(qname + "/gml:UnitDefinition");
            unitDefinitionElement.addAttribute("gml:id", unitsGmlId);

            unitDefinitionElement.addElement("gml:identifier")
                    .addAttribute("codeSpace", UDUNITS_CODESPACE_VALUE)
                    .addText(unitsIdentifierText);
            unitDefinitionElement.addElement("gml:catalogSymbol")
                    .addText(units);
        }
    }

    private IdfElement findPreviousSibling(String qname, Element parent, List<String> allSiblingQnames) {
        int idxStart = allSiblingQnames.indexOf(qname);
        IdfElement previousSibling = null;
        for(int i=idxStart; i>=0 && previousSibling == null; i--) { // breaks as soon as previousSibling is found (!= null)
            previousSibling = domUtil.getElement(parent, allSiblingQnames.get(i) + "[last()]");
        }
        return previousSibling;
    }
}
