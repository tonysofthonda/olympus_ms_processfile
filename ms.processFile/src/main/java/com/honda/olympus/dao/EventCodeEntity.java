package com.honda.olympus.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "event_code", schema = "afedb")
public class EventCodeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "event_code_number")
	private Long eventCodeNumber;

	@Column(name = "description")
	private String description;

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

}
