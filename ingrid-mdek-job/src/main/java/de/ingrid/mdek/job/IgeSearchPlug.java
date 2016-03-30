/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.job;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

@Service("ige")
public class IgeSearchPlug extends HeartBeatPlug implements IRecordLoader {

    private static Log log = LogFactory.getLog( IgeSearchPlug.class );

    @Autowired
    @Qualifier("dscRecordCreator")
    private DscRecordCreator dscRecordProducerObject = null;

    @Autowired
    @Qualifier("dscRecordCreatorAddress")
    private DscRecordCreator dscRecordProducerAddress = null;

    @Autowired
    private MdekIdcCatalogJob catalogJob = null;

    @Autowired
    private MdekIdcObjectJob objectJob = null;

    private final IndexImpl _indexSearcher;
    
    private XPathUtils utils = new XPathUtils( new Csw202NamespaceContext() );

    private String adminUserUUID;

    @Autowired
    public IgeSearchPlug(final IndexImpl indexSearcher, IPlugdescriptionFieldFilter[] fieldFilters, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors)
            throws IOException {
        super( 60000, new PlugDescriptionFieldFilters( fieldFilters ), injector, preProcessors, postProcessors );
        _indexSearcher = indexSearcher;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug( "Incoming query: " + query.toString() + ", start=" + start + ", length=" + length );
        }
        preProcess( query );
        return _indexSearcher.search( query, start, length );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        ElasticDocument document = _indexSearcher.getDocById( hit.getDocumentId() );
        // TODO: choose between different mapping types
        if (document != null) {
            if (document.get( "t01_object.id" ) != null) {
                return dscRecordProducerObject.getRecord( document );
            } else {
                return dscRecordProducerAddress.getRecord( document );
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() throws Exception {
        _indexSearcher.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail detail = _indexSearcher.getDetail( hit, query, fields );
        return detail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail[] details = _indexSearcher.getDetails( hits, query, fields );
        return details;
    }

    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        switch (info.getMethod()) {
        case "importCSWDoc":
            doc = cswTransaction( (String) info.getParameter() );
        }

        return doc;
    }

    public IngridDocument cswTransaction(String xml) {
        IngridDocument doc = new IngridDocument();

        DocumentBuilderFactory factory;
        IngridDocument resultInsert = null;
        IngridDocument resultUpdate = null;
        IngridDocument resultDelete = null;
        int deletedObjects = 0;
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
            NodeList insertDocs = xmlDoc.getElementsByTagName( "csw:Insert" );
            NodeList updateDocs = xmlDoc.getElementsByTagName( "csw:Update" );
            NodeList deleteDocs = xmlDoc.getElementsByTagName( "csw:Delete" );
            
            adminUserUUID = catalogJob.getCatalogAdminUserUuid();
            
            catalogJob.beginTransaction();
                        
            /**
             * INSERT DOCS
             */
            for (int i = 0; i < insertDocs.getLength(); i++) {
                IngridDocument document = prepareImportAnalyzeDocument( builder, insertDocs.item( i ) );
                //document.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, i==0 ? true : false );
                IngridDocument analyzerResult = catalogJob.analyzeImportData( document );
                resultInsert = catalogJob.importEntities( prepareImportDocument() );
            }
            
            /**
             * UPDATE DOCS
             */
            for (int i = 0; i < updateDocs.getLength(); i++) {
                Node item = updateDocs.item( i );
                String propName = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                String propValue = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:Literal" );
                
                if ("uuid".equals( propName ) && propValue != null) {
                    IngridDocument document = prepareImportAnalyzeDocument( builder, updateDocs.item( i ) );
                
                    IngridDocument analyzerResult = catalogJob.analyzeImportData( document );
                    resultUpdate = catalogJob.importEntities( document );
                }
            }
            
            /**
             * DELETE DOCS
             */
            for (int i = 0; i < deleteDocs.getLength(); i++) {
                Node item = deleteDocs.item( i );
                String propName = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                String propValue = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:Literal" );

                if ("uuid".equals( propName ) && propValue != null) {
                    IngridDocument params = new IngridDocument();
                    params.put( MdekKeys.USER_ID, "TEST_USER_ID" );
                    params.put( MdekKeys.UUID, propValue );
                    params.put( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, false );

                    resultDelete = objectJob.deleteObject( params );
                    deletedObjects++;
                } else {
                    log.warn( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                }
            }
            
            catalogJob.commitTransaction();
            
            
            doc.putBoolean( "success", true );

        } catch (Exception e) {
            catalogJob.rollbackTransaction();
            e.printStackTrace();
            doc.put( "error", e.getMessage() );
            doc.putBoolean( "success", false);
        } finally {
            IngridDocument result = new IngridDocument();
            result.putInt( "inserts", resultInsert == null ? 0 : resultInsert.getInt(MdekKeys.JOBINFO_NUM_OBJECTS) );
            result.putInt( "updates", resultUpdate == null ? 0 : resultUpdate.getInt(MdekKeys.JOBINFO_NUM_OBJECTS) );
            result.putInt( "deletes", deletedObjects );
            result.put( "resultInserts", resultInsert );
            result.put( "resultUpdates", resultUpdate );
            doc.put( "result", result );
        }

        return doc;
    }

    private IngridDocument prepareImportAnalyzeDocument(DocumentBuilder builder, Node doc) throws Exception {
        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode( doc.getFirstChild().getNextSibling(), true );
        singleInsertDocument.appendChild( importedNode );
        String insertDoc = XMLUtils.toString( singleInsertDocument );

        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, adminUserUUID );
        
        // TODO: it should not be neccessary to provide an object and address node for the import!
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278" );  // IMPORTKNOTEN
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355" ); // IMPORTKNOTEN
        
        // docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, GZipTool.gzip( insertDoc ).getBytes());
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, catalogJob.compress( new ByteArrayInputStream( insertDoc.getBytes() ) ).toByteArray() );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_TRANSACTION_IS_HANDLED, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, false );
        return docIn;
    }
    
    private IngridDocument prepareImportDocument() throws Exception {
        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, adminUserUUID );
        // TODO: it should not be neccessary to provide an object and address node for the import!
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278" );  // IMPORTKNOTEN
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355" ); // IMPORTKNOTEN
        
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_TRANSACTION_IS_HANDLED, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, true );
        
        return docIn;
    }

    public void setCatalogJob(MdekIdcCatalogJob catalogJob) {
        this.catalogJob = catalogJob;
    }
    
    public void setObjectJob(MdekIdcObjectJob objectJob) {
        this.objectJob = objectJob;
    }

}
