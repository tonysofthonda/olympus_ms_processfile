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
@Table(name = "afe_fixed_orders_ev", schema = "afedb")
public class AfeFixedOrdersEvEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "envio_Flag_Gm")
	private Boolean envioFlagGm;
	
	@Column(name = "action_Id")
	private Long actionId;

	@Column(name = "model_Color_Id")
	private Long modelColorId;
	
	@Column(name = "order_Number",length = 6)
	private String orderNumber;

	@Column(name = "selling_Code",length = 2)
	private String sellingCode;

	@Column(name = "origin_Type",length = 8)
	private String originType;

	@Column(name = "extern_Config_Id",length = 32)
	private String externConfigId;

	@Column(name = "order_Type",length = 3)
	private String orderType;

	@Column(name = "chrg_Asct")
	private Long chrgAsct;

	@Column(name = "chrg_Fcn")
	private Long chrgFcn;

	@Column(name = "ship_Sct")
	private Long shipSct;

	@Column(name = "ship_Fcn")
	private Long shipFcn;

	@Column(name = "request_Id",length = 40)
	private String requestId;

	@Column(name = "vin_Number",length = 17)
	private String vinNumber;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "prod_Week_Start_Day")
	Date prodWeekStartDay;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ord_Due_Dt")
	Date ordDueDt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_Ord_Timestamp")
	Date createOrdTimestamp;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cancel_Ord_Timestamp")
	Date cancelOrdTimestamp;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "change_Ord_Timestamp")
	Date changeOrdTimestamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_Timestamp")
	Date creationTimeStamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_Timestamp")
	Date updateTimeStamp;
	
	
	@Column(name = "obs")
	private String obs;

	@Column(name = "bstate")
	private Character bstate;

	public AfeFixedOrdersEvEntity() {
	}

}
