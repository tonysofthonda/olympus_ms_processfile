package com.honda.olympus.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "afe_fixed_orders_ev", schema= "afedb")
public class AfeFixedOrdersEvEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "envio_flag")
	private String envioFlag;

	@Column(name = "order_number")
	private String orderNumber;

	@Column(name = "selling_code")
	private String sellingCode;
	
	@Column(name = "origin_type")
	private String originType;

	@Column(name = "extern_config_id")
	private String externConfigId;

	@Column(name = "order_type")
	private String orderType;

	@Column(name = "chrg_asct")
	private String chrgAsct;

	@Column(name = "chrg_fcm")
	private String chrgFcm;

	@Column(name = "ship_fcm")
	private String shipFcm;

	@Column(name = "request_id")
	private String requestId;

	@Column(name = "start_day")
	private String startDay;

	@Column(name = "due_date")
	private String dueDate;

	@Column(name = "ack_id")
	private String ackId;

	@Column(name = "status_ev_id")
	private String statusEvId;

	@Column(name = "action_id")
	private String actionId;

	@Column(name = "model_color_id")
	private String modelColorId;

	public AfeFixedOrdersEvEntity() {
	}

	public AfeFixedOrdersEvEntity(Long id, String envioFlag, String orderNumber, String sellingCode, String originType,
			String externConfigId, String orderType, String chrgAsct, String chrgFcm, String shipFcm, String requestId,
			String startDay, String dueDate, String ackId, String statusEvId, String actionId, String modelColorId) {
		super();
		this.id = id;
		this.envioFlag = envioFlag;
		this.orderNumber = orderNumber;
		this.sellingCode = sellingCode;
		this.originType = originType;
		this.externConfigId = externConfigId;
		this.orderType = orderType;
		this.chrgAsct = chrgAsct;
		this.chrgFcm = chrgFcm;
		this.shipFcm = shipFcm;
		this.requestId = requestId;
		this.startDay = startDay;
		this.dueDate = dueDate;
		this.ackId = ackId;
		this.statusEvId = statusEvId;
		this.actionId = actionId;
		this.modelColorId = modelColorId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEnvioFlag() {
		return envioFlag;
	}

	public void setEnvioFlag(String envioFlag) {
		this.envioFlag = envioFlag;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getSellingCode() {
		return sellingCode;
	}

	public void setSellingCode(String sellingCode) {
		this.sellingCode = sellingCode;
	}

	public String getOriginType() {
		return originType;
	}

	public void setOriginType(String originType) {
		this.originType = originType;
	}

	public String getExternConfigId() {
		return externConfigId;
	}

	public void setExternConfigId(String externConfigId) {
		this.externConfigId = externConfigId;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getChrgAsct() {
		return chrgAsct;
	}

	public void setChrgAsct(String chrgAsct) {
		this.chrgAsct = chrgAsct;
	}

	public String getChrgFcm() {
		return chrgFcm;
	}

	public void setChrgFcm(String chrgFcm) {
		this.chrgFcm = chrgFcm;
	}

	public String getShipFcm() {
		return shipFcm;
	}

	public void setShipFcm(String shipFcm) {
		this.shipFcm = shipFcm;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getAckId() {
		return ackId;
	}

	public void setAckId(String ackId) {
		this.ackId = ackId;
	}

	public String getStatusEvId() {
		return statusEvId;
	}

	public void setStatusEvId(String statusEvId) {
		this.statusEvId = statusEvId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getModelColorId() {
		return modelColorId;
	}

	public void setModelColorId(String modelColorId) {
		this.modelColorId = modelColorId;
	}

}
