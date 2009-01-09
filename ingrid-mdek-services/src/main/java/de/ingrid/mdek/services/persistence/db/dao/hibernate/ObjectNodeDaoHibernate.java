package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.ExpiryState;
import de.ingrid.mdek.MdekUtils.IdcChildrenSelectionType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.mdek.services.utils.MdekTreePathHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Hibernate-specific implementation of the <tt>IObjectNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class ObjectNodeDaoHibernate
	extends GenericHibernateDao<ObjectNode>
	implements  IObjectNodeDao, IFullIndexAccess {

	private static final Logger LOG = Logger.getLogger(ObjectNodeDaoHibernate.class);

    public ObjectNodeDaoHibernate(SessionFactory factory) {
        super(factory, ObjectNode.class);
    }

	public ObjectNode loadByUuid(String uuid, IdcEntityVersion whichEntityVersion) {
		if (uuid == null) {
			return null;
		}

		Session session = getSession();

		String qString = "from ObjectNode oNode ";
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectWork ";			
		}
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectPublished ";			
		}
		qString += "where oNode.objUuid = ?";

		ObjectNode oN = (ObjectNode) session.createQuery(qString)
			.setString(0, uuid)
			.uniqueResult();

		return oN;
	}

	public ObjectNode loadByOrigId(String origId, IdcEntityVersion whichEntityVersion) {
		if (origId == null) {
			return null;
		}

		Session session = getSession();

		// always fetch working version. Is needed for querying, so we fetch it.
		String qString = "select distinct oNode from ObjectNode oNode " +
				"left join fetch oNode.t01ObjectWork oWork ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			qString += "left join fetch oNode.t01ObjectPublished ";			
		}
		qString += "where oWork.orgObjId = ? ";
		// order to guarantee always same node in front if multiple nodes with same orig id ! 
		qString += "order by oNode.objUuid";

		List<ObjectNode> oNodes = session.createQuery(qString)
			.setString(0, origId)
			.list();

		ObjectNode retNode = null;
		String nodeUuids = "";
		for (ObjectNode oNode : oNodes) {
			if (retNode == null) {
				retNode = oNode;
			}
			nodeUuids += "\n     " + oNode.getObjUuid();
		}
		if (oNodes.size() > 1) {
			LOG.warn("MULTIPLE NODES WITH SAME ORIG_ID: " + origId + " ! Nodes:" + nodeUuids);
		}

		return retNode;
	}

	public List<ObjectNode> getTopObjects(IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.fkObjUuid is null ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName"; 
			}
		}
		
		List<ObjectNode> oNodes = session.createQuery(q).list();

		return oNodes;
	}

	public List<ObjectNode> getSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.fkObjUuid = ? ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName"; 
			}
		}
		
		List<ObjectNode> oNodes = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		return oNodes;
	}

	public List<ObjectNode> getAllSubObjects(String parentUuid,
			IdcEntityVersion whichEntityVersion,
			boolean fetchSubNodesChildren) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		String objAlias = "?";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oPub";
			q += "left join fetch oNode.t01ObjectPublished " + objAlias + " ";
		}
		if (whichEntityVersion == IdcEntityVersion.WORKING_VERSION || 
			whichEntityVersion == IdcEntityVersion.ALL_VERSIONS) {
			objAlias = "oWork";
			q += "left join fetch oNode.t01ObjectWork " + objAlias + " ";
		}
		if (fetchSubNodesChildren) {
			q += "left join fetch oNode.objectNodeChildren ";
		}
		q += "where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";

		if (whichEntityVersion != null) {
			// only order if only ONE version requested
			if (whichEntityVersion != IdcEntityVersion.ALL_VERSIONS) {
				q += "order by " + objAlias + ".objName"; 
			}
		}
		
		List<ObjectNode> oNodes = session.createQuery(q)
				.list();

		return oNodes;
	}

	public List<ObjectNode> getSelectedSubObjects(String parentUuid,
			IdcChildrenSelectionType whichChildren,
			PublishType parentPubType) {
		Session session = getSession();

		String q = "select distinct oNode from ObjectNode oNode ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "left join fetch oNode.t01ObjectPublished o ";
		}
		q += "where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' ";
		if (whichChildren == IdcChildrenSelectionType.PUBLICATION_CONDITION_PROBLEMATIC) {
			q += "and o.publishId < " + parentPubType.getDbValue();
		}
		
		List<ObjectNode> oNodes = session.createQuery(q)
				.list();

		return oNodes;
	}

	public int countAllSubObjects(String parentUuid, IdcEntityVersion versionOfSubObjectsToCount) {
		Session session = getSession();
		
		String q = "select count(oNode) " +
			"from ObjectNode oNode " +
			"where oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%'";

		if (versionOfSubObjectsToCount == IdcEntityVersion.WORKING_VERSION) {
			q += " and oNode.objId != oNode.objIdPublished ";

		} else if (versionOfSubObjectsToCount == IdcEntityVersion.PUBLISHED_VERSION) {
			q += " and oNode.objIdPublished is not null";			
		}
		
		Long totalNum = (Long) session.createQuery(q).uniqueResult();
		
		return totalNum.intValue();
	}

	public boolean isSubNode(String uuidToCheck, String uuidParent) {
		boolean isSubNode = false;

		List<String> path = getObjectPath(uuidToCheck);
		
		if (path != null) {
			if (path.contains(uuidParent)) {
				isSubNode = true;
			}
		}
		
		return isSubNode;
	}
	
	public ObjectNode getParent(String uuid) {
		ObjectNode parentNode = null;
		ObjectNode oN = loadByUuid(uuid, null);
		if (oN != null && oN.getFkObjUuid() != null) {
			parentNode = loadByUuid(oN.getFkObjUuid(), null);
		}
		
		return parentNode;
	}

	public ObjectNode getObjDetails(String uuid) {
		return getObjDetails(uuid, IdcEntityVersion.WORKING_VERSION);
	}

	public ObjectNode getObjDetails(String uuid, IdcEntityVersion whichEntityVersion) {
		Session session = getSession();

		String q = "from ObjectNode oNode ";
		if (whichEntityVersion == IdcEntityVersion.PUBLISHED_VERSION) {
			q += "left join fetch oNode.t01ObjectPublished o ";			
		} else {
			q += "left join fetch oNode.t01ObjectWork o ";			
		}
		q += 
		// referenced objects (to) 
			"left join fetch o.objectReferences oRef " +
			"left join fetch oRef.objectNode oRefNode " +
			"left join fetch oRefNode.t01ObjectWork oRefObj " +

// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !

		// referenced addresses
//			"left join fetch o.t012ObjAdrs objAdr " +
//			"left join fetch objAdr.addressNode aNode " +
//			"left join fetch aNode.t02AddressWork aWork " +
//			"left join fetch aWork.t021Communications aComm " +
		// spatial references 
//			"left join fetch o.spatialReferences spatRef " +
//			"left join fetch spatRef.spatialRefValue spatialRefVal " +
//			"left join fetch spatialRefVal.spatialRefSns " +
		// url refs 
//			"left join fetch o.t017UrlRefs urlRef " +
			"where oNode.objUuid = ?";

		// enable address filter ?
//		session.enableFilter("t012ObjAdrFilter").setParameter("type", 1);
		
		// fetch all at once (one select with outer joins)
		ObjectNode oN = (ObjectNode) session.createQuery(q)
			.setString(0, uuid)
			.uniqueResult();

//		session.disableFilter("t012ObjAdrFilter");

		return oN;
	}

	public List<ObjectNode>[] getObjectReferencesFrom(String uuid) {
		Session session = getSession();

		// first select all references from working copies (node ids)
		// NOTICE: working copy == published one if not "in Bearbeitung" !
		List<Long> nodeIdsWork = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectWork oWork " +
				"left join oWork.objectReferences oRef " +
				"where oRef.objToUuid = ?")
				.setString(0, uuid)
				.list();

		// then select all references from published ones
		List<Long> nodeIdsPub = session.createQuery(
				"select distinct oNode.id from ObjectNode oNode " +
				"left join oNode.t01ObjectPublished oPub " +
				"left join oPub.objectReferences oRef " +
				"where oRef.objToUuid = ?")
				.setString(0, uuid)
				.list();

		// then remove all published references also contained in working references.
		// we get the ones only in published version, meaning they were deleted in the
		// working copies !
		List<Long> nodeIdsPubOnly = new ArrayList<Long>();
		for (Long idPub : nodeIdsPub) {
			if (!nodeIdsWork.contains(idPub)) {
				nodeIdsPubOnly.add(idPub);
			}
		}
		
		// fetch all "nodes with work references"
		List<ObjectNode> nodesWork = new ArrayList<ObjectNode>();
		if (nodeIdsWork.size() > 0) {
			nodesWork = session.createQuery(
					"select distinct oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectWork oWork " +
					"where oNode.id in (:idList)")
					.setParameterList("idList", nodeIdsWork)
					.list();			
		}

		// fetch all "nodes with only publish references"
		List<ObjectNode> nodesPubOnly = new ArrayList<ObjectNode>();
		if (nodeIdsPubOnly.size() > 0) {
			nodesPubOnly = session.createQuery(
					"select distinct oNode from ObjectNode oNode " +
					"left join fetch oNode.t01ObjectPublished oPub " +
					"where oNode.id in (:idList)")
					.setParameterList("idList", nodeIdsPubOnly)
					.list();			
		}
		
		List<ObjectNode>[] retObjects = new List[] {
			nodesPubOnly,
			nodesWork
		};

		return retObjects;
	}

	public List<String> getObjectPath(String uuid) {
		ArrayList<String> uuidList = new ArrayList<String>();
		while(uuid != null) {
			ObjectNode oN = loadByUuid(uuid, null);
			if (oN == null) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));
			}
			uuidList.add(0, uuid);
			uuid = oN.getFkObjUuid();
		}

		return uuidList;
	}

	public long queryObjectsThesaurusTermTotalNum(String termSnsId) {

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<ObjectNode> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		String qString = createThesaurusQueryString(termSnsId);
		
		if (qString == null) {
			return retList;
		}

		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
	}
	
	/**
	 * Create basic query string for querying objects associated with passed thesaurus term.
	 * @param termSnsId sns id of thesaurus term
	 * @param isCountQuery<br>
	 * 		true=create query for counting total results<br>
	 * 		false=create query for fetching results
	 * @return basic query string or null if no parameters. 
	 */
	private String createThesaurusQueryString(String termSnsId) {
		termSnsId = MdekUtils.processStringParameter(termSnsId);

		if (termSnsId == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join obj.searchtermObjs termObjs " +
			"inner join termObjs.searchtermValue termVal " +
			"inner join termVal.searchtermSns termSns " +
			"where " +
			"termSns.snsId = '" + termSnsId + "'";
		
		return qString;
	}

	public long queryObjectsFullTextTotalNum(String searchTerm) {

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	public List<ObjectNode> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		List<ObjectNode> retList = new ArrayList<ObjectNode>();

		String qString = createFullTextQueryString(searchTerm);
		
		if (qString == null) {
			return retList;
		}

		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";

		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
	}
	
	public List<ObjectNode> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		
		List<ObjectNode> retList = new ArrayList<ObjectNode>();
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);
		
		qString = "select distinct oNode " + qString;
		qString += " order by obj.objClass, obj.objName";
		
		Session session = getSession();

		retList = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		return retList;
		
	}
	
	public long queryObjectsExtendedTotalNum(IngridDocument searchParams) {
		
		// create hql from queryParams
		String qString = ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);
		
		if (qString == null) {
			return 0;
		}

		qString = "select count(distinct oNode) " + qString;

		Session session = getSession();

		Long totalNum = (Long) session.createQuery(qString)
			.uniqueResult();

		return totalNum;
	}

	/**
	 * Create basic query string for querying addresses concerning full text.
	 * @param searchTerm term to search for
	 * @return basic query string or null if no parameters. 
	 */
	private String createFullTextQueryString(String searchTerm) {
		searchTerm = MdekUtils.processStringParameter(searchTerm);

		if (searchTerm == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from ObjectNode oNode " +
			"inner join oNode.t01ObjectWork obj " +
			"inner join oNode.fullIndexObjs fidx " +
			"where " +
			"fidx.idxName = '" + IDX_NAME_FULLTEXT + "' " +
			"and fidx.idxValue like '%" + searchTerm + "%'";

		return qString;
	}

	public List<T01Object> getAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		List<T01Object> retList = new ArrayList<T01Object>();

		Session session = getSession();

		retList = session.createQuery("select distinct o " +
			"from T01Object o " +
			"where o.responsibleUuid = ?")
			.setString(0, responsibleUserUuid)
			.list();

		return retList;
	}

	public IngridDocument getWorkObjects(String userUuid,
			IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, new Long(0));
		defaultResult.put(MdekKeys.OBJ_ENTITIES, new ArrayList<ObjectNode>());
		
		if (selectionType == IdcWorkEntitiesSelectionType.EXPIRED) {
			return getWorkObjectsExpired(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.MODIFIED) {
			return getWorkObjectsModified(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.IN_QA_WORKFLOW) {
			return getWorkObjectsInQAWorkflow(userUuid, orderBy, orderAsc, startHit, numHits);
		} else 	if (selectionType == IdcWorkEntitiesSelectionType.PORTAL_QUICKLIST) {
			return getWorkObjectsPortalQuicklist(userUuid, startHit, numHits);
		}

		return defaultResult;
	}

	/** NOTICE: queries PUBLISHED version because mod-date of published one is displayed ! */
	private IngridDocument getWorkObjectsExpired(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"o.responsibleUuid = '"+ userUuid +"' " +
			"and oMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectPublished o " +
				"inner join o.objectMetadata oMeta " + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectPublished o " +
				"inner join fetch o.objectMetadata oMeta " +
				"left join fetch o.addressNodeMod aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);
		
		return result;
	}

	private IngridDocument getWorkObjectsModified(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"o.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' " +
			"and (o.modUuid = '" + userUuid + "' or o.responsibleUuid = '" + userUuid + "') ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"left join fetch o.addressNodeMod aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is name
		String qOrderBy = " order by o.objName ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.objName ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.objName ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects: " + qStringCount);
		}
		Long totalNum = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);
		
		return result;
	}

	private IngridDocument getWorkObjectsInQAWorkflow(String userUuid,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteriaUser = "and (oMeta.assignerUuid = '" + userUuid + "' or o.responsibleUuid = '" + userUuid + "') ";
		String qCriteria = " where " +
			"(o.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' or " +
				"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " + qCriteriaUser;
		String qCriteriaAssigned = " where " +
			"o.workState = '" + WorkState.QS_UEBERWIESEN.getDbValue() + "' " + qCriteriaUser;
		String qCriteriaReassigned = " where " +
			"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "' " + qCriteriaUser;

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " +
				"inner join o.objectMetadata oMeta ";

		// query string for fetching results ! 
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"inner join fetch o.objectMetadata oMeta " +
				"left join fetch oMeta.addressNodeAssigner aNode " +
				"left join fetch aNode.t02AddressWork a " + qCriteria;

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.STATE) {
			qOrderBy = " order by o.workState ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;
		
		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects \"QA\": " + qStringCount + qCriteria);
			LOG.debug("HQL Counting WORK objects \"QA ASSIGNED\": " + qStringCount + qCriteriaAssigned);
			LOG.debug("HQL Counting WORK objects \"QA REASSIGNED\": " + qStringCount + qCriteriaReassigned);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount + qCriteria).uniqueResult();
		Long totalNumAssigned = (Long) session.createQuery(qStringCount + qCriteriaAssigned).uniqueResult();
		Long totalNumReassigned = (Long) session.createQuery(qStringCount + qCriteriaReassigned).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.TOTAL_NUM_QA_ASSIGNED, totalNumAssigned);
		result.put(MdekKeys.TOTAL_NUM_QA_REASSIGNED, totalNumReassigned);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);
		
		return result;
	}

	private IngridDocument getWorkObjectsPortalQuicklist(String userUuid,
			int startHit, int numHits) {
		Session session = getSession();

		// prepare queries

		// selection criteria
		String qCriteria = " where " +
			"(o.workState = '" + WorkState.IN_BEARBEITUNG.getDbValue() + "' or " +
				"o.workState = '" + WorkState.QS_RUECKUEBERWIESEN.getDbValue() + "') " +
			"and (oMeta.assignerUuid = '" + userUuid + "' or o.modUuid = '" + userUuid + "') ";

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectWork o " +
				"inner join o.objectMetadata oMeta " + qCriteria;

		// query string for fetching results ! 
		String qStringSelect = "from ObjectNode oNode " +
				"inner join fetch oNode.t01ObjectWork o " +
				"inner join fetch o.objectMetadata oMeta " + qCriteria;

		// always order by date
		String qOrderBy = " order by o.modTime desc";
		qStringSelect += qOrderBy;
		
		// first count total numbers
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting WORK objects \"QA\": " + qStringCount);
		}
		Long totalNumPaging = (Long) session.createQuery(qStringCount).uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching WORK objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = session.createQuery(qStringSelect)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// return results
		IngridDocument result = new IngridDocument();
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumPaging);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);
		
		return result;
	}

	public IngridDocument getQAObjects(String userUuid, boolean isCatAdmin, MdekPermissionHandler permHandler,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {

		// default result
		IngridDocument defaultResult = new IngridDocument();
		defaultResult.put(MdekKeys.TOTAL_NUM_PAGING, new Long(0));
		defaultResult.put(MdekKeys.OBJ_ENTITIES, new ArrayList<ObjectNode>());
		
		// check whether QA user
		if (!permHandler.hasQAPermission(userUuid)) {
			return defaultResult;
		}

		if (isCatAdmin) {
			return getQAObjects(null, null, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);
		} else {
			return getQAObjectsViaGroup(userUuid, whichWorkState, selectionType, orderBy, orderAsc, startHit, numHits);			
		}
	}

	/**
	 * QA PAGE: Get ALL Objects where given user is QA and objects WORKING VERSION match passed selection criteria.
	 * The QA objects are determined via assigned objects in QA group of user.
	 * All sub-objects of "write-tree" objects are included !
	 * We return nodes, so we can evaluate whether published version exists ! 
	 * @param userUuid QA user
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAObjectsViaGroup(String userUuid,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		Session session = getSession();

		// select all objects in QA group (write permission) !
		// NOTICE: this doesn't include sub objects of "write-tree" objects !
		String qString = "select distinct pObj.uuid, p2.action as perm " +
		"from " +
			"IdcUser usr, " +
			"IdcGroup grp, " +
			"IdcUserPermission pUsr, " +
			"Permission p1, " +
			"PermissionObj pObj, " +
			"Permission p2 " +
		"where " +
			// user -> grp -> QA
			"usr.addrUuid = '" + userUuid + "'" +
			" and usr.idcGroupId = grp.id" +
			" and grp.id = pUsr.idcGroupId " +
			" and pUsr.permissionId = p1.id " +
			" and p1.action = '" + IdcPermission.QUALITY_ASSURANCE.getDbValue() + "'" +
			// grp -> object -> permission
			" and grp.id = pObj.idcGroupId " +
			" and pObj.permissionId = p2.id";

		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Selecting objects in QA group: " + qString);
		}
		List<Object[]> groupObjsAndPerms = session.createQuery(qString).list();

		// parse group objects and separate "write single" and "write tree"
		List<String> objUuidsWriteSingle = new ArrayList<String>();
		List<String> objUuidsWriteTree = new ArrayList<String>();
		for (Object[] groupObjAndPerm : groupObjsAndPerms) {
			String oUuid = (String) groupObjAndPerm[0];
			IdcPermission p = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, groupObjAndPerm[1]);

			if (p == IdcPermission.WRITE_SINGLE) {
				objUuidsWriteSingle.add(oUuid);
			} else if (p == IdcPermission.WRITE_TREE) {
				objUuidsWriteTree.add(oUuid);
			}
		}

		return getQAObjects(objUuidsWriteSingle,
				objUuidsWriteTree,
				whichWorkState, selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}

	/**
	 * QA PAGE: Get ALL Objects where user has write permission matching passed selection criteria
	 * We return nodes, so we can evaluate whether published version exists !
	 * @param objUuidsWriteSingle list of object uuids where user has single write permission, pass null if all objects
	 * @param objUuidsWriteTree list of object uuids where user has tree write permission, pass null if all objects
	 * @param whichWorkState only return objects in this work state, pass null if workstate should be ignored
	 * @param selectionType further selection criteria (see Enum), pass null if no further criteria
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 * @return doc encapsulating total number for paging and list of nodes
	 */
	private IngridDocument getQAObjects(List<String> objUuidsWriteSingle,
			List<String> objUuidsWriteTree,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		// first check content of lists and set to null if no content (to be used as flag)
		if (objUuidsWriteSingle != null && objUuidsWriteSingle.size() == 0) {
			objUuidsWriteSingle = null;
		}
		if (objUuidsWriteTree != null && objUuidsWriteTree.size() == 0) {
			objUuidsWriteTree = null;
		}
		
		Session session = getSession();

		// prepare queries

		// query string for counting -> without fetch (fetching not possible)
		String qStringCount = "select count(oNode) " +
			"from ObjectNode oNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringCount += "inner join oNode.t01ObjectPublished o ";
		} else {
			qStringCount += "inner join oNode.t01ObjectWork o ";			
		}
		qStringCount += "inner join o.objectMetadata oMeta ";


		// with fetch: always fetch object and metadata, e.g. needed when mapping user operation (mark deleted) 
		String qStringSelect = "from ObjectNode oNode ";
		if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
			// queries PUBLISHED version because mod-date of published one is displayed !
			qStringSelect += "inner join fetch oNode.t01ObjectPublished o ";
		} else {
			qStringSelect += "inner join fetch oNode.t01ObjectWork o ";			
		}
		qStringSelect += "inner join fetch o.objectMetadata oMeta ";
		if (whichWorkState == WorkState.QS_UEBERWIESEN) {
			qStringSelect += "inner join fetch oMeta.addressNodeAssigner aNode ";
		} else {
			qStringSelect += "inner join fetch o.addressNodeMod aNode ";
		}
		qStringSelect += "inner join fetch aNode.t02AddressWork a ";

		// selection criteria
		if (whichWorkState != null || selectionType != null ||
				objUuidsWriteSingle != null || objUuidsWriteTree != null) {
			String qStringCriteria = " where ";

			boolean addAnd = false;

			if (whichWorkState != null) {
				qStringCriteria += "o.workState = '" + whichWorkState.getDbValue() + "'";
				addAnd = true;
			}

			if (selectionType != null) {
				if (addAnd) {
					qStringCriteria += " and ";
				}
				if (selectionType == IdcQAEntitiesSelectionType.EXPIRED) {
					qStringCriteria += "oMeta.expiryState = " + ExpiryState.EXPIRED.getDbValue();
				} else if (selectionType == IdcQAEntitiesSelectionType.SPATIAL_RELATIONS_UPDATED) {
					// TODO: Add when implementing catalog management sns update !
					return result;
				} else {
					// QASelectionType not handled ? return nothing !
					return result;
				}
				addAnd = true;
			}
			
			if (objUuidsWriteSingle != null || objUuidsWriteTree != null) {
				if (addAnd) {
					qStringCriteria += " and ( ";
				}

				// WRITE SINGLE 
				// add all write tree nodes to single nodes
				// -> top nodes of branch have to be selected in same way as write single objects
				if (objUuidsWriteSingle == null) {
					objUuidsWriteSingle = new ArrayList<String>();
				}
				if (objUuidsWriteTree != null) {
					objUuidsWriteSingle.addAll(objUuidsWriteTree);
				}

				qStringCriteria += " oNode.objUuid in (:singleUuidList) ";

				// WRITE TREE 
				if (objUuidsWriteTree != null) {
					qStringCriteria += " or ( ";

					boolean start = true;
					for (String oUuid : objUuidsWriteTree) {
						if (!start) {
							qStringCriteria += " or ";							
						}
						qStringCriteria += 
							" oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(oUuid) + "%' ";
						start = false;
					}
					qStringCriteria += " ) ";
				}
				
				if (addAnd) {
					qStringCriteria += " ) ";
				}
				addAnd = true;
			}
			
			qStringCount += qStringCriteria;
			qStringSelect += qStringCriteria;
		}

		// order by: default is date
		String qOrderBy = " order by o.modTime ";
		if (orderBy == IdcEntityOrderBy.CLASS) {
			qOrderBy = " order by o.objClass ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.NAME) {
			qOrderBy = " order by o.objName ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", o.modTime ";
		} else  if (orderBy == IdcEntityOrderBy.USER) {
			qOrderBy = " order by a.institution ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.lastname ";
			qOrderBy += orderAsc ? " asc " : " desc ";
			qOrderBy += ", a.firstname ";
			qOrderBy += orderAsc ? " asc " : " desc ";				
			qOrderBy += ", o.modTime ";
		}
		qOrderBy += orderAsc ? " asc " : " desc ";

		qStringSelect += qOrderBy;

		// set query parameters 
		Query qCount = session.createQuery(qStringCount);
		Query qSelect = session.createQuery(qStringSelect);
		if (objUuidsWriteSingle != null) {
			qCount.setParameterList("singleUuidList", objUuidsWriteSingle);
			qSelect.setParameterList("singleUuidList", objUuidsWriteSingle);
		}

		// first count total number
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Counting QA objects: " + qStringCount);
		}
		Long totalNum = (Long) qCount.uniqueResult();

		// then fetch requested entities
		if (LOG.isDebugEnabled()) {
			LOG.debug("HQL Fetching QA objects: " + qStringSelect);
		}
		List<ObjectNode> oNodes = qSelect.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();
	
		// and return results
		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNum);
		result.put(MdekKeys.OBJ_ENTITIES, oNodes);
		
		return result;
	}

	public IngridDocument getObjectStatistics(String parentUuid,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits) {
		IngridDocument result = new IngridDocument();

		if (selectionType == IdcStatisticsSelectionType.CLASSES_AND_STATES) {
			result = getObjectStatistics_classesAndStates(parentUuid);

		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE ||
				selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			result = getObjectStatistics_searchterms(parentUuid, startHit, numHits, selectionType);
		}
		
		return result;
	}
	
	private IngridDocument getObjectStatistics_classesAndStates(String parentUuid) {
		IngridDocument result = new IngridDocument();
		
		Session session = getSession();

		// prepare query
		String qString = "select count(distinct oNode) " +
			"from " +
				"ObjectNode oNode " +
				"inner join oNode.t01ObjectWork obj " +
			"where ";
		if (parentUuid != null) {
			// NOTICE: tree path in node doesn't contain node itself
			qString += "(oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' " +
				"OR oNode.objUuid = '" + parentUuid + "') " +
				"AND ";
		}

		// fetch number of objects of specific class and work state
		Object[] objClasses = EnumUtil.getDbValues(ObjectType.class);
		Object[] workStates = EnumUtil.getDbValues(WorkState.class);
		Long totalNum;
		for (Object objClass : objClasses) {
			IngridDocument classMap = new IngridDocument();

			// get total number of entities of given class underneath parent
			String qStringClass = qString +	" obj.objClass = " + objClass;
			totalNum = (Long) session.createQuery(qStringClass).uniqueResult();
			
			classMap.put(MdekKeys.TOTAL_NUM, totalNum);
			
			// add number of different work states
			for (Object workState : workStates) {
				// get total number of entities of given work state
				String qStringState = qStringClass + " AND obj.workState = '" + workState + "'";
				totalNum = (Long) session.createQuery(qStringState).uniqueResult();

				classMap.put(workState, totalNum);
			}

			result.put(objClass, classMap);
		}

		return result;
	}

	private IngridDocument getObjectStatistics_searchterms(String parentUuid,
			int startHit, int numHits,
			IdcStatisticsSelectionType selectionType) {

		IngridDocument result = new IngridDocument();
		
		Session session = getSession();

		// basics for queries to execute

		String qStringFromWhere = "from " +
				"ObjectNode oNode " +
				"inner join oNode.t01ObjectWork obj " +
				"inner join obj.searchtermObjs searchtObj " +
				"inner join searchtObj.searchtermValue searchtVal " +
			"where ";
		if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_FREE) {
			qStringFromWhere += " searchtVal.type = '" + SearchtermType.FREI.getDbValue() + "' ";			
		} else if (selectionType == IdcStatisticsSelectionType.SEARCHTERMS_THESAURUS) {
			qStringFromWhere += " searchtVal.type = '" + SearchtermType.THESAURUS.getDbValue() + "' ";			
		}

		if (parentUuid != null) {
			// NOTICE: tree path in node doesn't contain node itself
			qStringFromWhere += " AND (oNode.treePath like '%" + MdekTreePathHandler.translateToTreePathUuid(parentUuid) + "%' " +
				"OR oNode.objUuid = '" + parentUuid + "') ";
		}

		// first count number of assigned search terms
		String qString = "select count(searchtVal.term) " +
			qStringFromWhere;
		Long totalNumSearchtermsAssigned = (Long) session.createQuery(qString).uniqueResult();

		// then count number of distinct search terms for paging
		qString = "select count(distinct searchtVal.term) " +
			qStringFromWhere;
		Long totalNumSearchtermsPaging = (Long) session.createQuery(qString).uniqueResult();

		// then count every searchterm
		qString = "select searchtVal.term, count(searchtVal.term) " +
			qStringFromWhere;
		qString += " group by searchtVal.term " +
			// NOTICE: in order clause: use of alias for count causes HQL error !
			// use of same count expression in order causes error on mySql 4 !
			// use of integer for which select attribute works ! 
			"order by 2 desc, searchtVal.term";

