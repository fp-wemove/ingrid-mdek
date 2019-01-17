/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
}

importPackage(Packages.org.apache.lucene.document);
importPackage(Packages.de.ingrid.iplug.dsc.om);
importPackage(Packages.de.ingrid.geo.utils.transformation);
importPackage(Packages.de.ingrid.iplug.dsc.index.mapper);

// constant to punish the rank of a service/data object, which has no coupled resource
var BOOST_NO_COUPLED_RESOURCE  = 0.9;
//constant to boost the rank of a service/data object, which has at least one coupled resource
var BOOST_HAS_COUPLED_RESOURCE = 1.0;

if (log.isDebugEnabled()) {
    log.debug('Mapping source record to lucene document: ' + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException('Record is no DatabaseRecord!');
}

// add default boost value
IDX.addDocumentBoost(1.0);

// **********************************************
// The following mapping object must be equal to:
//     https://gitlab.wemove.com/mcloud/mcloud-ckan-importer/tree/develop/server/model/index-document.ts
// **********************************************
function map(mapper) {
    return {
        title: mapper.getTitle(),
        description: mapper.getDescription(),
        theme: mapper.getThemes(),
        issued: mapper.getIssued(),
        modified: mapper.getModifiedDate(),
        accrualPeriodicity: mapper.getAccrualPeriodicity(),
        keywords: mapper.getKeywords(),
        creator: mapper.getCreator(),
        publisher: mapper.getPublisher(),
        accessRights: mapper.getAccessRights(),
        distribution: mapper.getDistributions(),
        extras: {
            metadata: {
                source: mapper.getMetadataSource(),
                issued: mapper.getMetadataIssued(),
                modified: mapper.getMetadataModified(),
                harvested: mapper.getMetadataHarvested(),
                harvesting_errors: null // get errors after all operations been done
            },
            generated_id: mapper.getGeneratedId(),
            subgroups: mapper.getCategories(),
            license_id: mapper.getLicenseId(),
            license_title: mapper.getLicenseTitle(),
            license_url: mapper.getLicenseURL(),
            harvested_data: mapper.getHarvestedData(),
            subsection: mapper.getSubSections(),
            temporal: mapper.getTemporal(),
            groups: mapper.getGroups(),
            displayContact: mapper.getDisplayContacts(),
            all: mapper.getExtrasAllData(),
            temporal_start: mapper.getTemporalStart(),
            temporal_end: mapper.getTemporalEnd(),
            realtime: mapper.isRealtime(),
            citation: mapper.getCitation(),
            mfund_fkz: mapper.getMFundFKZ(),
            mfund_project_title: mapper.getMFundProjectTitle()
        }
    };
}

// ---------- t01_object ----------
var objId = +sourceRecord.get('id');
var objRows = SQL.all('SELECT * FROM t01_object WHERE id=?', [objId]);
for (var i=0; i<objRows.size(); i++) {

    var objRow = objRows.get(i);
    var objClass = objRow.get('obj_class');
    var objUuid = objRow.get('obj_uuid')

    log.info("Map ID: " + objId);
    var mapper = new McloudMapper({
        objId: objId,
        objUuid: objUuid,
        objRow: objRow
    });
    var doc = map(mapper);

    log.info("add doc to index");
    IDX.addAllFromJSON(JSON.stringify(doc));

}


