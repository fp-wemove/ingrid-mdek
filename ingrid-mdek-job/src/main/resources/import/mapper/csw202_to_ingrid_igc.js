/*
 * **************************************************-
 * Ingrid Portal MDEK Application
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
/**
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 * 
 * CSW 2.0.2 AP ISO 1.0 import script. This script translates an input xml from
 * the CSW 2.0.2 format structure into a IGC 
 * import format structure.
 * 
 * It uses a template that provides a basic IGC import format structure.
 * 
 * If the input document is invalid an Exception will be raised.
 *
 *
 * The following global variable are passed from the application:
 *
 * @param source A org.w3c.dom.Document instance, that defines the input
 * @param target A org.w3c.dom.Document instance, that defines the output, based on the IGC import format template.
 * @param protocol A Protocol instance to add UI protocol messages.
 * @param codeListService An instance of MdekCatalogService for accessing the codelist repository.
 * @param javaVersion A String with the java version.
 * @param SQL An instance of de.ingrid.iplug.dsc.utils.SQLUtils
 * @param XPATH Utils for XPath
 * @param TRANSF An instance of de.ingrid.iplug.dsc.utils.TransformationUtils
 * @param DOM An instance of de.ingrid.iplug.dsc.utils.DOMUtils
 * @param log A Log instance
 *
 *
 * Example to set debug message for protocol:
 * if(protocol.isDebugEnabled()){
 *		protocol.addMessage(protocol.getCurrentFilename() + ": Debug message");
 * }
 */
if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
    var ProtocolHandler = Java.type("de.ingrid.mdek.job.protocol.ProtocolHandler");
}

importPackage(Packages.de.ingrid.utils.udk);
importPackage(Packages.org.w3c.dom);
importClass(Packages.de.ingrid.utils.xml.XMLUtils);
importClass(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler);

var DEBUG = 1;
var INFO = 2;
var WARN = 3;
var ERROR = 4;

