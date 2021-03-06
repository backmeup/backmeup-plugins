package org.backmeup.plugin.api.actions.extractor.dao;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.model.Appointment;
 

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//@Transactional
public class AppointmentDAOImpl implements AppointmentDAO {
	//@Inject
	public SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
    
	public AppointmentDAOImpl() {
    }
    
    public AppointmentDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public void doDestroy() {
    	System.out.println("ExtractorAction.doDestroy()");
    }
    
    @Override
    public List<Appointment> list() {
        @SuppressWarnings("unchecked")
        List<Appointment> listUser = (List<Appointment>) sessionFactory.getCurrentSession()
                .createCriteria(Appointment.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 
        return listUser;
    }
    
    @Override
    @Transactional
    public void saveOrUpdate(Appointment user) {
    	Transaction trans = sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        trans.commit();
    }
 
    @Override
    @Transactional
    public void delete(long id) {
    	Appointment userToDelete = new Appointment();
        userToDelete.setId(id);
        Transaction trans = sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().delete(userToDelete);
        trans.commit();
    }
 
//    @Override
//    public Appointment get(long id) {
//        String hql = "from User where id=" + id;
//        Query query = sessionFactory.getCurrentSession().createQuery(hql);
//         
//        @SuppressWarnings("unchecked")
//        List<Appointment> listUser = (List<Appointment>) query.list();
//         
//        if (listUser != null && !listUser.isEmpty()) {
//            return listUser.get(0);
//        }
//         
//        return null;
//    }
}
