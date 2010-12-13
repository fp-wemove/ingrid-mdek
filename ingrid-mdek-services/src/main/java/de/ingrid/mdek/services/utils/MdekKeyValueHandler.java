package de.ingrid.mdek.services.utils;

import java.util.Map;

import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoKeyc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;


/**
 * Encapsulates validation and mapping of key/value pairs in beans. -> syslists !
 */
public class MdekKeyValueHandler {

	private static MdekKeyValueHandler myInstance;

	private MdekCatalogService catalogService;

	// DO NOT FORGET TO KEEP THIS ONE UP TO DATE !!!
	// !!! DO NOT FORGET TO ASSURE ACCORDING DAO CAN BE FETCHED VIA DaoFactory.getDao(Class) !!!!
	private static Class[] keyValueClasses = new Class[] {
		T011ObjServ.class,
		T011ObjServOperation.class,
		T011ObjGeo.class,
		T011ObjGeoSymc.class,
		T011ObjGeoKeyc.class,
		T017UrlRef.class,
		T015Legist.class,
		T014InfoImpart.class,
		T0110AvailFormat.class,
		T011ObjLiterature.class,
		T021Communication.class,
		SpatialRefValue.class,
		T02Address.class,
		ObjectReference.class,
		T012ObjAdr.class,
		ObjectConformity.class,
		ObjectAccess.class,
		T011ObjServType.class,
		SearchtermValue.class,
		T03Catalogue.class,
		T01Object.class,
		ObjectDataQuality.class,
		ObjectFormatInspire.class
	};