var mappingDescription = {"mappings":[
  		
  		
  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/general
  		//
  		// ****************************************************
  		{
  			// set the obj_class to a fixed value "Geoinformation/Karte"
  			"srcXpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/object-class",
  			"targetAttribute":"id",
  			"storeValue":"objectClass",
  			"transform":{
				"funct":getObjectClassFromHierarchyLevel
			}
  		},
		{
  			"srcXpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/title"
  		},
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:abstract/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/abstract"
  		},
		{
			"srcXpath":"//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]",
			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/metadata-date",
			"transform":{
				"funct":transformDateIso8601ToIndex
			}
		},
		{

			"srcXpath":"//gmd:fileIdentifier/gco:CharacterString",
			// make sure we always have a UUID
			"defaultValue":createUUID,
			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/original-control-identifier"
		},
  		{
			"srcXpath":"//gmd:parentIdentifier/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/parent-identifier-extern"
  		},
  		{	
  			"srcXpath":"//gmd:metadataStandardName/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/metadata/metadata-standard-name"
  		},
  		{	
  			"srcXpath":"//gmd:metadataStandardVersion/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/metadata/metadata-standard-version"
  		},
  		{	
  			"srcXpath":"//gmd:MD_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/metadata/metadata-character-set",
  			"targetAttribute":"iso-code",
  			"transform":{
				"funct":transformISOToIgcDomainId,
				"params":[510, "Could not tranform gmd:MD_CharacterSetCode: "]
			}
  		},
  		{
  		    "execute":{
  		        "funct":transformAlternateNameAndProductGroup
  		    }
  		},
        {   
            "srcXpath":"//gmd:identificationInfo/*/gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
            "targetNode":"/igc/data-sources/data-source/data-source-instance/general/dataset-character-set",
            "targetAttribute":"iso-code",
            "transform":{
                "funct":transformISOToIgcDomainId,
                "params":[510, "Could not tranform gmd:MD_CharacterSetCode: "]
            }
        },
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:topicCategory",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/general/topic-categories",
  			"newNodeName":"topic-category",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:MD_TopicCategoryCode",
			  			"targetNode":"",
			  			"targetAttribute":"id",
			  			"transform":{
							"funct":transformISOToIgcDomainId,
							"params":[527, "Could not tranform topic category: ", true]
						}
			  		}
			  	]
			}
  		},
		{
		    "execute": {
		    	"funct": handleBoundingPolygon
			}
		},

  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/technical-domain/map
  		//
  		// ****************************************************
  		{
  			"srcXpath":"/",
  			"targetNode":"/",
  			"conditional": {
  				"storedValue": {
  					"name":"objectClass",
  					"value":"1"
  				}
  			},
  			"subMappings": {
  			    "mappings": [
	         		{	
	        			"srcXpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
	        			"defaultValue":"5", // default to "dataset", if no hierarchyLevel is supplied
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/hierarchy-level",
	        			"targetAttribute":"iso-code",
		      			"transform":{
		      				"funct":transformGeneric,
		      				"params":[{"dataset":"5", "series":"6"}, false, "Could not map hierarchyLevel (only 'dataset' and 'series' are supported) : "]
		        			}
		        	},
	        		{
	        			"srcXpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map",
	        			"newNodeName":"publication-scale",
	        			"subMappings":{
	        				"mappings": [
		      	  				{
		      			  			"srcXpath":"gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer",
		      			  			"targetNode":"scale"
		      			  		},
		      	  				{
		      			  			"srcXpath":"gmd:MD_Resolution/gmd:distance/gco:Distance[@uom='meter' or @uom='m']",
		      			  			"targetNode":"resolution-ground"
		      			  		},
		      	  				{
		      			  			"srcXpath":"gmd:MD_Resolution/gmd:distance/gco:Distance[@uom='dpi']",
		      			  			"targetNode":"resolution-scan"
		      			  		}
	      			  		]
		      			}
		        	},
                    {   
                        "srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_CompletenessOmission/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record",
                        "targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/degree-of-record"
                    },
                    {
                        "srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record",
                        "targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-pos-accuracy"
                    },
		        	{	
	        			"srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/method-of-production"
	        		},
	        		{	
	        		    "srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString",
	        		    "targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/data-basis-text",
        		        "concatEntriesWith":"; "
	        		},
	        		{	
	        			"srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/technical-base"
	        		},
	        		{
	        			"srcXpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map",
	        			"newNodeName":"spatial-representation-type",
	        			"subMappings":{
	        				"mappings": [
		      	  				{
		      			  			"srcXpath":"gmd:MD_SpatialRepresentationTypeCode/@codeListValue",
		      			  			"targetNode":"",
		      			  			"targetAttribute":"iso-code",
		      			  			"transform":{
		          						"funct":transformISOToIgcDomainId,
		          						"params":[526, "Could not transform spatial representation type: "]
		          					}
		      			  		}
		      			  	]
		      			}
		        	},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/*/gmd:numberOfDimensions/gco:Integer",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-num-dimensions"
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/*/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-axis-name",
						"transform":{
							"funct":transformISOToIgcDomainId,
							"params":[514, "Could not transform dimension name in axisDimensionProperties: "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/*/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionSize/gco:Integer",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-axis-size"
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/*/gmd:cellGeometry/gmd:MD_CellGeometryCode/@codeListValue",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-cell-geometry",
						"transform":{
							"funct":transformISOToIgcDomainId,
							"params":[509, "Could not transform geometric object type code: "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/*/gmd:transformationParameterAvailability/gco:Boolean",
						"defaultValue": "N",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-transform-param",
						"transform":{
							"funct":transformGeneric,
							"params":[{"true":"Y", "false":"N"}, false, "Could not map transformationParameterAvailability "]
						}
					},
					{
						"execute":{
							"funct": determineGridSpatialRepresentationConcreteType
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:checkPointAvailability/gco:Boolean",
						"defaultValue": "N",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-rect-checkpoint",
						"transform":{
							"funct":transformGeneric,
							"params":[{"true":"Y", "false":"N"}, false, "Could not map controlPointAvailability "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:checkPointDescription/gco:CharacterString",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-rect-description"
					},
					// TODO review import/export of corner point coordinates
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:cornerPoints/gml:Point/gml:coordinates",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-rect-corner-point"
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:pointInPixel/gmd:MD_PixelOrientationCode",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-rect-point-in-pixel",
						"defaultValue": "1",
						"transform":{
							"funct":transformISOToIgcDomainId,
							"params":[2100, "Could not transform point in pixel code: "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georeferenceable/gmd:controlPointAvailability/gco:Boolean",
						"defaultValue": "N",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-ref-control-point",
						"transform":{
							"funct":transformGeneric,
							"params":[{"true":"Y", "false":"N"}, false, "Could not map controlPointAvailability "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georeferenceable/gmd:orientationParameterAvailability/gco:Boolean",
						"defaultValue": "N",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-ref-orientation-param",
						"transform":{
							"funct":transformGeneric,
							"params":[{"true":"Y", "false":"N"}, false, "Could not map controlPointAvailability "]
						}
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_Georeferenceable/gmd:georeferencedParameters/gco:Record",
						"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-ref-referenced-param"
					},
					{
						"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:topologyLevel/gmd:MD_TopologyLevelCode/@codeListValue",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/vector-format/vector-topology-level",
	        			"targetAttribute":"iso-code",
	        			"transform":{
		      				"funct":transformISOToIgcDomainId,
		      				"params":[528, "Could not transform vector topology level: "]
	      				}
	        		},
	        		{
	        			"srcXpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:geometricObjects",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map/vector-format",
	        			"newNodeName":"geo-vector",
	        			"subMappings":{
	        				"mappings": [
		      	  				{
		      			  			"srcXpath":"gmd:MD_GeometricObjects/gmd:geometricObjectType/gmd:MD_GeometricObjectTypeCode/@codeListValue",
		      			  			"targetNode":"geometric-object-type",
		      			  			"targetAttribute":"iso-code",
		      			  			"transform":{
		      							"funct":transformISOToIgcDomainId,
		      							"params":[515, "Could not transform geometric object type code: "]
		      						}
		      			  		},
		      	  				{
		      			  			"srcXpath":"gmd:MD_GeometricObjects/gmd:geometricObjectCount/gco:Integer",
		      			  			"targetNode":"geometric-object-count"
		      			  		}
	      			  		]
	      				}
	        		},
	        		{
	        			"srcXpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/map",
	        			"newNodeName":"feature-type",
	        			"subMappings":{
	        				"mappings": [
		      	  				{
		      			  			"srcXpath":"gco:LocalName",
		      			  			"targetNode":""
		      			  		}
	      	  				]
	      				}
	        		},
	        		{
	        			"execute":{
	        				"funct":mapRSIdentifier
	        			}
	        		},
                    {
                        "execute":{
                            "funct":mapMDIdentifier
                        }
                    }
  			    ]
  			}
  		},

  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/technical-domain/service
  		//
  		// ****************************************************
  		{
  			"srcXpath":"/",
  			"targetNode":"/",
  			"conditional": {
  				"storedValue": {
  					"name":"objectClass",
  					"value":"3"
  				}
  			},
  			"subMappings": {
  			    "mappings": [
	         		{	
	        			"srcXpath":"//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/service/service-type",
	        			"targetAttribute":"id",
	        			"storeValue":"serviceType",
		      			"transform":{
		      				"funct":transformGeneric,
		      				"params":[{"discovery":"1", "view":"2", "download":"3", "transformation":"4", "invoke":"5", "other":"6"}, false, "Could not map serviceType : "]
		        		}
		        	},
		        	{
		        		"execute":{
		                	"funct":mapServiceClassifications
		            	}
		        	},
		        	{	
	        			"srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString",
	        			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/service/database-of-system"
	        		},
	         		{	
		      			"srcXpath":"//gmd:identificationInfo//srv:serviceTypeVersion/gco:CharacterString",
		      			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/service",
		      			"newNodeName":"service-version",
		      			"subMappings":{
		      				"mappings": [
		    	  				{
		    			  			"srcXpath":".",
		    			  			"targetNode":""
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"1" // CSW
			    		  				}
		    			  			},
		    			  			"srcXpath":".",
		    			  			"targetNode":"",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5151, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"2" // WMS
			    		  				}
		    			  			},
		    			  			"srcXpath":".",
		    			  			"targetNode":"",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5152, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"3" // WFS
			    		  				}
		    			  			},
		    			  			"srcXpath":".",
		    			  			"targetNode":"",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5153, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"4" // WCTS
			    		  				}
		    			  			},
		    			  			"srcXpath":".",
		    			  			"targetNode":"",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5154, ""]
		        					}
		    			  		}
		    			  	]
		    			}
		        	},
	         		{	
		      			"srcXpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata",
		      			"targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/service",
		      			"newNodeName":"service-operation",
		      			"subMappings":{
		      				"mappings": [
		    	  				{
		    			  			"srcXpath":"srv:operationName/gco:CharacterString",
		    			  			"targetNode":"operation-name"
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"1" // CSW
			    		  				}
		    			  			},
		    			  			"srcXpath":"srv:operationName/gco:CharacterString",
		    			  			"targetNode":"operation-name",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5105, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"2" // WMS
			    		  				}
		    			  			},
		    			  			"srcXpath":"srv:operationName/gco:CharacterString",
		    			  			"targetNode":"operation-name",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5110, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"3" // WFS
			    		  				}
		    			  			},
		    			  			"srcXpath":"srv:operationName/gco:CharacterString",
		    			  			"targetNode":"operation-name",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5120, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"conditional": {
			    		  				"storedValue": {
			    		  					"name":"serviceType",
			    		  					"value":"4" // WCTS
			    		  				}
		    			  			},
		    			  			"srcXpath":"srv:operationName/gco:CharacterString",
		    			  			"targetNode":"operation-name",
		    			  			"targetAttribute":"id",
		    			  			"transform":{
		        						"funct":transformToIgcDomainId,
		        						"params":[5130, ""]
		        					}
		    			  		},
		    	  				{
		    			  			"srcXpath":"srv:operationDescription/gco:CharacterString",
		    			  			"targetNode":"description-of-operation"
		    			  		},
		    	  				{
		    			  			"srcXpath":"srv:invocationName/gco:CharacterString",
		    			  			"targetNode":"invocation-name"
		    			  		},
		    	  				{
		    			  			"srcXpath":"srv:DCP/srv:DCPList",
		    			  			"targetNode":"",
	    			      			"newNodeName":"platform",
	    			      			"subMappings":{
					      				"mappings": [
					    	  				{
					    			  			"srcXpath":"./@codeListValue",
					    			  			"targetNode":""
					    			  		}
					    	  			]
		    			  			}	
		    			  		},
		    	  				{
		    			  			"srcXpath":"srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
		    			  			"targetNode":"",
	    			      			"newNodeName":"connection-point",
	    			      			"subMappings":{
					      				"mappings": [
					    	  				{
					    			  			"srcXpath":".",
					    			  			"targetNode":""
					    			  		}
					    	  			]
		    			  			}	
		    			  		},
		    	         		{	
		    		      			"srcXpath":"srv:parameters/srv:SV_Parameter",
		    		      			"targetNode":"",
		    		      			"newNodeName":"parameter-of-operation",
		    		      			"subMappings":{
		    		      				"mappings": [
		    		    	  				{
		    		    			  			"srcXpath":"srv:name/gmd:aName/gco:CharacterString",
		    		    			  			"targetNode":"name"
		    		    			  		},
		    		    	  				{
		    		    			  			"srcXpath":"srv:optionality/gco:CharacterString",
		    		    			  			"targetNode":"optional",
		    		    			  			"defaultValue":"1",
		    		    		      			"transform":{
			    				      				"funct":transformGeneric,
			    				      				"params":[{"optional":"1", "mandatory":"0", "1":"1", "0":"0"}, false, "Could not map srv:optionality : "]
			    				        		}
		    		    			  		},
		    		    	  				{
		    		    			  			"srcXpath":"srv:repeatability/gco:Boolean",
		    		    			  			"targetNode":"repeatability",
		    		    			  			"defaultValue":"0",
		    		    		      			"transform":{
			    				      				"funct":transformGeneric,
			    				      				"params":[{"true":"1", "false":"0"}, false, "Could not map srv:repeatability : "]
			    				        		}
		    		    			  		},
		    		    	  				{
		    		    			  			"srcXpath":"srv:direction/srv:SV_ParameterDirection",
		    		    			  			"targetNode":"direction",
		    		    			  			"defaultValue":"Ein -und Ausgabe",
		    		    		      			"transform":{
			    				      				"funct":transformGeneric,
			    				      				"params":[{"in/out":"Ein -und Ausgabe", "in":"Eingabe", "out":"Ausgabe"}, false, "Could not map srv:direction : "]
			    				        		}
		    		    			  		},
		    		    	  				{
		    		    			  			"srcXpath":"srv:description/gco:CharacterString",
		    		    			  			"targetNode":"description-of-parameter"
		    		    			  		}
		    		    			   ] // service operation parameter submappings
		    			  		   }
		    	         		}
		    			  	] // service operation submappings
		    			}		
		        	},
		        	{    
		                "srcXpath":"//gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType/@codeListValue",
		                "targetNode":"/igc/data-sources/data-source/data-source-instance/technical-domain/service/coupling-type"
		        	},
		        	{    
		        	    "srcXpath":"//gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceSpecificUsage/gmd:MD_Usage/gmd:specificUsage/gco:CharacterString",
		        	    "targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information/dataset-usage"
		        	}
  			    ] // conditional submappings
  			}
  		},


  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/additional-information
  		//
  		// ****************************************************
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:language/gmd:LanguageCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information",
  			"newNodeName":"data-language",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":".",
			  			"targetNode":".",
			  			"targetAttribute":"id",
					  	"transform":{
							"funct":transformISOToIGCLanguageCode
						}
			  		},
	  				{
			  			"srcXpath":".",
			  			"targetNode":".",
				  		"transform":{
							"funct":transformISOToLanguage,
							"params":['de']
						}
			  		}
			  	]
			}
  		},
  		{
  			"srcXpath":"//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information",
  			"newNodeName":"metadata-language",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":".",
			  			"targetNode":".",
			  			"targetAttribute":"id",
					  	"transform":{
							"funct":transformISOToIGCLanguageCode
						}
			  		},
	  				{
			  			"srcXpath":".",
			  			"targetNode":".",
				  		"transform":{
							"funct":transformISOToLanguage,
							"params":['de']
						}
			  		}
			  	]
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:purpose/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information/dataset-intentions"
  		},
        {   
            "execute":{
                "funct":mapAccessConstraints
            }
        },
        {   
            "execute":{
                "funct":mapUseLimitation
            }
  		},
        {   
            "execute":{
                "funct":mapUseConstraints
            }
        },
  		{
  			"srcXpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:offLine/gmd:MD_Medium",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information",
  			"newNodeName":"medium-option",
  			"subMappings":{
  				"mappings": [
					{
						"srcXpath":"gmd:mediumNote/gco:CharacterString",
						"targetNode":"medium-note"
					},
	  				{
			  			"srcXpath":"gmd:name/gmd:MD_MediumNameCode/@codeListValue",
			  			"targetNode":"medium-name",
			  			"targetAttribute":"iso-code",
			  			"transform":{
							"funct":transformISOToIgcDomainId,
							"params":[520, "Could not transform medium name code: "]
						}
			  		},
	  				{
			  			"srcXpath":"../../gmd:transferSize/gco:Real",
			  			"targetNode":"transfer-size",
			  			"transform":{
							"funct":transformNumberStrToIGCNumber
						}
			  		}
			  	]
			}
  		},
  		{
  			"srcXpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format[gmd:name[not(@gco:nilReason)]]",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information",
  			"newNodeName":"data-format",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:name/gco:CharacterString",
			  			"targetNode":"format-name"
			  		},
	  				{
			  			"srcXpath":"gmd:name/gco:CharacterString",
			  			"targetNode":"format-name",
			  			"targetAttribute":"id",
			  			"defaultValue":"-1",
			  			"transform":{
							"funct":transformToIgcDomainId,
							"params":[1320, ""]
						}
			  		},
	  				{
			  			"srcXpath":"gmd:version/gco:CharacterString",
			  			"targetNode":"version"
			  		},
	  				{
			  			"srcXpath":"gmd:fileDecompressionTechnique/gco:CharacterString",
			  			"targetNode":"file-decompression-technique"
			  		},
	  				{
			  			"srcXpath":"gmd:specification/gco:CharacterString",
			  			"targetNode":"specification"
			  		}
			  	]
			}
  		},
  		{	
  			"defaultValue":"1",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information/publication-condition"
  		},
  		{	
  			"srcXpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information/ordering-instructions"
  		},
  		{
  			"srcXpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/additional-information",
  			"newNodeName":"conformity",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString",
			  			"targetNode":"conformity-is-inspire",
			  			"transform":{
							"funct":conformityIsInspire,
							"params":["", true]
						}
			  		},
	  				{
			  			"srcXpath":"gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString",
			  			"targetNode":"conformity-specification"
			  		},
	  				{
			  			"srcXpath":"gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString",
			  			"targetNode":"conformity-specification",
			  			"targetAttribute":"id",
			  			"defaultValue":"-1",
			  			"transform":{
							"funct":transformConformityToIgcDomainId,
							"params":["", true]
						}
			  		},
	  				{
			  			"srcXpath":"gmd:pass/gco:Boolean",
			  			"targetNode":"conformity-degree",
			  			"targetAttribute":"id",
			  			"transform":{
							"funct":transformGeneric,
							"params":[{"true":"1", "false":"2"}, false]
						}
			  		},
                    {
                        "srcXpath":"gmd:pass/@gco:nilReason",
                        "targetNode":"conformity-degree",
                        "targetAttribute":"id",
                        "transform":{
                            "funct":transformToStaticValue,
                            "params":["3"]
                        }
                    },
	  				{
			  			"srcXpath":"gmd:pass/gco:Boolean",
			  			"targetNode":"conformity-degree",
			  			"transform":{
							"funct":transformGeneric,
							"params":[{"true":"konform", "false":"nicht konform"}, false]
						}
			  		},
                    {
                        "srcXpath":"gmd:pass/@gco:nilReason",
                        "targetNode":"conformity-degree",
                        "transform":{
                            "funct":transformToStaticValue,
                            "params":["nicht evaluiert"]
                        }
                    },
	  				{
			  			"srcXpath":"gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
						"targetNode":"conformity-publication-date",
						"transform":{
							"funct":transformDateIso8601ToIndex
						}
			  		}
			  	]
			}
  		},

  		
  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/spatial-domain
  		//
  		// ****************************************************
        {
            "execute":{
                "funct":mapReferenceSystemInfo
            }
        },
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-minimum",
  			"transform":{
				"funct":transformNumberStrToIGCNumber
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-maximum",
  			"transform":{
				"funct":transformNumberStrToIGCNumber
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gmd:VerticalCRS/gmd:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@uom",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-unit",
  			"targetAttribute":"id",
  			"transform":{
				"funct":transformToIgcDomainId,
				"params":[102, "", "Could not map vertical-extent unit:"]
			}						    					
  		},
  		{	// same as rule before, but different path
  		    "srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gml:VerticalCRS/gml:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@gml:uom",
  		    "targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-unit",
  		    "targetAttribute":"id",
  		    "transform":{
  		        "funct":transformToIgcDomainId,
  		        "params":[102, "", "Could not map vertical-extent unit:"]
  		    }						    					
  		},
        {
            "execute":{
                "funct":mapVerticalExtentVdatum
            }
        },
        {
            "srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:description/gco:CharacterString",
            "targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain/description-of-spatial-domain"
        },
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:geographicElement",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain",
  			"newNodeName":"geo-location",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:EX_GeographicDescription/gmd:geographicIdentifier/*/gmd:code/gco:CharacterString",
	  					"defaultValue":"Raumbezug des Datensatzes",
			  			"targetNode":"uncontrolled-location/location-name"
			  		},
	  				{
			  			"defaultValue":"-1",
			  			"targetNode":"uncontrolled-location/location-name",
			  			"targetAttribute":"id"
			  		},
	  				{
			  			"srcXpath":"gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal",
			  			"targetNode":"bounding-coordinates/west-bounding-coordinate",
			  			"transform":{
							"funct":transformNumberStrToIGCNumber
						}
			  		},
	  				{
			  			"srcXpath":"gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal",
			  			"targetNode":"bounding-coordinates/east-bounding-coordinate",
			  			"transform":{
							"funct":transformNumberStrToIGCNumber
						}
			  		},
	  				{
			  			"srcXpath":"gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal",
			  			"targetNode":"bounding-coordinates/north-bounding-coordinate",
			  			"transform":{
							"funct":transformNumberStrToIGCNumber
						}
			  		},
	  				{
			  			"srcXpath":"gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal",
			  			"targetNode":"bounding-coordinates/south-bounding-coordinate",
			  			"transform":{
							"funct":transformNumberStrToIGCNumber
						}
			  		}
			  	]
			}
  		},
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place']/gmd:keyword/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/spatial-domain",
  			"newNodeName":"geo-location",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":".",
			  			"targetNode":"uncontrolled-location/location-name"
			  		},
	  				{
			  			"defaultValue":"-1",
			  			"targetNode":"uncontrolled-location/location-name",
			  			"targetAttribute":"id"
			  		}
			  	]
			}
  		},  	

  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/temporal-domain
  		//
  		// ****************************************************
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain/description-of-temporal-domain"
  		},
  		{	
  			"execute":{
				"funct":mapTimeConstraints
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain/time-period",
  			"targetAttribute":"iso-code",
  			"transform":{
				"funct":transformISOToIgcDomainId,
				"params":[518, "Could not map time-period to ISO code: "]
			},
			"targetContentHandling":"replace"
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:status/gmd:MD_ProgressCode/@codeListValue",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain/time-status",
  			"targetAttribute":"iso-code",
  			"transform":{
				"funct":transformISOToIgcDomainId,
				"params":[523, "Could not map time-status to ISO code: "]
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gmd:TM_PeriodDuration",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain/time-step",
  			"transform":{
				"funct":new TM_PeriodDurationToTimeInterval().parse
			}
  		},
  		{	
  			"srcXpath":"//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gmd:TM_PeriodDuration",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain/time-step",
  			"transform":{
				"funct":new TM_PeriodDurationToTimeAlle().parse
			}
  		},
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/temporal-domain",
  			"newNodeName":"dataset-reference",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:date/gco:Date[not(../gco:DateTime)] | gmd:date/gco:DateTime",
			  			"targetNode":"dataset-reference-date",
			  			"transform":{
							"funct":transformDateIso8601ToIndex
						}
			  		},
	  				{
			  			"srcXpath":"gmd:dateType/gmd:CI_DateTypeCode/@codeListValue",
			  			"targetNode":"dataset-reference-type",
			  			"targetAttribute":"iso-code",
			  			"transform":{
    						"funct":transformISOToIgcDomainId,
    						"params":[502, "Could not map date role: "]
    					}
			  		}
			  	]
			}
  		},
  		
  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/subject-terms
  		//
  		// ****************************************************
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='GEMET - INSPIRE themes, version 1.0']/gmd:keyword/gco:CharacterString",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance/subject-terms",
  			"newNodeName":"controlled-term",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":".",
			  			"targetNode":""
			  		},
	  				{
			  			"srcXpath":".",
			  			"targetNode":"",
			  			"targetAttribute":"id",
			  			"transform":{
							"funct":transformToIgcDomainId,
							// PASS "" as language to check all localized values !!!
							"params":[6100, "", "Could not map INSPIRE theme:", true]
						}
			  		},
	  				{
			  			"defaultValue":"INSPIRE",
			  			"targetNode":"",
			  			"targetAttribute":"source"
			  		}
			  	]
			}
  		},
  		{
  		    "srcXpath":"//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='German Environmental Classification - Topic, version 1.0']/gmd:keyword/gco:CharacterString",
  		    "targetNode":"/igc/data-sources/data-source/data-source-instance/subject-terms",
  		    "newNodeName":"controlled-term",
  		    "subMappings":{
  		        "mappings": [
  		                     {
  		                         "srcXpath":".",
  		                         "targetNode":""
  		                     },
  		                     {
  		                         "srcXpath":".",
  		                         "targetNode":"",
  		                         "targetAttribute":"id",
  		                         "transform":{
  		                             "funct":transformToIgcDomainId,
  		                             // PASS "" as language to check all localized values !!!
  		                             "params":[1410, "", "Could not map Topic:", true]
  		                         }
  		                     },
  		                     {
  		                         "defaultValue":"Topic",
  		                         "targetNode":"",
  		                         "targetAttribute":"source"
  		                     }
  		                     ]
  		    }		
  		},
  		{
  		    "srcXpath":"//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='OGDD-Kategorien']/gmd:keyword/gco:CharacterString",
  		    "targetNode":"/igc/data-sources/data-source/data-source-instance/subject-terms",
  		    "newNodeName":"controlled-term",
  		    "subMappings":{
  		        "mappings": [
  		                     {
  		                         "srcXpath":".",
  		                         "targetNode":""
  		                     },
  		                     {
  		                         "srcXpath":".",
  		                         "targetNode":"",
  		                         "targetAttribute":"id",
  		                         "transform":{
  		                             "funct":transformToIgcDomainId,
  		                             // PASS "" as language to check all localized values !!!
  		                             "params":[6400, "", "Could not map OpenData Category:", true]
  		                         }
  		                     },
  		                     {
  		                         "defaultValue":"OpenData",
  		                         "targetNode":"",
  		                         "targetAttribute":"source"
  		                     }
  		                     ]
  		    }		
  		},
        {
            "execute":{
                "funct":mapUncontrolledTerms
            }
        },

  		// ****************************************************
  		//
  		// /igc/data-sources/data-source/data-source-instance/available-linkage
  		//
  		// ****************************************************

        {   
            "execute":{
                "funct":mapDistributionLinkages
            }
        },
  		{
  			"srcXpath":"//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic[gmd:fileName/gco:CharacterString != '']",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance",
  			"newNodeName":"available-linkage",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"srcXpath":"gmd:fileDescription/gco:CharacterString",
			  			"defaultValue":"grafische Darstellung",
			  			"targetNode":"linkage-description"
			  		},
	  				{
			  			"srcXpath":"gmd:fileName/gco:CharacterString",
			  			"targetNode":"linkage-url"
			  		},
	  				{
			  			"defaultValue":"1",
			  			"targetNode":"linkage-url-type"
			  		},
	  				{
			  			"defaultValue":"9000",
			  			"targetNode":"linkage-reference",
			  			"targetAttribute":"id"
          			}
			  	]
			}
  		},
  		{
  			"srcXpath":"//gmd:identificationInfo//srv:operatesOn",
  			"targetNode":"/igc/data-sources/data-source/data-source-instance",
  			"newNodeName":"available-linkage",
  			"subMappings":{
  				"mappings": [
	  				{
			  			"defaultValue":"operates on",
			  			"targetNode":"linkage-name"
			  		},
	  				{
			  			"srcXpath":"./@xlink:href",
			  			"defaultValue":"invalid link",
			  			"targetNode":"linkage-url"
			  		},
	  				{
			  			"defaultValue":"1",
			  			"targetNode":"linkage-url-type"
			  		},
	  				{
			  			"defaultValue":"-1",
			  			"targetNode":"linkage-reference",
			  			"targetAttribute":"id"
			  		}
			  	]
			}
  		},

  		// ****************************************************
  		//
  		// /igc/addresses
  		//
  		// ****************************************************
  		
  		{	
  			"execute":{
				"funct":mapAddresses
			}
  		}
	]};

