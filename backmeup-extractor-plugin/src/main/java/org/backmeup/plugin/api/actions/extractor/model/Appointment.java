package org.backmeup.plugin.api.actions.extractor.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="APPOINTMENT")
public class Appointment {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="APPOINTMENT_ID")
	private Long id;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="DATE_CREATED")
    private String dateCreated;
	
	@Column(name="DATE_STAMP")
    private String dateStamp;
	
	@Column(name="DATE_START")
    private String dateStart;
	
	@Column(name="DATE_END")
	private String dateEnd;
	
	@Column(name="STATUS")
	private String status;
	
	@Column(name="SUMMARY")
	private String summary;

	
	public Appointment() { }
    
    public Appointment(String description, String dateStart, String dateEnd) {
    	this.description = description;
    	this.dateStart = dateStart;
    	this.dateEnd = dateEnd;
    }
	
	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDateStart() {
		return dateStart;
	}

	public void setDateStart(String dateStart) {
		this.dateStart = dateStart;
	}

	public String getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(String dateEnd) {
		this.dateEnd = dateEnd;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateStamp() {
		return dateStamp;
	}

	public void setDateStamp(String dateStamp) {
		this.dateStamp = dateStamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
