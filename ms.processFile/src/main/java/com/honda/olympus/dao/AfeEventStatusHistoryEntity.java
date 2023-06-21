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
@Table(name = "afe_event_status_history", schema = "afedb")
public class AfeEventStatusHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private Long id;

	@Column(name = "\"afe_Event_Status_Id\"")
	private Long afeEventStatusId;

	@Column(name = "\"event_Status_Id\"")
	private Long eventStatusId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"cur_Evnt_Status_Dt\"")
	private Date curEvntStatusDt;

	@Column(name = "\"cur_Veh_Evnt_Desc\"", length = 55)
	private String curVehEvntDesc;

	@Column(name = "\"envio_Flag_Ah_Event\"")
	private Boolean envioFlagAhEvent;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"create_Timestamp\"")
	Date creationTimeStamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"update_timestamp\"")
	Date updateTimeStamp;

	@Column(name = "\"obs\"")
	private String obs;

	@Column(name = "\"bstate\"")
	private Character bstate;

	public AfeEventStatusHistoryEntity() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAfeEventStatusId() {
		return afeEventStatusId;
	}

	public void setAfeEventStatusId(Long afeEventStatusId) {
		this.afeEventStatusId = afeEventStatusId;
	}

	public Long getEventStatusId() {
		return eventStatusId;
	}

	public void setEventStatusId(Long eventStatusId) {
		this.eventStatusId = eventStatusId;
	}

	public Date getCurEvntStatusDt() {
		return curEvntStatusDt;
	}

	public void setCurEvntStatusDt(Date curEvntStatusDt) {
		this.curEvntStatusDt = curEvntStatusDt;
	}

	public String getCurVehEvntDesc() {
		return curVehEvntDesc;
	}

	public void setCurVehEvntDesc(String curVehEvntDesc) {
		this.curVehEvntDesc = curVehEvntDesc;
	}

	public Boolean getEnvioFlagAhEvent() {
		return envioFlagAhEvent;
	}

	public void setEnvioFlagAhEvent(Boolean envioFlagAhEvent) {
		this.envioFlagAhEvent = envioFlagAhEvent;
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
