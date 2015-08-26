package org.backmeup.plugin.api.actions.extractor.dao;

import java.util.List;

import org.backmeup.plugin.api.actions.extractor.model.Appointment;

public interface AppointmentDAO {
    public List<Appointment> list();
    
//    public Appointment get(long id);
    
    public void saveOrUpdate(Appointment user);
     
    public void delete(long id);
}