if (log.isDebugEnabled()) {
	log.debug("mapping CSW 2.0.2 AP ISO 1.0 document to IGC import document.");
}
//protocol(INFO, "Start transformation of: " + protocolHandler.getCurrentFilename());

log.debug("validate source");
validateSource(source);


var uuid = XPATH.getString(source, "//gmd:fileIdentifier/gco:CharacterString");
if (hasValue(uuid)) {
	protocol(INFO, "fileIdentifier: " + uuid);
}

var title = XPATH.getString(source, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
if (hasValue(title)) {
	protocol(INFO, "title: " + title);
}

var storedValues = new Object();


log.debug("map to target");
mapToTarget(mappingDescription, source, target.getDocumentElement());

function mapToTarget(mapping, source, target) {
		
		// iterate over all mapping descriptions
		for (var i in mapping.mappings) {
			var m = mapping.mappings[i];
			// check for conditional mapping
			if (m.conditional) {
				if (m.conditional.storedValue) {
					log.debug("found mapping with stored value conditional: " + m.conditional.storedValue.name + " ? " + storedValues[m.conditional.storedValue.name] + " == " + m.conditional.storedValue.value);
					if (storedValues[m.conditional.storedValue.name] != m.conditional.storedValue.value) {
						log.debug("Skip mapping because: " + m.conditional.storedValue.name + "!=" + m.conditional.storedValue.value);
						continue;
					} else {
						log.debug("Execute mapping because: " + m.conditional.storedValue.name + "==" + m.conditional.storedValue.value);
					}
				}
			}
			
			// check for execution (special function)
			if (hasValue(m.execute)) {
				log.debug("Execute function: " + m.execute.funct.name + "...")
				var args = new Array(source, target);
				if (hasValue(m.execute.params)) {
					args = args.concat(m.execute.params);
				}
				call_f(m.execute.funct, args)
			} else if (m.subMappings) {
				if (m.srcXpath) {
					// iterate over all xpath results
					var sourceNodeList = XPATH.getNodeList(source, m.srcXpath);
					if (sourceNodeList) {
						log.debug("found sub mapping sources: " + m.srcXpath + "; count: " + sourceNodeList.getLength())
						for (var j=0; j<sourceNodeList.getLength(); j++ ) {
							log.debug("handle sub mapping: " + sourceNodeList.item(j))
							var node = XPATH.createElementFromXPath(target, m.targetNode);
							if (m.newNodeName) {
								node = node.appendChild(node.getOwnerDocument().createElement(m.newNodeName));
							}
							mapToTarget(m.subMappings, sourceNodeList.item(j), node);
						}
					} else {
						log.debug("found sub mapping sources: " + m.srcXpath + "; count: 0")
					}
				}	
			} else {
				if (m.srcXpath) {
					log.debug("Working on " + m.targetNode + " with xpath:'" + m.srcXpath + "'")
					// iterate over all xpath results
					var sourceNodeList = XPATH.getNodeList(source, m.srcXpath);
					var nodeText = "";
					if (sourceNodeList && sourceNodeList.getLength() > 0) {
						for (var j=0; j<sourceNodeList.getLength(); j++ ) {
							var value = sourceNodeList.item(j).getTextContent()
							log.debug("Found value: '" + value + "' hasValue:" + hasValue(value));
							if (hasValue(value)) {
								// trim
								value = value.trim();
							}
							
							// check for transformation
							if (hasValue(m.transform)) {
								log.debug("Transform value '" + value + "'")
								var args = new Array(value);
								if (hasValue(m.transform.params)) {
									args = args.concat(m.transform.params);
								}
								value = call_f(m.transform.funct,args);
							}
							if (m.defaultValue && !hasValue(value)) {
								log.debug("typeof m.defaultValue:" + typeof m.defaultValue);
								if (typeof m.defaultValue == "function" ) {
									log.debug("Call function with value:" +source);
									var args = new Array(source);
									value = call_f(m.defaultValue,args);
								} else {
									value = m.defaultValue;
								}
								log.debug("Setting value to default '" + m.defaultValue + "'")
								value = m.defaultValue;
							}
							if (hasValue(value)) {
								var node = XPATH.createElementFromXPath(target, m.targetNode);
								log.debug("Found node with content: '" + node.getTextContent() + "'")
								if (j==0) { 
									// append content to target nodes content?
									if (m.appendWith && hasValue(node.getTextContent())) {
										log.debug("Append to target node...")
										nodeText = node.getTextContent() + m.appendWith;
									}
									// is a prefix has been defined with the xpath? 
									if (m.prefix) {
										log.debug("Append prefix...")
										nodeText += m.prefix;
									}
								} else {
									// concat multiple entries?
									if (m.concatEntriesWith) {
										log.debug("concat entries... ")
										nodeText += m.concatEntriesWith;
									}
								}
								
								if (m.targetContentHandling && m.targetContentHandling == "replace") {
									log.debug("replace target content '" + nodeText + "' with new content '" + value + "'");
									nodeText = value;
								} else {
									nodeText += value;
								}
								
								if (m.storeValue) {
									log.debug("stored '" + value + "' as '" + m.storeValue + "' in store:" + storedValues + ".");
									storedValues[""+m.storeValue] = value;
								}
								
								if (m.targetAttribute) {
									log.debug("adding '" + m.targetNode + "/@" + m.targetAttribute + "' = '" + nodeText + "'.");
									XMLUtils.createOrReplaceAttribute(node, m.targetAttribute, nodeText);
								} else {
									log.debug("adding '" + m.targetNode + "' = '" + nodeText + "'.");
									XMLUtils.createOrReplaceTextNode(node, nodeText);
								}
							}
						}
					} else {
						// nothing found in srcPath, check for default values
						if (m.defaultValue) {
							var value;
							if (typeof m.defaultValue == "function" ) {
								var args = new Array(source);
								value = call_f(m.defaultValue,args);
							} else {
								value = m.defaultValue;
							}
							if (hasValue(value)) {
								var node = XPATH.createElementFromXPath(target, m.targetNode);
								log.debug("Found node with content: '" + node.getTextContent() + "'")
								if (j==0) { 
									// append content to target nodes content?
									if (m.appendWith && hasValue(node.getTextContent())) {
										log.debug("Append to target node...")
										nodeText = node.getTextContent() + m.appendWith;
									}
									// is a prefix has been defined with the xpath? 
									if (m.prefix) {
										log.debug("Append prefix...")
										nodeText += m.prefix;
									}
								} else {
									// concat multiple entries?
									if (m.concatEntriesWith) {
										log.debug("concat entries... ")
										nodeText += m.concatEntriesWith;
									}
								}
								
								nodeText += value;
								
								if (m.storeValue) {
									log.debug("stored '" + value + "' as '" + m.storeValue + "'.");
									storedValues[m.storeValue] = value;
								}
								
								if (m.targetAttribute) {
									log.debug("adding '" + m.targetNode + "/@" + m.targetAttribute + "' = '" + nodeText + "'.");
									XMLUtils.createOrReplaceAttribute(node, m.targetAttribute, nodeText);
								} else {
									log.debug("adding '" + m.targetNode + "' = '" + nodeText + "'.");
									XMLUtils.createOrReplaceTextNode(node, nodeText);
								}
							}
						}	
					}
				// check if a default value was supplied
				// -> set a target node to a default value
				} else if (m.defaultValue || m.setStoredValue) {
					var nodeText = "";
					var value;
					log.debug("typeof m.defaultValue:" + typeof m.defaultValue)
					if (m.setStoredValue) {
						log.debug("get value '" + value + "' from stored value '" + m.setStoredValue + "'.");
						value = storedValues[m.setStoredValue];
					} else if (typeof m.defaultValue == "function" ) {
						var args = new Array(source);
						value = call_f(m.defaultValue,args);
					} else {
						value = m.defaultValue;
					}
					var node = XPATH.createElementFromXPath(target, m.targetNode);
					// check for transformation
					if (hasValue(m.transform)) {
						var args = new Array(value);
						if (hasValue(m.transform.params)) {
							args = args.concat(m.transform.params);
						}
						value = call_f(m.transform.funct,args);
					}

					nodeText += value;
					
					if (m.storeValue) {
						log.debug("stored '" + value + "' as '" + m.storeValue + "'.");
						storedValues[m.storeValue] = value;
					}
					
					if (m.targetAttribute) {
						log.debug("adding '" + m.targetNode + "/@" + m.targetAttribute + "' = '" + nodeText + "'.");
						XMLUtils.createOrReplaceAttribute(node, m.targetAttribute, nodeText);
					} else {
						log.debug("adding '" + m.targetNode + "' = '" + nodeText + "'.");
						XMLUtils.createOrReplaceTextNode(node, nodeText);
					}
				}
			}
		}
		
		return target;
}



function getObjectClassFromHierarchyLevel(val) {
	// default to "Geo-Information / Karte"
	var result = "1"; 
	if (hasValue(val) && val.toLowerCase() == "service") {
		// "Dienst / Anwendung / Informationssystem"
		result = "3";
	}
	return result;
}



function validateSource(source) {
	// pre check source if required
	var metadataNodes = XPATH.getNodeList(source, "//gmd:MD_Metadata");
	if (!hasValue(metadataNodes) || metadataNodes.getLength() == 0) {
		log.error("No valid ISO metadata record.");
		protocol(ERROR, "No valid ISO metadata record.");
		throw "No valid ISO metadata record.";
	}
	var hierarchyLevel = XPATH.getString(source, "//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
	log.debug("Found hierarchyLevel: " + hierarchyLevel);
	if (hierarchyLevel == "application") {
		log.error("HierarchyLevel 'application' is not supported.");
		protocol(ERROR, "HierarchyLevel 'application' is not supported.");
		throw "HierarchyLevel 'application' is not supported.";
	}
	return true;
}

function mapReferenceSystemInfo(source, target) {
	var rsIdentifiers = XPATH.getNodeList(source, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
	if (hasValue(rsIdentifiers)) {
		for (i=0; i<rsIdentifiers.getLength(); i++ ) {
			var code = XPATH.getString(rsIdentifiers.item(i), "gmd:code/gco:CharacterString");

			// If gco:CharacterString is empty, then also try gmx:Anchor
			if (!hasValue(code)) {
				code = XPATH.getString(rsIdentifiers.item(i), "gmd:code/gmx:Anchor");
			}

			var codeSpace = XPATH.getString(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
			var coordinateSystem;
			if (hasValue(codeSpace) && hasValue(code)) {
				coordinateSystem = codeSpace+":"+code; 
			} else if (hasValue(code)) {
				coordinateSystem = code;
			}
			log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/spatial-domain/coordinate-system" + "' = '" + coordinateSystem + "' to target document.");
			var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/spatial-domain/coordinate-system");
			XMLUtils.createOrReplaceTextNode(node, code);
            
            // get syslist id
			var coordinateSystemId = transformToIgcDomainId(code, 100, "");
            if (hasValue(coordinateSystemId) && coordinateSystemId == -1) {
                // try to parse coordsystem name for correct mapping to syslist id
                var coordinateSystemLower = coordinateSystem.toLowerCase();
                var indx = coordinateSystemLower.indexOf("epsg:");
                if (indx != -1) {
                    var tmpCoordinateSystemId = coordinateSystemLower.substring(indx+5);
                    var tmpCoordinateSystem = transformToIgcDomainValue(tmpCoordinateSystemId, 100, "en");
                    if (hasValue(tmpCoordinateSystem)) {
                        XMLUtils.createOrReplaceTextNode(node, tmpCoordinateSystem);
                        coordinateSystemId = tmpCoordinateSystemId;
                    } else {
                        var myMsg = "Could not map coordinate-system to syslist 100, use as free entry: ";
                        log.warn(myMsg + coordinateSystem);
                        protocol(WARN, myMsg + coordinateSystem)
                    }
                }
            }
			if (hasValue(coordinateSystemId)) {
				XMLUtils.createOrReplaceAttribute(node, "id", coordinateSystemId);
			}
		}
	} else {
		protocol(INFO, "No referenceSystemInfo has been found!");
		log.debug("No referenceSystemInfo has been found!");
	}
}

function mapVerticalExtentVdatum(source, target) {
    var verticalDatums = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gml:VerticalCRS/gml:verticalDatum/gml:VerticalDatum");
    if (hasValue(verticalDatums)) {
        for (i=0; i<verticalDatums.getLength(); i++ ) {
            var vDatumName = XPATH.getString(verticalDatums.item(i), "gml:name");
            if (!hasValue(vDatumName)) {
            	vDatumName = XPATH.getString(verticalDatums.item(i), "gml:identifier");
            }
            if (hasValue(vDatumName)) {
	            log.debug("adding '/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-vdatum' = '" + vDatumName + "' to target document.");
	            var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/spatial-domain/vertical-extent/vertical-extent-vdatum");
	            XMLUtils.createOrReplaceTextNode(node, vDatumName);
	            var datumId = transformToIgcDomainId(vDatumName, 101, "", "Could not map VerticalDatum: ");
	            if (hasValue(datumId)) {
	                XMLUtils.createOrReplaceAttribute(node, "id", datumId);
	            }
            }
        }
    } else {
        protocol(INFO, "No VerticalDatum has been found!");
        log.debug("No VerticalDatum has been found!");
    }
}


function mapCommunicationData(source, target) {
	var email = XPATH.getString(source, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
	if (hasValue(email)) {
		var communication = target.appendChild(target.getOwnerDocument().createElement("communication"));
		var node = XPATH.createElementFromXPath(communication, "communication-medium");
		XMLUtils.createOrReplaceTextNode(node, "Email");
		XMLUtils.createOrReplaceAttribute(node, "id", "3");
		node = XPATH.createElementFromXPath(communication, "communication-value");
		XMLUtils.createOrReplaceTextNode(node, email.trim());
	}
	var phone = XPATH.getString(source, "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString");
	if (hasValue(phone)) {
		var communication = target.appendChild(target.getOwnerDocument().createElement("communication"));
		var node = XPATH.createElementFromXPath(communication, "communication-medium");
		XMLUtils.createOrReplaceTextNode(node, "Telefon");
		XMLUtils.createOrReplaceAttribute(node, "id", "1");
		node = XPATH.createElementFromXPath(communication, "communication-value");
		XMLUtils.createOrReplaceTextNode(node, phone.trim());
	}
	var fax = XPATH.getString(source, "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString");
	if (hasValue(fax)) {
		var communication = target.appendChild(target.getOwnerDocument().createElement("communication"));
		var node = XPATH.createElementFromXPath(communication, "communication-medium");
		XMLUtils.createOrReplaceTextNode(node, "Fax");
		XMLUtils.createOrReplaceAttribute(node, "id", "2");
		node = XPATH.createElementFromXPath(communication, "communication-value");
		XMLUtils.createOrReplaceTextNode(node, fax.trim());
	}
	var url = XPATH.getString(source, "gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
	if (hasValue(url)) {
		var communication = target.appendChild(target.getOwnerDocument().createElement("communication"));
		var node = XPATH.createElementFromXPath(communication, "communication-medium");
		XMLUtils.createOrReplaceTextNode(node, "URL");
		XMLUtils.createOrReplaceAttribute(node, "id", "4");
		node = XPATH.createElementFromXPath(communication, "communication-value");
		XMLUtils.createOrReplaceTextNode(node, url.trim());
	}
}


function mapTimeConstraints(source, target) {
	var timePeriods = XPATH.getNodeList(source, "//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod");
	log.debug("Found " + timePeriods.getLength() + " TimePeriod records.");
	if (hasValue(timePeriods) && timePeriods.getLength() > 0) {
		var beginPosition = XPATH.getString(timePeriods.item(0), "gml:beginPosition");
		var endPosition = XPATH.getString(timePeriods.item(0), "gml:endPosition");
		if (hasValue(beginPosition) && hasValue(endPosition)) {
			if (beginPosition.equals(endPosition)) {
				var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/beginning-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(beginPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/ending-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(endPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/time-type");
				XMLUtils.createOrReplaceTextNode(node, "am");
			} else {
				var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/beginning-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(beginPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/ending-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(endPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/time-type");
				XMLUtils.createOrReplaceTextNode(node, "von");
			}
		} else if (hasValue(beginPosition)) {
				var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/beginning-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(beginPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/time-type");
				XMLUtils.createOrReplaceTextNode(node, "seit");
		} else if (hasValue(endPosition)) {
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/ending-date");
				XMLUtils.createOrReplaceTextNode(node, transformDateIso8601ToIndex(endPosition));
				node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/temporal-domain/time-type");
				XMLUtils.createOrReplaceTextNode(node, "bis");
		}
	}
}

function mapRSIdentifier(source, target)  {
	log.debug("Map RS_Identifier.");
	var rsIdentifiers = XPATH.getNodeList(source, "//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier");
	if (hasValue(rsIdentifiers) && rsIdentifiers.getLength() > 0) {
		log.debug("Found " + rsIdentifiers.getLength() + " RS_Identifier records.");
		var codeSpace = XPATH.getString(rsIdentifiers.item(0), "gmd:codeSpace/gco:CharacterString");
		var code = XPATH.getString(rsIdentifiers.item(0), "gmd:code/gco:CharacterString");
		if (hasValue(code)) {
			log.debug("Found RS_Identifier: " + code);
			var dataSourceID = "";
			if (hasValue(codeSpace)) {
				dataSourceID = codeSpace + "/" + code;
			} else {
				dataSourceID = code;
			}
			var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/technical-domain/map/datasource-identificator");
			XMLUtils.createOrReplaceTextNode(node, dataSourceID);
		}
	}
}

function mapMDIdentifier(source, target)  {
    log.debug("Map MD_Identifier.");
    var mdIdentifiers = XPATH.getNodeList(source, "//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier");
    if (hasValue(mdIdentifiers) && mdIdentifiers.getLength() > 0) {
        log.debug("Found " + mdIdentifiers.getLength() + " MD_Identifier records.");
        var code = XPATH.getString(mdIdentifiers.item(0), "gmd:code/gco:CharacterString");
        if (hasValue(code)) {
            log.debug("Found MD_Identifier: " + code);
            var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/technical-domain/map/datasource-identificator");
            XMLUtils.createOrReplaceTextNode(node, code);
        }
    }
}

function mapUseLimitation(source, target) {
    var useLimitations = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:resourceConstraints/*/gmd:useLimitation");
    if (hasValue(useLimitations)) {
        for (i=0; i<useLimitations.getLength(); i++ ) {
            var useLimitation = XPATH.getString(useLimitations.item(i), "./gco:CharacterString");
            // filter "Nutzungsbedingungen:" ! These are the useConstraints ! see #384
            if (hasValue(useLimitation) && !useLimitation.startsWith("Nutzungsbedingungen:")) {
            	addUseLimitation(useLimitation, target);
            }
        }
    }
}
function addUseLimitation(useLimitation, target) {
    if (hasValue(useLimitation)) {
    	useLimitation = removeConstraintPraefix(useLimitation);
        log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/additional-information/use-limitation/terms-of-use" + "' = '" + useLimitation + "' to target document.");
        var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/additional-information/use-limitation");
        node = XPATH.createElementFromXPath(node, "terms-of-use");
        XMLUtils.createOrReplaceTextNode(node, useLimitation);
        XMLUtils.createOrReplaceAttribute(node, "id", "-1");
    }
}


function mapUseConstraints(source, target) {
	// check stuff under every MD_LegalConstraints having useConstraints !
    var legalConstraints = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints[descendant::gmd:useConstraints]");
    if (hasValue(legalConstraints)) {
        for (i=0; i<legalConstraints.getLength(); i++ ) {

        	// evaluate useConstraints -> MD_RestrictionCode@codeListValue

        	var hasOtherRestrictions = hasValue(XPATH.getNode(legalConstraints.item(i), "./gmd:useConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions']"));
        	var codeListValues = XPATH.getStringArray(legalConstraints.item(i), "./gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue");
            if (hasValue(codeListValues)) {
                for (j=0; j<codeListValues.length; j++ ) {
                	var isoValue = codeListValues[j];

                	// only add useConstraints "license" if no "otherRestrictions" !
                	// GDI-DE conformity always writes "license" AND "otherRestrictions", so if this combination is found, we only map "otherConstraints" (see below) !
                	// see #13, #704
                    if (isoValue == "license" && hasOtherRestrictions) {
                    	continue;
                    }

                    if (isoValue == "otherRestrictions") {
                    	continue;
                    }
                    addUseConstraint(isoValue, target, null);
                }
            }

            // evaluate otherConstraints
			//since issue 1443 the source note is attached to its constraint
            var otherConstraints = XPATH.getNodeList(legalConstraints.item(i), "./gmd:otherConstraints");
            if (hasValue(otherConstraints)) {
                for (j=0; j<otherConstraints.getLength(); j++ ) {
					var otherConstraint = XPATH.getString(otherConstraints.item(j), "./gco:CharacterString");

					if (!otherConstraint) {
                    	//constraint given with gmx:Anchor tag
                    	otherConstraint = XPATH.getString(otherConstraints.item(j), "./gmx:Anchor");
					}

					var thisElementIsSourceNote = isSourceNote(otherConstraint);
					var thisElementIsJson = isJsonString(otherConstraint);

                    if (thisElementIsJson) {
                    	// add license name and source from JSON
						addJsonUseConstraint(otherConstraint, target);
						continue;
                    }

                    if(thisElementIsSourceNote){
                    	continue;
					}

					var hasNextElement = j < otherConstraints.getLength()-1;
					if (hasNextElement) {
                    	//check next element
						var nextElem = XPATH.getString(otherConstraints.item(j+1), "./gco:CharacterString") ;
						var nextElementIsJson = isJsonString(nextElem) && hasValue(nextElem) ;
						var secondNextElem = null;
						var nextElementIsSourceNote = hasValue(nextElem) && isSourceNote(nextElem);

						var hasSecondNextElement = j < otherConstraints.getLength()-2;
						if (hasSecondNextElement) {
							//check second next element
							secondNextElem = XPATH.getString(otherConstraints.item(j+2), "./gco:CharacterString");
							var hasJsonAndNextElementIsSourceNote = hasValue(nextElem) && isSourceNote(nextElem) && isJsonString(secondNextElem) && hasValue(secondNextElem);
							var hasNotJsonAndNextElemIsSourceNote =  nextElementIsSourceNote && !isJsonString(secondNextElem) && hasValue(secondNextElem);

							if ( hasJsonAndNextElementIsSourceNote ){
								// skip, since JSON is handled explicitly
								continue;
							}

							if ( hasNotJsonAndNextElemIsSourceNote ){
								addUseConstraint(otherConstraint, target, nextElem);
							}
						}
						if(nextElementIsJson && !thisElementIsSourceNote){
							if(compareUseConstraintWithJson(otherConstraint, nextElem)){
								// skip
								continue;
							}
						}

						if (nextElementIsSourceNote && !hasValue(secondNextElem)){
							log.debug("Im supposed to be here");
							addUseConstraint(otherConstraint, target, nextElem);
							continue;
						}
					}
					addUseConstraint(otherConstraint, target, null);
                }
            }
        }
    }
}

function compareUseConstraintWithJson(useConstraint, jsonUseConstraint){
	jsonUseConstraint = JSON.parse(jsonUseConstraint);
	if (useConstraint == jsonUseConstraint.name){
		return true;
	}
	return false;
}

function addLicenseInfo(node, useConstraint, sourceNote) {
	node = XPATH.createElementFromXPath(node, "license");
	XMLUtils.createOrReplaceTextNode(node, useConstraint); //value is set here
	var useConstraintId = transformToIgcDomainId(useConstraint, 6500, "", "Could not map use-constraint, use as free entry: ");

	if (hasValue(useConstraintId)) {
		XMLUtils.createOrReplaceAttribute(node, "id", useConstraintId);
	}
	if (hasValue(sourceNote)) {
		XMLUtils.createOrReplaceAttribute(node, "source", sourceNote);
	}
}

function isSourceNote(useConstraint) {
	return useConstraint.startsWith("Quellenvermerk: ");
}

function isJsonString(useConstraint) {
	return useConstraint.startsWith("{") && useConstraint.endsWith("}");
}

function addJsonUseConstraint(useConstraint, target) {
	// since issue: 1443, json has priority if the same constraint is given in the two formats
	var useConstraintObj = JSON.parse(useConstraint);

	log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/additional-information/use-constraint/license" + "' = '" + useConstraintObj.name + "' to target document.");
	var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/additional-information/use-constraint");

	addLicenseInfo(node, useConstraintObj.name, useConstraintObj.quelle);
}

function addUseConstraint(useConstraint, target, sourceNote) {
	// JSON handled through its own function
	if (!hasValue(useConstraint) || isJsonString(useConstraint)) {
    	return;
    }

	useConstraint = removeConstraintPraefix(useConstraint);
	if(hasValue(sourceNote)){
		sourceNote = removeConstraintPraefix(sourceNote);
	}

	log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/additional-information/use-constraint/license" + "' = '" + useConstraint + "' to target document.");
	var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/additional-information/use-constraint");
	addLicenseInfo(node, useConstraint, sourceNote);
}

function mapAccessConstraints(source, target) {
    var accConstraints = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode");
    if (hasValue(accConstraints)) {
        for (i=0; i<accConstraints.getLength(); i++ ) {
            var isoValue = XPATH.getString(accConstraints.item(i), "./@codeListValue");
            if (isoValue != "otherRestrictions") {
                addAccessConstraint(isoValue, target);
            }
        }
    }

    accConstraints = XPATH.getSiblingsFromXPath(source, "//gmd:identificationInfo//gmd:resourceConstraints/*/gmd:accessConstraints", "gmd:otherConstraints", false);

    if (hasValue(accConstraints)) {
        for (i=0; i<accConstraints.size(); i++ ) {
            var accConstraint = XPATH.getString(accConstraints.get(i), "./gco:CharacterString");
			if (!accConstraint) {
				accConstraint = XPATH.getString(accConstraints.get(i), "./gmx:Anchor");
			}
            var idcCode = codeListService.getSysListEntryKey(1350, accConstraint, "", false);
            log.debug("result from legal constraint: " + idcCode);
            if (hasValue(idcCode)) {
                addLegalConstraint(accConstraint, target);
            } else {
            	// since #1219 access constraints are mapped differently and we have to check
				// in the data field for the text
				// the following lines are an adapter to check first in the data field before
				// the regular check by the official short name used in IGE
            	var accConstraintId = TRANSF.getISOCodeListEntryIdByDataFilter(6010, "\"de\":\"" + accConstraint + "\"");
            	if (accConstraintId) {
					accConstraint = TRANSF.getISOCodeListEntryFromIGCSyslistEntry(6010, accConstraintId);
				}
                addAccessConstraint(accConstraint, target);
            }
        }
    }
}

function addAccessConstraint(accConstraint, target) {
    if (hasValue(accConstraint)) {
        log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/additional-information/access-constraint/restriction" + "' = '" + accConstraint + "' to target document.");
        var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/additional-information/access-constraint");
        node = XPATH.createElementFromXPath(node, "restriction");
        XMLUtils.createOrReplaceTextNode(node, accConstraint);
        var accConstraintId = transformToIgcDomainId(accConstraint, 6010, "", "Could not map access-constraint, use as free entry: ");
        if (hasValue(accConstraintId)) {
            XMLUtils.createOrReplaceAttribute(node, "id", accConstraintId);
        }
    }
}

function addLegalConstraint(accConstraint, target) {
    if (hasValue(accConstraint)) {
        log.debug("adding '" + "/igc/data-sources/data-source/data-source-instance/additional-information/access-constraint/legislation" + "' = '" + accConstraint + "' to target document.");
        var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/additional-information/legislation");
        //node = XPATH.createElementFromXPath(node, "restriction");
        XMLUtils.createOrReplaceTextNode(node, accConstraint);
        var accConstraintId = transformToIgcDomainId(accConstraint, 1350, "", "Could not map access-constraint, use as free entry: ");
        if (hasValue(accConstraintId)) {
            XMLUtils.createOrReplaceAttribute(node, "id", accConstraintId);
        }
    }
}

function mapAddresses(source, target) {

    var isoAddressNodes = XPATH.getNodeList(source, "//*/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue!='']");
    if (hasValue(isoAddressNodes)) {
        var igcAdressNodes = XPATH.createElementFromXPath(target, "/igc/addresses");
        for (i=0; i<isoAddressNodes.getLength(); i++ ) {
        	var isoAddressNode = isoAddressNodes.item(i);
        	var organisationName = XPATH.getString(isoAddressNode, "gmd:organisationName/gco:CharacterString");
        	var individualName = XPATH.getString(isoAddressNode, "gmd:individualName/gco:CharacterString");

        	// then create the actual address
        	var uuid = createUUIDFromAddress(isoAddressNode);
    		var igcAddressNode = XPATH.createElementFromXPathAsSibling(igcAdressNodes, "address/address-instance");
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "address-identifier"), uuid);
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "modificator-identifier"), "xxx");
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "responsible-identifier"), "xxx");
            if (hasValue(individualName)) {
              	// always use free address type for ISO import if address has an individual name, see https://dev.informationgrid.eu/redmine/issues/494
                XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcAddressNode, "type-of-address"), "id", "3");
            } else  {
            	// otherwise import as institution
            	XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcAddressNode, "type-of-address"), "id", "0");
            }
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "organisation"), organisationName);
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "name"), individualName);
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "publication-condition"), "1");
            var countryCode = UtilsCountryCodelist.getCodeFromShortcut3(XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"));
            if (hasValue(countryCode)) {
                XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "country"), XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"));
                XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcAddressNode, "country"), "id", countryCode);
            }

            var administrativeAreaValue = XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:administrativeArea/gco:CharacterString");
            if (administrativeAreaValue) {
                var administrativeAreaKey = codeListService.getSysListEntryKey(6250, administrativeAreaValue, "de");
                XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "administrativeArea"), administrativeAreaValue);
                if (hasValue(administrativeAreaKey)) {
                    XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcAddressNode, "administrative-area"), "id", administrativeAreaKey);
                } else {
                    XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcAddressNode, "administrative-area"), "id", "-1");
                }
            }

            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "postal-code"), XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"));
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "street"), XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"));
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "city"), XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"));
            mapCommunicationData(isoAddressNode, igcAddressNode);
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "function"), XPATH.getString(isoAddressNode, "gmd:positionName/gco:CharacterString"));
            // add hours of service (REDMINE-380, REDMINE-1284) 
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcAddressNode, "hours-of-service"), XPATH.getString(isoAddressNode, "gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService/gco:CharacterString"));

            // add related addresses
            var igcRelatedAddressNode = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/related-address");
            var addressRoleId = transformISOToIgcDomainId(XPATH.getString(isoAddressNode, "gmd:role/gmd:CI_RoleCode/@codeListValue"), 505, "Could not transform ISO address role code to IGC id: ");
            if (!hasValue(addressRoleId)) {
            	addressRoleId = "-1";
            }
            XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcRelatedAddressNode, "type-of-relation"), "entry-id", addressRoleId);
            XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(igcRelatedAddressNode, "type-of-relation"), "list-id", "505");
            var addressRoleValue = transformISOToIgcDomainValue(XPATH.getString(isoAddressNode, "gmd:role/gmd:CI_RoleCode/@codeListValue"), 505, "de", "Could not transform ISO address role code to IGC codelist value: ");
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcRelatedAddressNode, "type-of-relation"), addressRoleValue);
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(igcRelatedAddressNode, "address-identifier"), uuid);

        }

        // set address type of "gmd:contact" addresses with role "Point of Contact" to pointOfContactMd
        var contactMdNodes = XPATH.getNodeList(source, "//gmd:contact/gmd:CI_ResponsibleParty");
        for (var i=0; i<contactMdNodes.getLength(); i++) {
            var contactMdNode = contactMdNodes.item(i);
            var ContactMdUUID = createUUIDFromAddress(contactMdNode);
            // Point Of Contact
            var relatedAddresses = XPATH.getNodeList(target, "//related-address[./type-of-relation/@entry-id=7 and address-identifier='"+ContactMdUUID+"']");
            for (var j=0; j<relatedAddresses.getLength(); j++) {
                var relatedAddressNode = relatedAddresses.item(j);
                XMLUtils.createOrReplaceAttribute(XPATH.getNode(relatedAddressNode, "./type-of-relation"), "entry-id", "12");
                XMLUtils.createOrReplaceTextNode(XPATH.getNode(relatedAddressNode, "./type-of-relation"), "pointOfContactMd");
            }
        }

    }

}

