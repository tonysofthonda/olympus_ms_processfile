package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeModelTypeEntity;

@Repository
public interface AfeModelTypeRepository extends JpaRepository<AfeModelTypeEntity, Long>{

	// QUERY4
		@Query("SELECT o FROM AfeModelTypeEntity o WHERE o.id = :id ")
		List<AfeModelTypeEntity> findAllById(@Param("id") Long id);
		
		//QUERY21
		@Query("SELECT o FROM AfeModelTypeEntity o WHERE o.modelType = :modelType AND o.id = :id")
		List<AfeModelTypeEntity> findAllByIdAndModel(@Param("modelType") String modelType,@Param("id") Long id);
	
}
