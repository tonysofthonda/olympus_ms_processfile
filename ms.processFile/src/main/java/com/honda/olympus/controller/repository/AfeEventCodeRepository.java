package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.EventCodeEntity;

@Repository
public interface AfeEventCodeRepository extends JpaRepository<EventCodeEntity, Long> {

	// QUERY10
	@Query("SELECT o FROM EventCodeEntity o WHERE o.id = :eventCodeId ")
	public List<EventCodeEntity> findAllByEventCode(@Param("eventCodeId") Long eventCodeId);
}