function mapUncontrolledTerms(source, target) {
    var terms = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place') and (not(gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString) or ( (gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString!='OGDD-Kategorien') and (gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString!='German Environmental Classification - Topic, version 1.0') and (gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString!='GEMET - INSPIRE themes, version 1.0') and (gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString!='Service Classification, version 1.0') ))]/gmd:keyword/gco:CharacterString");
    if (hasValue(terms)) {
        for (i=0; i<terms.getLength(); i++ ) {
            var term = XPATH.getString(terms.item(i), ".");
            if (hasValue(term)) {
        		// make sure that service classification codes are not included in uncontrolled keywords
            	// transform to IGC domain id
        		var igcCode = null;
        		try {
        			//igcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(5200, term);
        			igcCode = codeListService.getSysListEntryKey(5200, term, "iso");
        		} catch (e) { /* can be ignored */}
                if (!hasValue(igcCode)) {
	        		// check "inspireidentifiziert" and add flag !
	                if (term.equals("inspireidentifiziert")) {
	                    log.debug("adding '/igc/data-sources/data-source/data-source-instance/general/is-inspire-relevant' = 'Y' to target document.");
	                    XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/is-inspire-relevant"), "Y");
	                } else if (term.equals("opendata")) {
                        log.debug("adding '/igc/data-sources/data-source/data-source-instance/general/is-open-data' = 'Y' to target document.");
                        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/is-open-data"), "Y");
	                } else if (term.equals("AdVMIS")) {
	                    log.debug("adding '/igc/data-sources/data-source/data-source-instance/general/is-adv-compatible' = 'Y' to target document.");
	                    XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/is-adv-compatible"), "Y");
	                } else {
	                    log.debug("adding '/igc/data-sources/data-source/data-source-instance/subject-terms/uncontrolled-term' = '" + term + "' to target document.");
	                    XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/subject-terms/uncontrolled-term"), term);
	                }
                }
            }
        }
    }
}

