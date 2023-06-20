package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeModelColorEntity;

@Repository
public interface AfeModelColorRepository extends JpaRepository<AfeModelColorEntity, Long>{

	// QUERY4
		@Query("SELECT o FROM AfeModelColorEntity o WHERE o.modelId = :modelId ")
		List<AfeModelColorEntity> findAllByModelId(@Param("modelId") Long modelId);
	
}