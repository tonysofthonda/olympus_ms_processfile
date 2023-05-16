package com.honda.olympus.controller.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.honda.olympus.dao.AfeFixedOrdersEvEntity;
import com.honda.olympus.dao.AfeModelColor;

public interface AfeColorRepository extends JpaRepository<AfeModelColor, Long> {

	// QUERY2
	@Query("SELECT o FROM AfeModelColor o WHERE o.id = :id ")
	public Collection<AfeFixedOrdersEvEntity> findAllById(@Param("id") Long id);
}