function mapDistributionLinkages(source, target) {
    var linkages = XPATH.getNodeList(source, "//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']");
    if (hasValue(linkages)) {
        for (i=0; i<linkages.getLength(); i++ ) {
            var linkage = {};
            linkage.name = XPATH.getString(linkages.item(i), "./gmd:name/gco:CharacterString");
            linkage.url = XPATH.getString(linkages.item(i), "./gmd:linkage/gmd:URL");
            linkage.urlType = "1";
            var isCoupled = XPATH.getString(linkages.item(i), "./gmd:applicationProfile/gco:CharacterString") === "coupled";
            linkage.referenceId = isCoupled ? "3600" : "-1";
            //referenceName = "";
            linkage.description = XPATH.getString(linkages.item(i), "./gmd:description/gco:CharacterString");
            addAvailableLinkage(linkage, target);
        }
    }
}

function addAvailableLinkage(linkage, target) {
    if (hasValue(linkage) && hasValue(linkage.url) && hasValue(linkage.urlType)) {
        log.debug("adding '/igc/data-sources/data-source/data-source-instance/available-linkage' -> 'linkage-url' = '" + linkage.url + "' to target document.");
        var linkageNode = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/available-linkage");
        if (!hasValue(linkage.name)) {
            linkage.name = linkage.url;
        }
        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(linkageNode, "linkage-name"), linkage.name);
        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(linkageNode, "linkage-url"), linkage.url);
        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(linkageNode, "linkage-url-type"), linkage.urlType);
        if (hasValue(linkage.referenceId)) {
            XMLUtils.createOrReplaceAttribute(XPATH.createElementFromXPath(linkageNode, "linkage-reference"), "id", linkage.referenceId);
            if (hasValue(linkage.referenceName)) {
                XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(linkageNode, "linkage-reference"), linkage.referenceName);
            }
        }
        if (hasValue(linkage.description)) {
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(linkageNode, "linkage-description"), linkage.description);
        }
    }
}

