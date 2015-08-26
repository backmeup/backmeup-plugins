package org.backmeup.plugin.api.actions.extractor;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.actions.extractor.model.PersonIdentity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;


/**
 * Tests the JPA Hibernate storage and retrieval layer for index user configurations via derby DB with
 * hibernate.hbm2ddl.auto=create
 */
public class PersonIdentityDaoTest {
	@Rule
    public final DerbyDatabase database = new DerbyDatabase();
	
	PersonIdentityDAO personIdentityDAO;

	@Before
    public void before() {
    	this.personIdentityDAO = this.database.personIdentityDAO;    	
    }

    private PersonIdentity createIdentity() {
    	PersonIdentity ident = new PersonIdentity();
        ident.setFamilyName("aaaa");
        ident.setSurName("bbbb");
        return ident;
    }
    
    @Test
    public void testStoreIdentity() {
        PersonIdentity ident = createIdentity();
        this.personIdentityDAO.saveOrUpdate(ident);
        List<PersonIdentity> found = this.personIdentityDAO.list();
        //assertNotZero("identity with id " + id, found);
    }
}