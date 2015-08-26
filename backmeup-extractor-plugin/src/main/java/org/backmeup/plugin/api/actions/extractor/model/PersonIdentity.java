package org.backmeup.plugin.api.actions.extractor.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="PERSON_IDENTITY")
public class PersonIdentity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="USER_ID")
	private Long id;
	
	@Column(name="FAMILYNAME")
    private String familyName;
	
	@Column(name="SURNAME")
    private String surName;
	
	@Column(name="PREFIX")
    private String prefix;

//	private List<String> phoneNumbers;
	
    public PersonIdentity() { }
    
    public PersonIdentity(String familyName, String surName, String prefix) {
    	this.familyName = familyName;
    	this.surName = surName;
    	this.prefix = prefix;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }
    
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
//    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
//    public List<String> getValues() {
//        return phoneNumbers;
//    }   
}
