package de.ingrid.mdek.job.tools;

import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IFullIndexAccess;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.FullIndexObj;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoKeyc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjProject;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpDepends;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;


/**
 * Handles Update of Full Index.
 */
public class MdekFullIndexHandler implements IFullIndexAccess {

	private static final Logger LOG = Logger.getLogger(MdekFullIndexHandler.class);
	
	protected MdekCatalogHandler catalogHandler;

	private IGenericDao<IEntity> daoFullIndexAddr;
	private IGenericDao<IEntity> daoFullIndexObj;
	private ISysListDao daoSysList;

	private static MdekFullIndexHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekFullIndexHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekFullIndexHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekFullIndexHandler(DaoFactory daoFactory) {
		catalogHandler = MdekCatalogHandler.getInstance(daoFactory);

		daoFullIndexAddr = daoFactory.getDao(FullIndexAddr.class);
		daoFullIndexObj = daoFactory.getDao(FullIndexObj.class);
		daoSysList = daoFactory.getSysListDao();
	}

	/** Updates data of given address in full index. */
	public void updateAddressIndex(AddressNode aNode) {
		// template for accessing data in index
		FullIndexAddr template = new FullIndexAddr();
		template.setAddrNodeId(aNode.getId());

		// update full data
		template.setIdxName(IDX_NAME_FULLTEXT);
		FullIndexAddr idxEntry = (FullIndexAddr) daoFullIndexAddr.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		String data = getFullData(aNode.getT02AddressWork());
		idxEntry.setIdxValue(data);
		
		daoFullIndexAddr.makePersistent(idxEntry);

		// template for accessing data in index
		template = new FullIndexAddr();
		template.setAddrNodeId(aNode.getId());

		// update partial data
		template.setIdxName(IDX_NAME_PARTIAL);
		idxEntry = (FullIndexAddr) daoFullIndexAddr.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		data = getPartialData(aNode.getT02AddressWork());
		idxEntry.setIdxValue(data);
		
		daoFullIndexAddr.makePersistent(idxEntry);
	
	}

	/** Updates data of given object in full index. */
	public void updateObjectIndex(ObjectNode oNode) {
		// template for accessing data in index
		FullIndexObj template = new FullIndexObj();
		template.setObjNodeId(oNode.getId());

		// update full data
		template.setIdxName(IDX_NAME_FULLTEXT);
		FullIndexObj idxEntry = (FullIndexObj) daoFullIndexObj.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		String data = getFullData(oNode.getT01ObjectWork());
		idxEntry.setIdxValue(data);
		
		daoFullIndexObj.makePersistent(idxEntry);
	}

	/** Get full data of given address for updating full text index. */
	private String getFullData(T02Address a) {
		StringBuffer data = new StringBuffer();

		if (a == null) {
			// this should never happen, so log to this!
			LOG.warn("Address for building full text index was null. Returning full text ''!!");
			return data.toString();
		}
		
		// AddressComment
		Set<AddressComment> comments = a.getAddressComments();
		for (AddressComment comment : comments) {
			extendFullData(data, comment.getComment());
		}
		// SearchtermAdr
		Set<SearchtermAdr> terms = a.getSearchtermAdrs();
		for (SearchtermAdr term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			if (termType == SearchtermType.FREI) {
				extendFullData(data, termValue.getTerm());
			} else if (termType == SearchtermType.THESAURUS) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
				extendFullData(data, termValue.getTerm());
			}
		}
		// T021Communication
		Set<T021Communication> comms = a.getT021Communications();
		for (T021Communication comm : comms) {
			extendFullData(data, comm.getCommValue());
		}
		// T02Address
		extendFullData(data, a.getAdrUuid());
		extendFullData(data, a.getOrgAdrId());
		extendFullData(data, a.getInstitution());
		extendFullData(data, a.getLastname());
		extendFullData(data, a.getFirstname());
		extendFullDataWithSysList(data, MdekSysList.ADDRESS_VALUE, a.getAddressKey(), a.getAddressValue());
		extendFullDataWithSysList(data, MdekSysList.ADDRESS_TITLE, a.getTitleKey(), a.getTitleValue());
		extendFullData(data, a.getStreet());
		extendFullData(data, a.getPostcode());
		extendFullData(data, a.getPostbox());
		extendFullData(data, a.getPostboxPc());
		extendFullData(data, a.getCity());
		extendFullData(data, a.getJob());
		extendFullData(data, a.getDescr());

