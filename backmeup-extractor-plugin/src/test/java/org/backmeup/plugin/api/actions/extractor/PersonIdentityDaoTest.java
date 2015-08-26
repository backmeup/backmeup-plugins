package org.backmeup.plugin.api.actions.extractor;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.actions.extractor.model.PersonIdentity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


/**
 * Tests the JPA Hibernate storage and retrieval layer for index user configurations via derby DB with
 * hibernate.hbm2ddl.auto=create
 */
public class PersonIdentityDaoTest {
	@Rule
    public final DerbyDatabase database = new DerbyDatabase();

//    @Override
//    protected void before() {
//    	PersonIdentityDao aa = this.database.personIdentityDao;
//    }
    
    private PersonIdentity createIdentity() {
    	PersonIdentity ident = new PersonIdentity();
        ident.setFamilyName("aaaa");
        ident.setSurName("bbbb");
        return ident;
    }
    
    @Test
    public void shouldStoreConfigurationAndReadFromDBByHttpPort() {
    	
        PersonIdentity ident = createIdentity();
        List<PersonIdentity> found = this.database.PersonIdentityDAO.list();
        //assertNotZero("identity with id " + id, found);
    }

    private PersonIdentity persistInTransaction(PersonIdentity config) {
        // need manual transaction in test because transactional interceptor is not installed in tests
    	PersonIdentity ret = null;
//    	this.database.entityManager.getTransaction().begin();
//      ret = this.database.PersonIdentityDAO.save();
//      this.database.entityManager.getTransaction().commit();
        return ret;
    }

}