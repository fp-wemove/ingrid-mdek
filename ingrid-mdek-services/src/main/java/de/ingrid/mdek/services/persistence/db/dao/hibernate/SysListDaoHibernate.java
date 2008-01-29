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

	public List<SysList> getSysList(int lstId, Integer languageCode) {
		Session session = getSession();

		String qString = "from SysList " +
			"where lstId = ? ";

		if (languageCode != null) {
			qString += "and langId = ? ";
		}

		Query q = session.createQuery(qString);
		q.setInteger(0, lstId);
		if (languageCode != null) {
			q.setInteger(1, languageCode);			
		}

		return q.list();
	}
}
