package com.honda.olympus.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeModelEntity;

@Repository
public interface AfeModelRepository extends JpaRepository<AfeModelEntity, Long> {

	// QUERY4
	@Query("SELECT o FROM AfeModelEntity o WHERE o.code = :code and o.modelYear = :modelYear ")
	List<AfeModelEntity> findAllByCode(@Param("code") String code,@Param("modelYear") Long modelYear);

}
