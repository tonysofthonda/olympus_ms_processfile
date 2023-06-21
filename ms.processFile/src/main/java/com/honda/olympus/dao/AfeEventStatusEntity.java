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
@Table(name = "afe_event_status", schema = "afedb")
public class AfeEventStatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private Long id;
	
	@Column(name = "\"fixed_Order_Id\"")
	private Long fixedOrderId;

	@Column(name = "\"extern_Config_Types\"", length = 50)
	private String externConfigTypes;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"vo_Last_Chg_Timstm\"")
	private Date voLastChgTimstm;

	@Column(name = "\"peg_Option\"")
	private String pegOption;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"target_Prodn_Dt\"")
	private Date targetProdnDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"estd_Delvry_Dt\"")
	private Date estdDelvryDt;
	
	@Column(name = "\"event_Code_Id\"")
	private Long eventCodeId;
	
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
	
	
	public AfeEventStatusEntity() {
		super();
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Long getFixedOrderId() {
		return fixedOrderId;
	}


	public void setFixedOrderId(Long fixedOrderId) {
		this.fixedOrderId = fixedOrderId;
	}


	public String getExternConfigTypes() {
		return externConfigTypes;
	}


	public void setExternConfigTypes(String externConfigTypes) {
		this.externConfigTypes = externConfigTypes;
	}


	public Date getVoLastChgTimstm() {
		return voLastChgTimstm;
	}


	public void setVoLastChgTimstm(Date voLastChgTimstm) {
		this.voLastChgTimstm = voLastChgTimstm;
	}


	public String getPegOption() {
		return pegOption;
	}


	public void setPegOption(String pegOption) {
		this.pegOption = pegOption;
	}


	public Date getTargetProdnDt() {
		return targetProdnDt;
	}


	public void setTargetProdnDt(Date targetProdnDt) {
		this.targetProdnDt = targetProdnDt;
	}


	public Date getEstdDelvryDt() {
		return estdDelvryDt;
	}


	public void setEstdDelvryDt(Date estdDelvryDt) {
		this.estdDelvryDt = estdDelvryDt;
	}


	public Long getEventCodeId() {
		return eventCodeId;
	}


	public void setEventCodeId(Long eventCodeId) {
		this.eventCodeId = eventCodeId;
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
