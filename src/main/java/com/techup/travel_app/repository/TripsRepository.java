package com.techup.travel_app.repository;

import com.techup.travel_app.entity.Trips;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripsRepository extends JpaRepository<Trips, Long> {

    List<Trips> findByAuthorId(Long authorId);

    @Query(value = "SELECT * FROM trips WHERE :tag = ANY(tags)", nativeQuery = true)
    List<Trips> findByTagsContaining(@Param("tag") String tag);

    List<Trips> findByTitleContainingIgnoreCase(String title);

}
