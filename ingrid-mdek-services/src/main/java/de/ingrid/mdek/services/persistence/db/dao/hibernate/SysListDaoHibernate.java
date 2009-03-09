package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;

/**
 * Hibernate-specific implementation of the <tt>ISysListDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysListDaoHibernate
	extends GenericHibernateDao<SysList>
	implements  ISysListDao {

    public SysListDaoHibernate(SessionFactory factory) {
        super(factory, SysList.class);
    }

	public List<Integer> getSysListIds() {
		Session session = getSession();

		String qString = "select distinct lstId from SysList order by lstId";

		return session.createQuery(qString).list();
	}

	public List<SysList> getSysList(int lstId, String language) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = ? ";

		if (language != null) {
			qString += "and langId = ? ";
		}
		qString += "order by line";

		Query q = session.createQuery(qString);
		q.setInteger(0, lstId);
		if (language != null) {
			q.setString(1, language);			
		}

		return q.list();
	}

	public SysList getSysListEntry(int lstId, int entryId, String language) {
		Session session = getSession();
		
		String qString = "from SysList " +
			"where lstId = ? " +
			"and entryId = ? " +
			"and langId = ?";

		Query q = session.createQuery(qString);
		q.setInteger(0, lstId);
		q.setInteger(1, entryId);
		q.setString(2, language);

		return (SysList) q.uniqueResult();
	}
}
