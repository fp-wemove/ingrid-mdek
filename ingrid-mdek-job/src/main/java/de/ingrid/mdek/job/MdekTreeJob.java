package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.model.BeanToDocMapper;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

public class MdekTreeJob extends MdekJob {

    /** Logger configured via Properties. ONLY if no logger via logservice is specified
     * for same class !. If Logservice logger is specified, this one uses
     * Logservice configuration -> writes to separate logfile for this Job. */
//    private final static Log log = LogFactory.getLog(MdekTreeJob.class);

	/** logs in separate File (job specific log file) */
	protected Logger log;

	private IT01ObjectDao daoT01Object;
	private IT02AddressDao daoT02Address;

	public MdekTreeJob(ILogService logService,
			DaoFactory daoFactory) {
		
		// use logger from service -> logs into separate file !
		log = logService.getLogger(MdekTreeJob.class); 

		daoT01Object = daoFactory.getT01ObjectDao();
		daoT02Address = daoFactory.getT02AddressDao();
	}

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
				resultList.add(mapper.mapT01Object(o));
			}			
		} else {
			daoT01Object.makePersistent(objTemplate);
			
			T01Object o = daoT01Object.loadById(objTemplate.getId());
			resultList.add(mapper.mapT01Object(o));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);

		return result;
	}

	public IngridDocument getTopObjects() {
		IngridDocument result = new IngridDocument();

		daoT01Object.beginTransaction();

		// fetch top Objects
		List<T01Object> objs = daoT01Object.getTopObjects();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(objs.size());
		Iterator iter = objs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T01Object obj = (T01Object)iter.next();
			IngridDocument doc = mapper.mapT01Object(obj);
			boolean hasChild = false;
			// NOTICE: Causes another select !
			if (obj.getT012ObjObjs().size() > 0) {
				hasChild = true;
			}
			doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(doc);
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubObjects(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT01Object.beginTransaction();

		T01Object o = daoT01Object.getObjectWithSubObjects(uuid);
		if (log.isDebugEnabled()) {
			log.debug("Fetched T01Object with SubObjects: " + o);			
		}

		Set subObjs = o.getT012ObjObjs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subObjs.size());
		Iterator iter = subObjs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T01Object subObj = (T01Object)iter.next();
			IngridDocument subDoc = mapper.mapT01Object(subObj);
			boolean hasChild = false;
			if (subObj.getT012ObjObjs().size() > 0) {
				hasChild = true;
			}
			subDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(subDoc);
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.OBJ_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getTopAddresses() {
		IngridDocument result = new IngridDocument();

		daoT02Address.beginTransaction();

		// fetch top Addresses
		List<T02Address> adrs = daoT02Address.getTopAddresses();

		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(adrs.size());
		Iterator iter = adrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T02Address adr = (T02Address)iter.next();
			IngridDocument doc = mapper.mapT02Address(adr);
			boolean hasChild = false;
			// NOTICE: Causes another select !
			if (adr.getT022AdrAdrs().size() > 0) {
				hasChild = true;
			}
			doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(doc);
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getSubAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT02Address.beginTransaction();

		T02Address a = daoT02Address.getAddressWithSubAddresses(uuid);
		if (log.isDebugEnabled()) {
			log.debug("Fetched T02Address with SubAddresses: " + a);			
		}

		Set subAdrs = a.getT022AdrAdrs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subAdrs.size());
		Iterator iter = subAdrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			T02Address subAdr = (T02Address)iter.next();
			IngridDocument subDoc = mapper.mapT02Address(subAdr);
			boolean hasChild = false;
			if (subAdr.getT022AdrAdrs().size() > 0) {
				hasChild = true;
			}
			subDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			resultList.add(subDoc);
		}

		daoT02Address.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}

	public IngridDocument getObjDetails(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		return result;
	}

	public IngridDocument getObjAddresses(IngridDocument params) {
		IngridDocument result = new IngridDocument();
		String uuid = (String) params.get(MdekKeys.UUID);

		daoT01Object.beginTransaction();

		T01Object o = daoT01Object.getObjWithAddresses(uuid);
		if (log.isDebugEnabled()) {
			log.debug("Fetched T01Object with Addresses: " + o);			
		}

		Set subAdrs = o.getT012ObjAdrs();
		ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subAdrs.size());
		Iterator iter = subAdrs.iterator();
		BeanToDocMapper mapper = BeanToDocMapper.getInstance();
		while (iter.hasNext()) {
			resultList.add(mapper.mapT02Address((T02Address)iter.next()));
		}

		daoT01Object.commitTransaction();

		result.put(MdekKeys.ADR_ENTITIES, resultList);
		return result;
	}
}
