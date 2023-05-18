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
@Table(name = "afe_status_ev", schema = "afedb")
public class AfeStatusEvEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "fixed_order_id")
	private Long fixedOrderId;

	@Column(name = "vin_id")
	private Long vinId;

	@Column(name = "ack_ev_id")
	private Long ackEvId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "event_code_id")
	private Long eventCodeId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "event_code_date")
	private Date eventCodeDate;

	@Column(name = "dir_rec_bys_ast_cd")
	private String dirRecBysAstCd;

	@Column(name = "dir_rec_bus_fcn_cd")
	private String dirRecBusFcnCd;

	@Column(name = "event_description")
	private String eventDescription;

	@Column(name = "code_vendor_event")
	private String codeVendorEvent;

	@Column(name = "current_event_status")
	private String currentEventStatus;

	
	public AfeStatusEvEntity() {
		super();
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVinId() {
		return vinId;
	}

	public void setVinId(Long vinId) {
		this.vinId = vinId;
	}

	public Long getAckEvId() {
		return ackEvId;
	}

	public void setAckEvId(Long ackEvId) {
		this.ackEvId = ackEvId;
	}

	public Long getFixedOrderId() {
		return fixedOrderId;
	}

	public void setFixedOrderId(Long fixedOrderId) {
		this.fixedOrderId = fixedOrderId;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public Long getEventCodeId() {
		return eventCodeId;
	}

	public void setEventCodeId(Long eventCodeId) {
		this.eventCodeId = eventCodeId;
	}

	public Date getEventCodeDate() {
		return eventCodeDate;
	}

	public void setEventCodeDate(Date eventCodeDate) {
		this.eventCodeDate = eventCodeDate;
	}

	public String getDirRecBysAstCd() {
		return dirRecBysAstCd;
	}

	public void setDirRecBysAstCd(String dirRecBysAstCd) {
		this.dirRecBysAstCd = dirRecBysAstCd;
	}

	public String getDirRecBusFcnCd() {
		return dirRecBusFcnCd;
	}

	public void setDirRecBusFcnCd(String dirRecBusFcnCd) {
		this.dirRecBusFcnCd = dirRecBusFcnCd;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	public String getCodeVendorEvent() {
		return codeVendorEvent;
	}

	public void setCodeVendorEvent(String codeVendorEvent) {
		this.codeVendorEvent = codeVendorEvent;
	}

	public String getCurrentEventStatus() {
		return currentEventStatus;
	}

	public void setCurrentEventStatus(String currentEventStatus) {
		this.currentEventStatus = currentEventStatus;
	}

}
