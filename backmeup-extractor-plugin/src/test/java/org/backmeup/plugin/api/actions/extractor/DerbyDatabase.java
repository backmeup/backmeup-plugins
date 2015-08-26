package org.backmeup.plugin.api.actions.extractor;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.backmeup.plugin.api.actions.extractor.dao.AppointmentDAO;
import org.backmeup.plugin.api.actions.extractor.dao.AppointmentDAOImpl;
import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAOImpl;
import org.junit.rules.ExternalResource;
import org.mockito.internal.util.reflection.Whitebox;

import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public class DerbyDatabase extends ExternalResource {

    //public EntityManagerFactory entityManagerFactory;
    //public EntityManager entityManager;

    public PersonIdentityDAO personIdentityDAO;
    
    public AppointmentDAO appointmentDAO;
    
    private static final String PERSISTENCE_UNIT = "org.backmeup.plugin.api.actions.extractor";
    
    public LocalSessionFactoryBean localSessionFactoryBean;
    
    @Override
    protected void before() {
    	//this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, overwrittenJPAProps());
        //this.entityManager = this.entityManagerFactory.createEntityManager();
        this.localSessionFactoryBean = new LocalSessionFactoryBean();
        
        this.personIdentityDAO = new PersonIdentityDAOImpl();
        Whitebox.setInternalState(this.personIdentityDAO, "sessionFactory", this.localSessionFactoryBean);
       
        this.appointmentDAO = new AppointmentDAOImpl();
        Whitebox.setInternalState(this.appointmentDAO, "sessionFactory", this.localSessionFactoryBean);
        
        
    }

    private Properties overwrittenJPAProps() {
        Properties overwrittenJPAProps = new Properties();

        overwrittenJPAProps.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        overwrittenJPAProps.setProperty("javax.persistence.jdbc.url", "jdbc:derby:target/junit/derby;create=true");

        overwrittenJPAProps.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
        overwrittenJPAProps.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        return overwrittenJPAProps;
    }

    @Override
    protected void after() {
        //this.entityManager.close();
        //if (this.entityManagerFactory.isOpen())
        //	this.entityManagerFactory.close();
    }

}
