package org.backmeup.plugin.api.actions.extractor;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAOImpl;
import org.junit.rules.ExternalResource;

public class DerbyDatabase extends ExternalResource {

    private EntityManagerFactory entityManagerFactory;
    public EntityManager entityManager;

    public PersonIdentityDAO PersonIdentityDAO;

    @Override
    protected void before() {
        this.PersonIdentityDAO = new PersonIdentityDAOImpl(null);
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
        this.entityManager.close();
    }

}