function mapServiceClassifications(source, target) {
    var terms = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
    if (hasValue(terms)) {
        for (i=0; i<terms.getLength(); i++ ) {
            var term = XPATH.getString(terms.item(i), ".");
            if (hasValue(term)) {
        		// transform to IGC domain id
        		var igcCode = null;
        		try {
        			//igcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(5200, term);
        			igcCode = codeListService.getSysListEntryKey(5200, term, "iso");
        		} catch (e) {
        			if (log.isWarnEnabled()) {
        				log.warn("Error tranforming INSPIRE Service Classification code '" + term + "' with code list 5200 to IGC id. Does the codeList exist?");
        			}
        			protocol(WARN, "Error tranforming ISO code '" + term + "' with code list 5200 to IGC id. Does the codeList exist?")
        		}
        		if (hasValue(igcCode)) {
            		var node = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/technical-domain/service/service-classification");
            		XMLUtils.createOrReplaceAttribute(node, "id", igcCode);
        		}
            }
        }
    }
}

function parseToInt(val) {
	return java.lang.Integer.parseInt(val);
}

function transformNumberStrToIGCNumber(val) {
	return UtilsString.transformNumberStrToIGCNumber(val);
}

// NOTICE: Also used in other mapping scripts in profiles (BKG ...) !
function removeConstraintPraefix(val) {
	if (hasValue(val)) {
//    	log.warn("MM IN constraint : " + val);

    	val = val.trim();

    	// remove GDI-DE prefix
    	val = val.replace("Nutzungseinschränkungen: ", "");
    	val = val.replace("Nutzungsbedingungen: ", "");
    	val = val.replace("Quellenvermerk: ", "");

//    	log.warn("MM OUT constraint : " + val);
	}
	return val;
}

