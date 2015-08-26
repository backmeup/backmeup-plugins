package org.backmeup.plugin.api.actions.extractor.dao;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.model.PersonIdentity;

public interface PersonIdentityDAO {
    public List<PersonIdentity> list();
    
//    public PersonIdentity get(long id);
    
    public void saveOrUpdate(PersonIdentity user);
     
    public void delete(long id);
}