//		if (LOG.isDebugEnabled()) {
//			LOG.debug("Executing HQL: " + qString);
//		}

		List hits = session.createQuery(qString)
			.setFirstResult(startHit)
			.setMaxResults(numHits)
			.list();

		ArrayList<IngridDocument> termDocs = new ArrayList<IngridDocument>();
		for (Object hit : hits) {
			Object[] objs = (Object[]) hit;

			IngridDocument termDoc = new IngridDocument();
			termDoc.put(MdekKeys.TERM_NAME, objs[0]);
			termDoc.put(MdekKeys.TOTAL_NUM, objs[1]);
			
			termDocs.add(termDoc);
		}

		result.put(MdekKeys.TOTAL_NUM_PAGING, totalNumSearchtermsPaging);
		result.put(MdekKeys.TOTAL_NUM, totalNumSearchtermsAssigned);
		result.put(MdekKeys.STATISTICS_SEARCHTERM_LIST, termDocs);

		return result;
	}

	public List<String> getObjectUuidsForExport(String exportCriterion) {
		Session session = getSession();

		String q = "select distinct oNode.objUuid " +
			"from ObjectNode oNode " +
				"inner join oNode.t01ObjectPublished o " +
				"inner join o.t014InfoImparts oExp " +
			"where oExp.impartValue = ?";
		
		List<String> oNodes = session.createQuery(q)
				.setString(0, exportCriterion)
				.list();

		return oNodes;
	}
}