function transformToStaticValue(val, staticValue) {
    return staticValue;
}

function transformGeneric(val, mappings, caseSensitive, logErrorOnNotFound) {
	for (var key in mappings) {
		if (caseSensitive) {
			if (key == val) {
				return mappings[key];
			}
		} else {
			if (key.toLowerCase() == val.toLowerCase()) {
				return mappings[key];
			}
		}
	}
	if (logErrorOnNotFound) {
		log.warn(logErrorOnNotFound + val);
		protocol(WARN, logErrorOnNotFound + val)
	}
	return val;
}

function transformToIgcDomainId(val, codeListId, languageId, logErrorOnNotFound, doRobustComparison) {
	if (hasValue(val)) {
		// transform to IGC domain id
		var idcCode = null;
		try {
            // more robust comparison, see INGRID-2334
			var robustCompare = false;
			if (doRobustComparison) {
                robustCompare = true;
			}
            //idcCode = UtilsUDKCodeLists.getCodeListDomainId(codeListId, val, languageId);
		    idcCode = codeListService.getSysListEntryKey(codeListId, val, languageId, robustCompare);
		} catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?");
			}
			protocol(WARN, "Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
		if (hasValue(idcCode)) {
			return idcCode;
		} else {
			if (log.isWarnEnabled()) {
				if (languageId != null && languageId != "") {
					log.warn("Domain code '" + val + "' unknown in code list " + codeListId + " for language '" + languageId + "'.");
					protocol(WARN, "Domain code '" + val + "' unknown in code list " + codeListId + " for language '" + languageId + "'.");
				} else {
                    log.warn("Domain code '" + val + "' unknown in code list " + codeListId + " for all languages.");
                    protocol(WARN, "Domain code '" + val + "' unknown in code list " + codeListId + " for all languages.");
				}
			}
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
	}
}


function transformToIgcDomainValue(val, codeListId, languageId, logErrorOnNotFound) {
	if (hasValue(val)) {
		// transform to IGC domain id
		var idcValue = null;
		try {
			//idcValue = UtilsUDKCodeLists.getCodeListEntryName(codeListId, parseToInt(val), languageId);
			idcValue = codeListService.getSysListEntryName(codeListId, val, languageId);
		} catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming ID '" + val + "' with code list " + codeListId + " with language '" + languageId + "'. Does the codeList exist?");
			}
			protocol(WARN, "Error tranforming ID '" + val + "' with code list " + codeListId + " with language '" + languageId + "'. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
		if (hasValue(idcValue)) {
			return idcValue;
		} else {
			if (log.isWarnEnabled()) {
				log.warn("Domain ID '" + val + "' unknown in code list " + codeListId + " for language '" + languageId + "'.");
				protocol(WARN, "Domain ID '" + val + "' unknown in code list " + codeListId + " for language '" + languageId + "'.")
			}
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
			return "";
		}
	}
}

function transformISOToIgcDomainId(val, codeListId, logErrorOnNotFound, doRobustComparison) {
	if (hasValue(val)) {
		// transform to IGC domain id
		var idcCode = null;
		try {
            // more robust comparison, see INGRID-2334
            var robustCompare = false;
            if (doRobustComparison) {
                robustCompare = true;
            }
			//idcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(codeListId, val);
			idcCode = codeListService.getSysListEntryKey(codeListId, val, "iso", robustCompare);
		} catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming ISO code '" + val + "' with code list " + codeListId + " to IGC id. Does the codeList exist?");
			}
			protocol(WARN, "Error tranforming ISO code '" + val + "' with code list " + codeListId + " to IGC id. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
		if (hasValue(idcCode)) {
			return idcCode;
		} else {
			if (log.isWarnEnabled()) {
				log.warn("ISO code '" + val + "' unknown in code list " + codeListId + ".");
				protocol(WARN, "ISO code '" + val + "' unknown in code list " + codeListId + ".")
			}
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
			return -1;
		}
	}
}

function transformISOToIgcDomainValue(val, codeListId, languageId, logErrorOnNotFound) {
	if (hasValue(val)) {
		// transform ISO code to IGC domain value
		var idcValue = null;
		try {
			//var idcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(codeListId, val);
			var idcCode = codeListService.getSysListEntryKey(codeListId, val, "iso");
			//idcValue = UtilsUDKCodeLists.getCodeListEntryName(codeListId, parseToInt(idcCode), parseToInt(languageId));
			idcValue = codeListService.getSysListEntryName(codeListId, idcCode, languageId);
		} catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming ISO code '" + val + "' with code list " + codeListId + " to IGC value with language '" + languageId + "'. Does the codeList exist?"  + e.toString());
			}
			protocol(WARN, "Error tranforming ISO code '" + val + "' with code list " + codeListId + " to IGC value with language '" + languageId + "'. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
		if (hasValue(idcValue)) {
			return idcValue;
		} else {
			if (log.isWarnEnabled()) {
				log.warn("ISO code '" + val + "' unknown in code list " + codeListId + ".");
				protocol(WARN, "ISO code '" + val + "' unknown in code list " + codeListId + ".")
			}
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
			return "";
		}
	}
}

function transformConformityToIgcDomainId(val, languageId, logErrorOnNotFound, doRobustComparison) {
    if (hasValue(val)) {
        // Robust comparison
        var robustCompare = false;
        if (doRobustComparison) {
            robustCompare = true;
        }

        // Which codelist should be used?
        var isInspire = conformityIsInspire(val, languageId, doRobustComparison);
        var codeListId = 6005; // Use INSPIRE by default
        if (isInspire != 'Y') {
            codeListId = 6006;
        }

        // transform to IGC domain id
        var idcCode = null;
        try {
            idcCode = codeListService.getSysListEntryKey(codeListId, val, languageId, robustCompare);
        } catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?");
			}
			protocol(WARN, "Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
        }
        log.debug("Values while debugging conformities: value -> " + val + ", codelist id -> " + codeListId + ", idcCode -> " + idcCode);
        if (hasValue(idcCode)) {
            return idcCode;
        } else {
				if (languageId != null && languageId != "") {
					log.warn("Domain code '" + val + "' unknown in code lists 6005 and 6006 for language '" + languageId + "'.");
					protocol(WARN, "Domain code '" + val + "' unknown in code lists 6005 and 6006 for language '" + languageId + "'.");
				} else {
                    log.warn("Domain code '" + val + "' unknown in code lists 6005 and 6006 for all languages.");
                    protocol(WARN, "Domain code '" + val + "' unknown in code lists 6005 and 6006 for all languages.");
				}
        }
    }
}

