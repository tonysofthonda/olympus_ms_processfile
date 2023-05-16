package com.honda.olympus.controller.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honda.olympus.dao.AfeModel;

@Repository
public interface AfeModelRepository extends JpaRepository<AfeModel, Long> {

	// QUERY3
	@Query("SELECT o FROM AfeModel o WHERE o.requestId = :id ")
	Collection<AfeModel> findAllById(@Param("id") Long id);

}
