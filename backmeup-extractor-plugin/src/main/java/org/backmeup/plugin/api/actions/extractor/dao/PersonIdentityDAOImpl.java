package org.backmeup.plugin.api.actions.extractor.dao;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.model.PersonIdentity;
 

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import javax.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//@Transactional
public class PersonIdentityDAOImpl implements PersonIdentityDAO {

	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
    
	public PersonIdentityDAOImpl() {
    	System.out.println("asdf");
    }
    
    public PersonIdentityDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public void doDestroy() {
    	System.out.println("ExtractorAction.doDestroy()");
    }
    
    @Override
    public List<PersonIdentity> list() {
        @SuppressWarnings("unchecked")
        List<PersonIdentity> listUser = (List<PersonIdentity>) sessionFactory.getCurrentSession()
                .createCriteria(PersonIdentity.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 
        return listUser;
    }
    
    @Override
    @Transactional
    public void saveOrUpdate(PersonIdentity user) {
    	Transaction trans = sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        trans.commit();
    }
 
    @Override
    @Transactional
    public void delete(long id) {
    	PersonIdentity userToDelete = new PersonIdentity();
        userToDelete.setId(id);
        Transaction trans = sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().delete(userToDelete);
        trans.commit();
    }
 
//    @Override
//    public PersonIdentity get(long id) {
//        String hql = "from User where id=" + id;
//        Query query = sessionFactory.getCurrentSession().createQuery(hql);
//         
//        @SuppressWarnings("unchecked")
//        List<PersonIdentity> listUser = (List<PersonIdentity>) query.list();
//         
//        if (listUser != null && !listUser.isEmpty()) {
//            return listUser.get(0);
//        }
//         
//        return null;
//    }
}
