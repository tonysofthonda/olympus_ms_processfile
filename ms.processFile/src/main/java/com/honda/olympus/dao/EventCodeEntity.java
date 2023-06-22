package com.honda.olympus.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "event_code", schema = "afedb")
public class EventCodeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "\"event_Code_Number\"")
	private Long eventCodeNumber;

	@Column(name = "description",length = 50)
	private String description;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"create_Timestamp\"")
	Date creationTimeStamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"update_timestamp\"")
	Date updateTimeStamp;

	@Column(name = "obs")
	private String obs;

	@Column(name = "bstate")
	private Character bstate;

	
	public EventCodeEntity() {
		super();
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getEventCodeNumber() {
		return eventCodeNumber;
	}

	public void setEventCodeNumber(Long eventCodeNumber) {
		this.eventCodeNumber = eventCodeNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(Date creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public Date getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(Date updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
	}

	public String getObs() {
		return obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

	public Character getBstate() {
		return bstate;
	}

	public void setBstate(Character bstate) {
		this.bstate = bstate;
	}

	
	
	

}
