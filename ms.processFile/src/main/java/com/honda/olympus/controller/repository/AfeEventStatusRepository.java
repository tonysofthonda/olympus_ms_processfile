package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeEventStatusEntity;

@Repository
public interface AfeEventStatusRepository extends JpaRepository<AfeEventStatusEntity, Long> {

	// QUERY9
	@Query("SELECT o FROM AfeEventStatusEntity o WHERE o.fixedOrderId = :fixedOrderId ")
	public List<AfeEventStatusEntity> findAllByFixedOrder(@Param("fixedOrderId") Long fixedOrderId);

}
