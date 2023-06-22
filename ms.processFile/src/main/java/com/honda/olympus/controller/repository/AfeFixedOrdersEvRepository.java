package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeFixedOrdersEvEntity;

@Repository
public interface AfeFixedOrdersEvRepository extends JpaRepository<AfeFixedOrdersEvEntity, Long> {

	// QUERY1
	@Query("SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.requestId = :requestId ")
	List<AfeFixedOrdersEvEntity> findByRequestId(@Param("requestId") String requestId);
	
	//QUERY2
	@Query("SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.externConfigId = :externConfigId ")
	List<AfeFixedOrdersEvEntity> findByExternConfigId(@Param("externConfigId") String externConfigId);
	
	//QUERY9
	@Query("SELECT o FROM AfeFixedOrdersEvEntity o WHERE  o.requestId = :requestId AND o.externConfigId = :externConfigId ")
	List<AfeFixedOrdersEvEntity> findByRequestAndExternConfigId(@Param("externConfigId") String externConfigId,
			@Param("requestId") String requestId);
	
	//QUERY15
	@Query("SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.id = :id ")
	AfeFixedOrdersEvEntity findFixedOrderById(@Param("id") Long id);
}