		return data.toString();
	}

	/** Get partial data of given address for updating full text index. */
	private String getPartialData(T02Address a) {
		StringBuffer data = new StringBuffer();

		if (a == null) {
			// this should never happen, so log to this!
			LOG.warn("Address for building partial index was null. Returning partial data ''!!");
			return data.toString();
		}
		
		// SearchtermAdr
		Set<SearchtermAdr> terms = a.getSearchtermAdrs();
		for (SearchtermAdr term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			if (termType == SearchtermType.FREI) {
				extendFullData(data, termValue.getTerm());
			} else if (termType == SearchtermType.THESAURUS) {
				extendFullData(data, termValue.getTerm());
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
			}
		}
		// T02Address
		extendFullData(data, a.getInstitution());
		extendFullData(data, a.getLastname());
		extendFullData(data, a.getFirstname());
		extendFullData(data, a.getDescr());

		return data.toString();
	}	
	
	/** Get full data of given object for updating full text index. */
	private String getFullData(T01Object o) {
		StringBuffer data = new StringBuffer();
		
		if (o == null) {
			// this should never happen, so log to this!
			LOG.warn("Object for building full text index was null. Returning full text ''!!");
			return data.toString();
		}
		
		// ObjectComment
		Set<ObjectComment> comments = o.getObjectComments();
		for (ObjectComment comment : comments) {
			extendFullData(data, comment.getComment());
		}
		// SearchtermObj
		Set<SearchtermObj> terms = o.getSearchtermObjs();
		for (SearchtermObj term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			if (termType == SearchtermType.FREI) {
				extendFullData(data, termValue.getTerm());
			} else if (termType == SearchtermType.THESAURUS) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
			}
		}
		// SpatialReference
		Set<SpatialReference> spatRefs = o.getSpatialReferences();
		for (SpatialReference spatRef : spatRefs) {
			SpatialRefValue spatRefValue = spatRef.getSpatialRefValue();
			extendFullData(data, spatRefValue.getNativekey());
			SpatialReferenceType spatRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, spatRefValue.getType());
			if (spatRefType == SpatialReferenceType.FREI) {
				extendFullDataWithSysList(data, MdekSysList.SPATIAL_REF_VALUE,
						spatRefValue.getNameKey(), spatRefValue.getNameValue());
			} else if (spatRefType == SpatialReferenceType.GEO_THESAURUS) {
				extendFullData(data, spatRefValue.getSpatialRefSns().getSnsId());
			}
		}
		// T0110AvailFormat
		Set<T0110AvailFormat> formats = o.getT0110AvailFormats();
		for (T0110AvailFormat format : formats) {
			extendFullDataWithSysList(data, MdekSysList.AVAIL_FORMAT,
					format.getFormatKey(), format.getFormatValue());
			extendFullData(data, format.getVer());
			extendFullData(data, format.getFileDecompressionTechnique());
			extendFullData(data, format.getSpecification());
		}
		// T0112MediaOption
		Set<T0112MediaOption> medOpts = o.getT0112MediaOptions();
		for (T0112MediaOption medOpt : medOpts) {
			extendFullData(data, medOpt.getMediumNote());
			extendFullDataWithSysList(data, MdekSysList.MEDIA_OPTION_MEDIUM,
					medOpt.getMediumName(), null);
		}
		// T011ObjData
		Set<T011ObjData> oDatas = o.getT011ObjDatas();
		for (T011ObjData oData : oDatas) {
			extendFullData(data, oData.getBase());
			extendFullData(data, oData.getDescription());
		}
		// T011ObjDataPara
		Set<T011ObjDataPara> oDataParas = o.getT011ObjDataParas();
		for (T011ObjDataPara oDataPara : oDataParas) {
			extendFullData(data, oDataPara.getParameter());
			extendFullData(data, oDataPara.getUnit());
		}
		// T011ObjGeo
		Set<T011ObjGeo> oGeos = o.getT011ObjGeos();
		for (T011ObjGeo oGeo : oGeos) {
			extendFullData(data, oGeo.getSpecialBase());
			extendFullData(data, oGeo.getDataBase());
			extendFullData(data, oGeo.getMethod());
			extendFullDataWithSysList(data, MdekSysList.OBJ_GEO_REFERENCESYSTEM,
					oGeo.getReferencesystemKey(), oGeo.getReferencesystemValue());
			Set<T011ObjGeoKeyc> oGeoKeycs = oGeo.getT011ObjGeoKeycs();
			for (T011ObjGeoKeyc oGeoKeyc : oGeoKeycs) {
				extendFullData(data, MdekUtils.timestampToDisplayDate(oGeoKeyc.getKeyDate()));
				extendFullData(data, oGeoKeyc.getEdition());
				extendFullDataWithSysList(data, MdekSysList.OBJ_GEO_KEYC,
						oGeoKeyc.getKeycKey(), oGeoKeyc.getKeycValue());
			}
			Set<T011ObjGeoSymc> oGeoSymcs = oGeo.getT011ObjGeoSymcs();
			for (T011ObjGeoSymc oGeoSymc : oGeoSymcs) {
				extendFullData(data, MdekUtils.timestampToDisplayDate(oGeoSymc.getSymbolDate()));
				extendFullData(data, oGeoSymc.getEdition());
				extendFullDataWithSysList(data, MdekSysList.OBJ_GEO_SYMC,
						oGeoSymc.getSymbolCatKey(), oGeoSymc.getSymbolCatValue());
			}
		}
		// T011ObjLiterature
		Set<T011ObjLiterature> oLits = o.getT011ObjLiteratures();
		for (T011ObjLiterature oLit : oLits) {
			extendFullData(data, oLit.getAuthor());			
			extendFullData(data, oLit.getPublisher());			
			extendFullData(data, oLit.getPublishIn());			
			extendFullData(data, oLit.getVolume());			
			extendFullData(data, oLit.getSides());			
			extendFullData(data, oLit.getPublishYear());			
			extendFullData(data, oLit.getPublishLoc());			
			extendFullData(data, oLit.getLoc());			
			extendFullData(data, oLit.getDocInfo());			
			extendFullData(data, oLit.getBase());			
			extendFullData(data, oLit.getIsbn());			
			extendFullData(data, oLit.getPublishing());			
			extendFullData(data, oLit.getDescription());			
			extendFullDataWithSysList(data, MdekSysList.OBJ_LITERATURE_TYPE,
					oLit.getTypeKey(), oLit.getTypeValue());
		}
		// T011ObjProject
		Set<T011ObjProject> oProjs = o.getT011ObjProjects();
		for (T011ObjProject oProj : oProjs) {
			extendFullData(data, oProj.getLeader());
			extendFullData(data, oProj.getMember());
			extendFullData(data, oProj.getDescription());
		}
		// T011ObjServ
		Set<T011ObjServ> oServs = o.getT011ObjServs();
		for (T011ObjServ oServ : oServs) {
			extendFullData(data, oServ.getHistory());
			extendFullData(data, oServ.getEnvironment());
			extendFullData(data, oServ.getBase());
			extendFullData(data, oServ.getDescription());
			String oServTypeName = getSysListOrFreeValue(MdekSysList.OBJ_SERV_TYPE,
					oServ.getTypeKey(), oServ.getTypeValue());
			extendFullData(data, oServTypeName);
			Set<T011ObjServOperation> oServOps = oServ.getT011ObjServOperations();
			for (T011ObjServOperation oServOp : oServOps) {
				if (MdekUtils.OBJ_SERV_TYPE_WMS.equals(oServTypeName)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_WMS,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else if (MdekUtils.OBJ_SERV_TYPE_WFS.equals(oServTypeName)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_WFS,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else {
					extendFullData(data, oServOp.getNameValue());					
				}
				extendFullData(data, oServOp.getDescr());
				extendFullData(data, oServOp.getInvocationName());
				Set<T011ObjServOpConnpoint> oServOpConnpts = oServOp.getT011ObjServOpConnpoints();
				for (T011ObjServOpConnpoint oServOpConnpt : oServOpConnpts) {
					extendFullData(data, oServOpConnpt.getConnectPoint());
				}
				Set<T011ObjServOpDepends> oServOpDeps = oServOp.getT011ObjServOpDependss();
				for (T011ObjServOpDepends oServOpDep : oServOpDeps) {
					extendFullData(data, oServOpDep.getDependsOn());
				}
				Set<T011ObjServOpPara> oServOpParas = oServOp.getT011ObjServOpParas();
				for (T011ObjServOpPara oServOpPara : oServOpParas) {
					extendFullData(data, oServOpPara.getName());
					extendFullData(data, oServOpPara.getDirection());
					extendFullData(data, oServOpPara.getDescr());
				}
				Set<T011ObjServOpPlatform> oServOpPlatfs = oServOp.getT011ObjServOpPlatforms();
				for (T011ObjServOpPlatform oServOpPlatf : oServOpPlatfs) {
					extendFullData(data, oServOpPlatf.getPlatform());
				}
			}
			Set<T011ObjServVersion> oServVersions = oServ.getT011ObjServVersions();
			for (T011ObjServVersion oServVersion : oServVersions) {
				extendFullData(data, oServVersion.getServVersion());
			}
		}
		// T014InfoImpart
		Set<T014InfoImpart> infoImps = o.getT014InfoImparts();
		for (T014InfoImpart infoImp : infoImps) {
			extendFullDataWithSysList(data, MdekSysList.INFO_IMPART,
					infoImp.getImpartKey(), infoImp.getImpartValue());				
		}
		// T015Legist
		Set<T015Legist> oLegists = o.getT015Legists();
		for (T015Legist oLegist : oLegists) {
			extendFullDataWithSysList(data, MdekSysList.LEGIST,
					oLegist.getLegistKey(), oLegist.getLegistValue());				
		}
		// T017UrlRef
		Set<T017UrlRef> oUrlRefs = o.getT017UrlRefs();
		for (T017UrlRef oUrlRef : oUrlRefs) {
			extendFullData(data, oUrlRef.getUrlLink());
			extendFullData(data, oUrlRef.getContent());
			extendFullData(data, oUrlRef.getVolume());
			extendFullData(data, oUrlRef.getIconText());
			extendFullData(data, oUrlRef.getDescr());
			extendFullDataWithSysList(data, MdekSysList.URL_REF_SPECIAL,
					oUrlRef.getSpecialRef(), oUrlRef.getSpecialName());				
			extendFullDataWithSysList(data, MdekSysList.URL_REF_DATATYPE,
					oUrlRef.getDatatypeKey(), oUrlRef.getDatatypeValue());				
		}
		// TODO: add T08Attr.data when mapped with Hibernate !!!

		// T01Object
		extendFullData(data, o.getObjUuid());
		extendFullData(data, o.getObjName());
		extendFullData(data, o.getOrgObjId());
		extendFullData(data, o.getObjDescr());
		extendFullData(data, o.getInfoNote());
		extendFullData(data, o.getAvailAccessNote());
		extendFullData(data, o.getLocDescr());
		extendFullData(data, o.getTimeDescr());
		extendFullData(data, o.getDatasetAlternateName());
		extendFullData(data, o.getDatasetUsage());
		extendFullData(data, o.getMetadataStandardName());
		extendFullData(data, o.getMetadataStandardVersion());
		extendFullData(data, o.getFees());
		extendFullData(data, o.getOrderingInstructions());

		return data.toString();
	}

	/** Append a value to full data. also adds separator
	 * @param fullData full data where value is appended
	 * @param dataToAppend the value to append
	 */
	private void extendFullData(StringBuffer fullData, String dataToAppend) {
		fullData.append(IDX_SEPARATOR);
		fullData.append(dataToAppend);
	}

	/** Append SysList or free entry value to full data.
	 * @param fullData full data where value is appended
	 * @param sysList which syslist (see enumeration)
	 * @param sysListEntryId the entryId from bean
	 * @param freeValue the free value from bean
	 */
	private void extendFullDataWithSysList(StringBuffer fullData,
			MdekSysList sysList, Integer sysListEntryId, String freeValue) {
		String value = getSysListOrFreeValue(sysList, sysListEntryId, freeValue);
		if (value != null) {
			extendFullData(fullData, value);			
		}
	}

	/** Analyzes given data and returns entry value or free value
	 * @param sysList which syslist (see enumeration)
	 * @param sysListEntryId the entryId from bean
	 * @param freeValue the free value from bean
	 * @return value (may be null if entry not found or free value is null ...)
	 */
	private String getSysListOrFreeValue(MdekSysList sysList, Integer sysListEntryId, String freeValue) {
		String retValue = null;

		if (sysListEntryId == null || freeValue == null) {
			return retValue;
		} else if (MdekSysList.FREE_ENTRY.getDbValue().equals(sysListEntryId)) {
			retValue = freeValue;
		} else {
			String catalogLanguage = catalogHandler.getCatalogLanguage();
			// TODO: cache syslists in Ehcache
			SysList listEntry = daoSysList.getSysListEntry(sysList.getDbValue(), sysListEntryId, catalogLanguage);
			if (listEntry != null) {
				retValue = listEntry.getName();
			}
		}
		
		return retValue;
	}
}
