/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
    CAPABILITIES = Java.type('de.ingrid.utils.capabilities.CapabilitiesUtils');
}

importPackage(Packages.org.w3c.dom);
importPackage(Packages.de.ingrid.iplug.dsc.om);

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

var mdMetadataChildrenReverseOrder = [
    "gmd:featureAttribute",
    "gmd:featureType",
    "gmd:propertyType",
    "gmd:describes",
    "gmd:series",
    "gmd:metadataMaintenance",
    "gmd:applicationSchemaInfo",
    "gmd:metadataConstraints",
    "gmd:portrayalCatalogueInfo",
    "gmd:dataQualityInfo",
    "gmd:distributionInfo",
    "gmd:contentInfo",
    "gmd:identificationInfo",
    "gmd:metadataExtensionInfo",
    "gmd:referenceSystemInfo",
    "gmd:spatialRepresentationInfo",
    "gmd:locale",
    "gmd:dataSetURI",
    "gmd:metadataStandardVersion",
    "gmd:metadataStandardName",
    "gmd:dateStamp",
    "gmd:contact",
    "gmd:hierarchyLevelName",
    "gmd:hierarchyLevel",
    "gmd:parentIdentifier",
    "gmd:characterSet",
    "gmd:language",
    "gmd:fileIdentifier"
];

var mdDataIdentificationChildrenReverseOrder = [
    "gmd:supplementalInformation",
    "gmd:extent",
    "gmd:environmentDescription",
    "gmd:topicCategory",
    "gmd:characterSet",
    "gmd:language",
    "gmd:spatialResolution",
    "gmd:spatialRepresentationType",
    "gmd:aggregationInfo",
    "gmd:resourceConstraints",
    "gmd:resourceSpecificUsage",
    "gmd:descriptiveKeywords",
    "gmd:resourceFormat",
    "gmd:graphicOverview",
    "gmd:resourceMaintenance",
    "gmd:pointOfContact",
    "gmd:status",
    "gmd:credit",
    "gmd:purpose",
    "gmd:abstract",
    "gmd:citation"
];

// Start mapping
log.debug("Starting mapping for HZG");

var mdMetadata = XPATH.getNode(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata");

var objId = parseInt(sourceRecord.get("id"));

// ========== Observed Properties ==========
var query = "SELECT DISTINCT fdc.sort AS sort FROM additional_field_data fdp " +
    "JOIN additional_field_data fdc ON fdc.parent_field_id = fdp.id " +
    "WHERE fdp.obj_id=? AND fdp.field_key=? " +
    "ORDER BY fdc.sort";
var sortRows = SQL.all(query, [objId, "observedPropertiesDataGrid"]);

query = "SELECT fdc.data AS data FROM additional_field_data fdp " +
    "JOIN additional_field_data fdc ON fdc.parent_field_id = fdp.id " +
    "WHERE fdp.obj_id=? AND fdp.field_key=? AND fdc.sort=? AND fdc.field_key=?";

for (var i=0; sortRows && i<sortRows.length; i++) {
    var sort = parseInt(sortRows[i].get("sort"));

    log.debug("Processing row " + sort + " of the observed properties table");

    var nameRow = SQL.first(query, [objId, "observedPropertiesDataGrid", sort, "observedPropertyName"]);
    var xmlDescriptionRow = SQL.first(query, [objId, "observedPropertiesDataGrid", sort, "observedPropertyXmlDescription"]);

    if (!hasValue(nameRow) || !hasValue(xmlDescriptionRow)) {
        log.error("Observed property is missing a 'Name' or 'XML-Description'.");
        continue;
    }

    var name = nameRow.get("data");
    var xmlDescription = xmlDescriptionRow.get("data");

    var catalogDesc;
    var nextSibling = searchNextSiblingTag(mdMetadata, mdMetadataChildrenReverseOrder, "gmd:contentInfo");
    if (nextSibling) {
        catalogDesc = nextSibling.addElementAsSibling("gmd:contentInfo/gmd:MD_FeatureCatalogueDescription");
    } else {
        catalogDesc = mdMetadata.addElement("gmd:contentInfo/gmd:MD_FeatureCatalogueDescription");
    }

    catalogDesc.addElement("gmd:includedWithDataset/gco:Boolean")
        .addText("false");

    catalogDesc.addElement("gmd:featureTypes/gco:LocalName")
        .addText(xmlDescription);

    var citation = catalogDesc.addElement("gmd:featureCatalogueCitation/gmd:CI_Citation");
    citation.addElement("gmd:title/gco:CharacterString")
        .addText(name);
    citation.addElement("gmd:date")
        .addAttribute("gco:nilreason", "inapplicable");
}

// ========== Platform references ==========
var mdDataIdentification = XPATH.getNode(mdMetadata, "gmd:identificationInfo/gmd:MD_DataIdentification");
if (!mdDataIdentification) {
    log.error("Could not locate the gmd:MD_DataIdentification node for adding platform information");
}

log.debug("Looking for references to platforms.");
query = "SELECT obj_to_uuid FROM object_reference WHERE obj_from_id=? AND special_ref=8001";
var referenceRows = SQL.all(query, [objId]);
for(var i=0; mdDataIdentification && referenceRows && i<referenceRows.length; i++) {
    var objToUuid = referenceRows[i].get("obj_to_uuid");

    log.debug("Found reference to platform with UUID: " + objToUuid);

    var nextSibling = searchNextSiblingTag(mdDataIdentification, mdDataIdentificationChildrenReverseOrder, "gmd:aggregationInfo");
    var mdAggregateInformation;
    if (nextSibling) {
        mdAggregateInformation = nextSibling.addElementAsSibling("gmd:aggregationInfo/gmd:MD_AggregateInformation");
    } else {
        mdAggregateInformation = mdDataIdentification.addElement("gmd:aggregationInfo/gmd:MD_AggregateInformation");
    }

    mdAggregateInformation.addElement("gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
        .addText(objToUuid);

    var value = "crossReference";
    mdAggregateInformation.addElement("gmd:associationType/gmd:DS_AssociationTypeCode")
        .addAttribute("codeList", globalCodeListAttrURL + "#DS_AssociationTypeCode")
        .addAttribute("codeListValue", value)
        .addText(value);
}

function searchNextSiblingTag(parentNode, siblingNamesReverseOrder, tagName) {
    var index = siblingNamesReverseOrder.indexOf(tagName);
    var nextSibling = null;
    for (var i=index; i<siblingNamesReverseOrder.length && !nextSibling; i++) {
        nextSibling = DOM.getElement(parentNode, siblingNamesReverseOrder[i] + "[last()]");
    }
    return nextSibling;
}