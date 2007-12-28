package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.DocToBeanMapper;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.IMapper.MappingQuantity;
import de.ingrid.utils.IngridDocument;

public class MdekIdcJob extends MdekJob {

    /** Logger configured via Properties. ONLY if no logger via logservice is specified
     * for same class !. If Logservice logger is specified, this one uses
     * Logservice configuration -> writes to separate logfile for this Job. */
//    private final static Log log = LogFactory.getLog(MdekTreeJob.class);

	/** logs in separate File (job specific log file) */
	protected Logger log;

	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;

	private BeanToDocMapper beanToDocMapper;
	private DocToBeanMapper docToBeanMapper;

	public MdekIdcJob(ILogService logService,
			DaoFactory daoFactory) {
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekIdcJob.class); 

		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();

		beanToDocMapper = BeanToDocMapper.getInstance();
		docToBeanMapper = docToBeanMapper.getInstance();
	}
/*
	public IngridDocument testMdekEntity(IngridDocument params) {
		IngridDocument result = new IngridDocument();

		// fetch parameters
		String name = (String) params.get(MdekKeys.TITLE);
		String descr = (String) params.get(MdekKeys.ABSTRACT);
		Integer threadNumber = (Integer) params.get("THREAD_NUMBER");

		T01Object objTemplate = new T01Object();
		objTemplate.setObjName(name);
		objTemplate.setObjDescr(descr);
		
		daoT01Object.beginTransaction();

		List<T01Object> objs = daoT01Object.findByExample(objTemplate);

		// thread 1 -> WAIT so we can test staled Object
		if (threadNumber == 1) {
			// wait time in ms
			long waitTime = 1000;
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < waitTime) {
				// do nothing
			}
		}

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		if (objs.size() > 0) {
			for (T01Object o : objs) {
				Integer oClass = o.getObjClass();
				oClass = (oClass == null ? 1 : oClass+1);
				o.setObjClass(oClass);
				
				if (threadNumber == 1) {
					// test update/deletion of staled Object !
		            log.debug("Thread 1 DELETING OBJECT:" + o.getId());
					daoT01Object.makeTransient(o);
//		            log.debug("Thread 1 UPDATE OBJECT:" + o.getId());
//					daoT01Object.makePersistent(o);
				} else {
					daoT01Object.makePersistent(o);
				}
				resultList.add(mapper.mapT01Object(o, MappingQuantity.DETAIL_ENTITY));
			}			
		} else {
			daoT01Object.makePersistent(objTemplate);
			
			T01Object o = daoT01Object.loadById(objTemplate.getId());
			resultList.add(mapper.mapT01Object(o, MappingQuantity.DETAIL_ENTITY));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);

		return result;
	}
*/
	public IngridDocument getTopObjects() {
		IngridDocument result = new IngridDocument();

		daoT01Object.beginTransaction();

		// fetch top Objects
		List<T01Object> objs = daoT01Object.getTopObjects();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		for (T01Object obj : objs) {
			resultList.add(beanToDocMapper.mapT01Object(obj, MappingQuantity.TOP_ENTITY));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT01Object.beginTransaction();

		List<T01Object> objs = daoT01Object.getSubObjects(uuid);

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		for (T01Object obj : objs) {
			resultList.add(beanToDocMapper.mapT01Object(obj, MappingQuantity.SUB_ENTITY));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}


	public IngridDocument getObjDetails(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		return getObjDetails(uuid);
	}
	private IngridDocument getObjDetails(String uuid) {
		daoT01Object.beginTransaction();

		T01Object o = daoT01Object.getObjDetails(uuid);
		IngridDocument oDoc = beanToDocMapper.mapT01Object(o, MappingQuantity.DETAIL_ENTITY);

		daoT01Object.commitTransaction();

		return oDoc;		
	}

	public IngridDocument storeObject(IngridDocument objDoc) {
		String uuid = (String) objDoc.get(MdekKeys.UUID);

		daoT01Object.beginTransaction();

		T01Object o;
		if (uuid == null) {
			// TODO: Create and Set UUID in objDoc
			o = new T01Object();
			docToBeanMapper.mapT01Object(objDoc, o, MappingQuantity.DETAIL_ENTITY);

		} else {
			o = daoT01Object.getObjDetails(uuid);
			docToBeanMapper.mapT01Object(objDoc, o, MappingQuantity.DETAIL_ENTITY);
		}
		daoT01Object.makePersistent(o);

		daoT01Object.commitTransaction();
		
		return getObjDetails(uuid);
	}

	public IngridDocument deleteObject(IngridDocument params) {
		String uuid = (String) params.get(MdekKeys.UUID);
		IngridDocument result = null;

		daoT01Object.beginTransaction();

		T01Object oExample = new T01Object();
		oExample.setObjUuid(uuid);
		T01Object o = daoT01Object.findUniqueByExample(oExample);

		if (o != null) {
			daoT01Object.makeTransient(o);
			// TODO: wie delete success in Result transportieren ? jetzt null / not null
			result = new IngridDocument();
		}

		daoT01Object.commitTransaction();

		return result;		
	}

/*
	public IngridDocument getTopAddresses() {
		IngridDocument result = new IngridDocument();

		daoT02Address.beginTransaction();

		// fetch top Addresses
		List<T02Address> adrs = daoT02Address.getTopAddresses();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		for (T02Address adr : adrs) {
			resultList.add(mapper.mapT02Address(adr, MappingType.TOP_ENTITY));
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT02Address.beginTransaction();

		Set<T02Address> adrs = daoT02Address.getSubAddresses(uuid);

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		for (T02Address adr : adrs) {
			resultList.add(mapper.mapT02Address(adr, MappingType.SUB_ENTITY));
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}
*/
}