function conformityIsInspire(val, languageId, doRobustComparison) {
	if (hasValue(val)) {
		// transform to IGC domain id
        var codeListId = 6005;
		var idcCode = null;
		try {
            // more robust comparison, see INGRID-2334
			var robustCompare = false;
			if (doRobustComparison) {
                robustCompare = true;
			}
		    idcCode = codeListService.getSysListEntryKey(codeListId, val, languageId, robustCompare);
		} catch (e) {
			if (log.isWarnEnabled()) {
				log.warn("Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?");
			}
			protocol(WARN, "Error tranforming code '" + val + "' with code list " + codeListId + " with language '" + languageId + "' to IGC id. Does the codeList exist?")
			if (logErrorOnNotFound) {
				log.warn(logErrorOnNotFound + val);
				protocol(WARN, logErrorOnNotFound + val)
			}
		}
		if (hasValue(idcCode) && idcCode > 0) {
			return "Y";
		} else {
            return "N";
		}
	}
    return "N";
}

function transformISOToIGCLanguageCode(val) {
	var code = UtilsLanguageCodelist.getCodeFromIso639_2(val);
	if (!hasValue(code)) {
		code = UtilsLanguageCodelist.getCodeFromShortcut(val);
	}
	return code;
}

function transformISOToLanguage(val, iso639_1) {
	var code = UtilsLanguageCodelist.getCodeFromIso639_2(val);
	if (!hasValue(code)) {
		code = UtilsLanguageCodelist.getCodeFromShortcut(val);
	}
	if (!hasValue(iso639_1)) {
		return UtilsLanguageCodelist.getNameFromCode(code, "de");
	} else {
		return UtilsLanguageCodelist.getNameFromCode(code, iso639_1);
	}
}

function transformDateIso8601ToIndex(isoFormat) {
	if (!UtilsCSWDate.isCSWDate(isoFormat)) {
		log.warn("ISO Date '" + isoFormat + "' could not be transformed. Date is not ISO 8601 conform. Leaving value unchanged.");
		protocol(WARN, "ISO Date '" + isoFormat + "' could not be transformed. Date is not ISO 8601 conform. Leaving value unchanged.");
		return isoFormat;
	}
	return UtilsCSWDate.mapDateFromIso8601ToIndex(isoFormat);
}

function transformAlternateNameAndProductGroup(source, target) {
    var altTitles = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString");
    if (hasValue(altTitles)) {
        var productGroups = [];
        var nonProductGroups = [];

        for (var i=0; i<altTitles.getLength(); i++ ) {
            var term = XPATH.getString(altTitles.item(i), ".");

            var splitted = term.split(';');
            if (splitted.length > 0) {

                // check all entries if they match to a product group and move them to this field
                for (var j=0; j<splitted.length; j++) {
                    var entry = splitted[j].trim();
                    var codelistItem = codeListService.getSysListEntryKey(8010, entry, "de");
                    // TODO: what about english entries
                    if (codelistItem !== undefined && codelistItem != null) {
                        // add to product group
                        productGroups.push(entry);
                    } else {
                        // let it stay in alternate title
                        nonProductGroups.push(entry);
                    }
                }
            }
        }
        if (productGroups.length > 0) {
            var node = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/adv-product-group");
            for (var k=0; k<productGroups.length; k++) {
                XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPathAsSibling(node, "item"), productGroups[k]);
            }
        }

        var finalAlternateName = "";
        if (nonProductGroups.length > 0) {
            finalAlternateName = nonProductGroups.join(";");
        }
        var pathAlternateName = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/dataset-alternate-name");
        XMLUtils.createOrReplaceTextNode(pathAlternateName, finalAlternateName);
    }
}

function getTypeOfAddress(source, target) {
	var organisationName = XPATH.getString(source, "gmd:organisationName/gco:CharacterString");
	var individualName = XPATH.getString(source, "gmd:individualName/gco:CharacterString");
	var node = XPATH.createElementFromXPath(target, "type-of-address");
	if (hasValue(individualName)) {
		XMLUtils.createOrReplaceAttribute(node, "id", "2");
	} else if (hasValue(organisationName)) {
		XMLUtils.createOrReplaceAttribute(node, "id", "0");
	} else {
		XMLUtils.createOrReplaceAttribute(node, "id", "2");
	}
}

function handleBoundingPolygon(source, target) {
	var gmlPointPosNodes = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:polygon/gml:Point/gml:pos");
	for(var i=0; i<gmlPointPosNodes.getLength(); i++) {
		var wkt = gmlPosListToWktCoordinates(gmlPointPosNodes.item(i));
		if (wkt) {
			wkt = "POINT " + wkt;
			addPolygonWktToIgc(target, wkt);
		}
	}

	var gmlLineStringPosListNodes = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:polygon/gml:LineString/gml:posList");
	for(var i=0; i<gmlLineStringPosListNodes.getLength(); i++) {
		var wkt = gmlPosListToWktCoordinates(gmlLineStringPosListNodes.item(i));
		if (wkt) {
			wkt = "LINESTRING " + wkt;
			addPolygonWktToIgc(target, wkt);
		}
	}

	// Polygon elements
	var gmlPolygonNodes = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:polygon/gml:Polygon");
	for(var i=0; i<gmlPolygonNodes.getLength(); i++) {
		var wkt = "";
	    var polygonNode = gmlPolygonNodes.item(i);

		var exteriorNode = XPATH.getNode(polygonNode, "./gml:exterior/gml:LinearRing/gml:posList");
		wkt += gmlPosListToWktCoordinates(exteriorNode);

		var interiorNodes = XPATH.getNodeList(polygonNode, "./gml:interior/gml:LinearRing/gml:posList");
		for(var j=0; j<interiorNodes.getLength(); j++) {
			var str = gmlPosListToWktCoordinates(interiorNodes.item(j));
			if (wkt && str)  {
				wkt += ", ";
			}
			wkt += str;
		}
		if (wkt) {
			wkt = "POLYGON (" + wkt + ")";
			addPolygonWktToIgc(target, wkt);
		}
	}
}

function gmlPosListToWktCoordinates(node) {
	if (!node) return "";

	var posList = node.getTextContent();
	if (!hasValue(posList)) return "";

	posList = posList.replace(/[\r\n]/g, ""); // Remove newlines
	var coords = "";
	var arr = posList.split(/\s+/);
	for(var i=0; i<arr.length; i+=2) {
		if (coords) { // not empty
			coords += ", ";
		}
		coords += arr[i] + " " + arr[i+1];
	}
	return "(" + coords + ")";
}

function addPolygonWktToIgc(target, wkt) {
	var additionalValues = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
	var additionalValue = XPATH.createElementFromXPath(additionalValues, "general-additional-value");

	XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-key"), "boundingPolygon");
	XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-data"), wkt);
}

function determineGridSpatialRepresentationConcreteType(source, target) {
	var isGeorectified = XPATH.nodeExists(source, ".//gmd:MD_Georectified") ? "Y" : "N";
	var georectifiedNode = XPATH.createElementFromXPath(target, "/igc/data-sources/data-source/data-source-instance/technical-domain/map/grid-format/grid-geo-rectified");
	XMLUtils.createOrReplaceTextNode(georectifiedNode, isGeorectified);
}


function hasValue(val) {
	if (typeof val == "undefined") {
		return false;
	} else if (!val) {
		return false;
	} else if (val == null) {
		return false;
	} else if (val instanceof String && val == "") {
		return false;
	} else {
	  return true;
	}
}

function call_f(f,args)
{
  if (hasValue(args)) {
    if (args.length === 0)
      return f();
    else if (args.length === 1)
      return f(args[0]);
    else if (args.length === 2)
      return f(args[0], args[1]);
    else if (args.length === 3)
      return f(args[0], args[1], args[2]);
    else if (args.length === 4)
      return f(args[0], args[1], args[2], args[3]);
    else if (args.length === 5)
        return f(args[0], args[1], args[2], args[3], args[4]);
    else
      log.error("Function call does not support number of arguments: " + args.length);

  } else {
    return f();
  }
}






function createUUIDFromAddress(source) {
	log.debug("create UUID from address node: " + source);
	var isoUuid = XPATH.getString(source, "./@uuid");
	var organisationName = XPATH.getString(source, "gmd:organisationName/gco:CharacterString");
	var individualName = XPATH.getString(source, "gmd:individualName/gco:CharacterString");
	var email = XPATH.getString(source, "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");

	var idString = "";
	if (hasValue(organisationName)) {
		idString += organisationName;
	}
	if (hasValue(individualName)) {
		idString += individualName;
	}
	if (hasValue(email)) {
		idString += email;
	}

	var uuid;
	// first check for valid uuid to be used for address identification
	if (hasValue(isoUuid)) {
	    uuid = isoUuid;
	} else if (hasValue(idString) && (hasValue(email) || (hasValue(organisationName) && hasValue(individualName)))) {
	    // otherwise create a uuid from the content, to try to find an address
	    // this should work if same address was referenced without a uuid
	    uuid = createUUIDFromString(idString.toString());
	} else {
	    // if no content was given, create a completely new uuid
		protocol(INFO, "Insufficient data for UUID creation (no 'email' or only one of 'individualName' or 'organisationName' has been set for this address: email='" + email + "', individualName='" + individualName + "', organisationName='" + organisationName + "'!)")
		protocol(INFO, "A new random UUID will be created!")
		log.info("Insufficient data for UUID creation (no 'email' or only one of 'individualName' or 'organisationName' has been set for this address: email='" + email + "', individualName='" + individualName + "', organisationName='" + organisationName + "'!)");
		log.info("A new random UUID will be created!");
		uuid = createUUID();
	}
	log.info("Created UUID from Address:" + uuid);

	return uuid;
}

function createUUIDFromString(str) {
	// replace multiple white spaces by ' '
	var uuid = java.util.UUID.nameUUIDFromBytes((new java.lang.String(str).replaceAll("\\s+", " ")).getBytes());
	var idcUuid = new java.lang.StringBuffer(uuid.toString().toUpperCase());
	while (idcUuid.length() < 36) {
		idcUuid.append("0");
	}
	uuid = idcUuid.toString();
	return uuid;
}


function createUUID() {
	var uuid = java.util.UUID.randomUUID();
	var idcUuid = new java.lang.StringBuffer(uuid.toString().toUpperCase());
	while (idcUuid.length() < 36) {
		idcUuid.append("0");
	}
	log.debug("Created UUID:" + idcUuid.toString());

	return idcUuid.toString();
}

function protocol(level, msg) {
	if (level==DEBUG){
	    protocolHandler.addMessage(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler.Type.DEBUG, msg);
	} else if (level==INFO){
	    protocolHandler.addMessage(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler.Type.INFO, msg);
	} else if (level==WARN){
	    protocolHandler.addMessage(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler.Type.WARN, msg);
	} else if (level==ERROR){
	    protocolHandler.addMessage(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler.Type.ERROR, msg);
	}
}