	/** Get The Singleton */
	public static synchronized MdekKeyValueHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekKeyValueHandler(daoFactory);
		}
		return myInstance;
	}

	private MdekKeyValueHandler(DaoFactory daoFactory) {
		catalogService = MdekCatalogService.getInstance(daoFactory);
	}

	
	/** Get all entity classes containing key/value pairs */
	public Class[] getEntityClassesContainingKeyValue() {
		return keyValueClasses;
	}

	/** evaluate keys and set correct syslist values in bean according to bean type. */
	public IEntity processKeyValue(IEntity bean) {
		Class clazz = bean.getClass();
		
		// NOTICE: bean may be proxy class generated by hibernate (=subclass of orig class)
		if (T011ObjServ.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName() +
				" -> Process with separate method 'processKeyValueT011ObjServ(...)' !!!");
		} else if (T011ObjServOperation.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName() +
				" -> Process with separate method 'processKeyValueT011ObjServOperation(...)' !!!");
		} else if (T011ObjGeo.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjGeo((T011ObjGeo) bean);
		} else if (T011ObjGeoSymc.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjGeoSymc((T011ObjGeoSymc) bean);
		} else if (T011ObjGeoKeyc.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjGeoKeyc((T011ObjGeoKeyc) bean);
		} else if (T017UrlRef.class.isAssignableFrom(clazz)) {
			processKeyValueT017UrlRef((T017UrlRef) bean);
		} else if (T015Legist.class.isAssignableFrom(clazz)) {
			processKeyValueT015Legist((T015Legist) bean);
		} else if (T014InfoImpart.class.isAssignableFrom(clazz)) {
			processKeyValueT014InfoImpart((T014InfoImpart) bean);
		} else if (T0110AvailFormat.class.isAssignableFrom(clazz)) {
			processKeyValueT0110AvailFormat((T0110AvailFormat) bean);
		} else if (T011ObjLiterature.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjLiterature((T011ObjLiterature) bean);
		} else if (T021Communication.class.isAssignableFrom(clazz)) {
			processKeyValueT021Communication((T021Communication) bean);
		} else if (SpatialRefValue.class.isAssignableFrom(clazz)) {
			processKeyValueSpatialRefValue((SpatialRefValue) bean);
		} else if (T02Address.class.isAssignableFrom(clazz)) {
			processKeyValueT02Address((T02Address) bean);
		} else if (ObjectReference.class.isAssignableFrom(clazz)) {
			processKeyValueObjectReference((ObjectReference) bean);
		} else if (T012ObjAdr.class.isAssignableFrom(clazz)) {
			processKeyValueT012ObjAdr((T012ObjAdr) bean);
		} else if (ObjectConformity.class.isAssignableFrom(clazz)) {
			processKeyValueObjectConformity((ObjectConformity) bean);
		} else if (ObjectAccess.class.isAssignableFrom(clazz)) {
			processKeyValueObjectAccess((ObjectAccess) bean);
		} else if (T011ObjServType.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjServType((T011ObjServType) bean);
		} else if (SearchtermValue.class.isAssignableFrom(clazz)) {
			processKeyValueSearchtermValue((SearchtermValue) bean);
		} else if (T03Catalogue.class.isAssignableFrom(clazz)) {
			processKeyValueT03Catalogue((T03Catalogue) bean);
		} else if (T01Object.class.isAssignableFrom(clazz)) {
			processKeyValueT01Object((T01Object) bean);
		} else if (ObjectDataQuality.class.isAssignableFrom(clazz)) {
			processKeyValueObjectDataQuality((ObjectDataQuality) bean);
		} else if (ObjectFormatInspire.class.isAssignableFrom(clazz)) {
			processKeyValueObjectFormatInspire((ObjectFormatInspire) bean);
		// NOTICE: ALSO ADD NEW CLASSES TO ARRAY keyValueClasses ABOVE !!!!
		// !!! DO NOT FORGET TO ASSURE ACCORDING DAO CAN BE FETCHED VIA DaoFactory.getDao(Class) !!!!

		} else {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
		}

		return bean;
	}
	
	/** Set correct syslist values in servOp according to serv Type (determines syslist) and entry key in servOp. */
	public IEntity processKeyValueT011ObjServOperation(T011ObjServOperation servOp, T011ObjServ serv) {
		Integer servOpKey = servOp.getNameKey();
		if (servOpKey != null && servOpKey > -1) {
			Integer servTypeKey = serv.getTypeKey();
			if (servTypeKey != null) {
				Map<Integer, String> keyNameMap = null;
				if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WMS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_SERV_OPERATION_WMS.getDbValue(),
						catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WFS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_WFS.getDbValue(),
							catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_CSW)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_CSW.getDbValue(),
							catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WCTS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_WCTS.getDbValue(),
							catalogService.getCatalogLanguage());
				}

				if (keyNameMap != null) {
					servOp.setNameValue(keyNameMap.get(servOpKey));
				}
			}
		}

		return servOp;
	}

	/** Set correct syslist values in objServ according to object class (determines syslist) and entry key in objServ. */
	public IEntity processKeyValueT011ObjServ(T011ObjServ objServ, T01Object obj) {
		Integer entryKey = objServ.getTypeKey();
		if (entryKey != null && entryKey > -1) {
			// ServType syslist is dependent from class of object !
			// default is class 3 = "Geodatendienst"
			Integer syslistId = MdekSysList.OBJ_SERV_TYPE.getDbValue();		
			// change syslist if class 6 = "Informationssystem/Dienst/Anwendung"
			if (ObjectType.INFOSYSTEM_DIENST.getDbValue().equals(obj.getObjClass())) {
				syslistId = MdekSysList.OBJ_SERV_TYPE_CLASS_6.getDbValue();
			}

			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				syslistId,
				catalogService.getCatalogLanguage());

			objServ.setTypeValue(keyNameMap.get(entryKey));
		}
		
		return objServ;
	}

	private IEntity processKeyValueT011ObjGeo(T011ObjGeo bean) {
		Integer entryKey = bean.getReferencesystemKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_GEO_REFERENCESYSTEM.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setReferencesystemValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT011ObjGeoSymc(T011ObjGeoSymc bean) {
		Integer entryKey = bean.getSymbolCatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_GEO_SYMC.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSymbolCatValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT011ObjGeoKeyc(T011ObjGeoKeyc bean) {
		Integer entryKey = bean.getKeycKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_GEO_KEYC.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setKeycValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT017UrlRef(T017UrlRef bean) {
		Integer entryKey = bean.getDatatypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.URL_REF_DATATYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setDatatypeValue(keyNameMap.get(entryKey));
		}

		entryKey = bean.getSpecialRef();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.URL_REF_SPECIAL.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSpecialName(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT015Legist(T015Legist bean) {
		Integer entryKey = bean.getLegistKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LEGIST.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setLegistValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT014InfoImpart(T014InfoImpart bean) {
		Integer entryKey = bean.getImpartKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.INFO_IMPART.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setImpartValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT0110AvailFormat(T0110AvailFormat bean) {
		Integer entryKey = bean.getFormatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.AVAIL_FORMAT.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setFormatValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT011ObjLiterature(T011ObjLiterature bean) {
		Integer entryKey = bean.getTypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_LITERATURE_TYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setTypeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT021Communication(T021Communication bean) {
		Integer entryKey = bean.getCommtypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COMM_TYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setCommtypeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueSpatialRefValue(SpatialRefValue bean) {
		Integer entryKey = bean.getNameKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.SPATIAL_REF_VALUE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setNameValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT02Address(T02Address bean) {
		Integer entryKey = bean.getAddressKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.ADDRESS_VALUE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setAddressValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getTitleKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.ADDRESS_TITLE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setTitleValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getCountryKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COUNTRY.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setCountryValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectReference(ObjectReference bean) {
		Integer entryKey = bean.getSpecialRef();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_REFERENCE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSpecialName(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT012ObjAdr(T012ObjAdr bean) {
		Integer sysListKey = bean.getSpecialRef();
		Integer entryKey = bean.getType();

		if (sysListKey != null && sysListKey > -1 &&
				entryKey != null && entryKey > -1)
		{
			Map<Integer, String> keyNameMap = null;
			if (sysListKey.equals(MdekSysList.OBJ_ADR_TYPE.getDbValue())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_ADR_TYPE.getDbValue(),
						catalogService.getCatalogLanguage());
				
			} else if (sysListKey.equals(MdekSysList.OBJ_ADR_TYPE_SPECIAL.getDbValue())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_ADR_TYPE_SPECIAL.getDbValue(),
						catalogService.getCatalogLanguage());				
			}

			if (keyNameMap != null) {
				bean.setSpecialName(keyNameMap.get(entryKey));
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectConformity(ObjectConformity bean) {
		Integer entryKey = bean.getDegreeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_CONFORMITY.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setDegreeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectAccess(ObjectAccess bean) {
		Integer entryKey = bean.getRestrictionKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_ACCESS.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setRestrictionValue(keyNameMap.get(entryKey));
		}

		return bean;
	}

	private IEntity processKeyValueT011ObjServType(T011ObjServType bean) {
		Integer entryKey = bean.getServTypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_SERV_TYPE2.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setServTypeValue(keyNameMap.get(entryKey));
		}

		return bean;
	}

	private IEntity processKeyValueSearchtermValue(SearchtermValue bean) {
		Integer entryKey = bean.getEntryId();
		if (entryKey != null && entryKey > -1) {
			if (SearchtermType.INSPIRE.getDbValue().equals(bean.getType())) {
				Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.INSPIRE_SEARCHTERM.getDbValue(),
						catalogService.getCatalogLanguage());
				bean.setTerm(keyNameMap.get(entryKey));
			}
		}

		return bean;
	}

	private IEntity processKeyValueT03Catalogue(T03Catalogue bean) {
		Integer entryKey = bean.getCountryKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COUNTRY.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setCountryValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setLanguageValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT01Object(T01Object bean) {
		Integer entryKey = bean.getDataLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setDataLanguageValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getMetadataLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setMetadataLanguageValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectDataQuality(ObjectDataQuality bean) {
		Integer dqElemId = bean.getDqElementId();
		Integer entryKey = bean.getNameOfMeasureKey();

		if (dqElemId != null && dqElemId > -1 &&
				entryKey != null && entryKey > -1)
		{
			Map<Integer, String> keyNameMap = null;
			if (dqElemId.equals(MdekSysList.DQ_117_AbsoluteExternalPositionalAccuracy.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_117_AbsoluteExternalPositionalAccuracy.getDbValue(),
						catalogService.getCatalogLanguage());
			} else if (dqElemId.equals(MdekSysList.DQ_109_CompletenessComission.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_109_CompletenessComission.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_110_CompletenessOmission.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_110_CompletenessOmission.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_112_ConceptualConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_112_ConceptualConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_113_DomainConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_113_DomainConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_114_FormatConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_114_FormatConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_126_NonQuantitativeAttributeAccuracy.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_126_NonQuantitativeAttributeAccuracy.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_127_QuantitativeAttributeAccuracy.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_127_QuantitativeAttributeAccuracy.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_120_TemporalConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_120_TemporalConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_125_ThematicClassificationCorrectness.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_125_ThematicClassificationCorrectness.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_115_TopologicalConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_115_TopologicalConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			}

			if (keyNameMap != null) {
				bean.setNameOfMeasureValue(keyNameMap.get(entryKey));
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectFormatInspire(ObjectFormatInspire bean) {
		Integer entryKey = bean.getFormatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_FORMAT_INSPIRE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setFormatValue(keyNameMap.get(entryKey));
		}

		return bean;
	}
}
