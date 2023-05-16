package com.honda.olympus.controller.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeFixedOrdersEvEntity;

@Repository
public interface AfeFixedOrdersEvRepository extends JpaRepository<AfeFixedOrdersEvEntity, Long> {

	// QUERY1
	@Query("SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.requestId = :requestId ")
	Collection<AfeFixedOrdersEvEntity> findAllById(@Param("requestId") String id);
}
