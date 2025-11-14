package com.techup.travel_app.service;

import com.techup.travel_app.entity.Trips;
import com.techup.travel_app.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripsService {

    private final TripsRepository tripsRepository;

    // ดู trips ทั้งหมด
    public List<Trips> getAllTrips() {
        return tripsRepository.findAll();
    }

    // ดู trip ตาม id
    public Trips getTripById(Long id) {
        return tripsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    // ค้นหา trips ตาม query (ค้นหาจาก title และ tags)
    public List<Trips> searchTrips(String query) {
        return tripsRepository.searchByTitleOrTags(query);
    }
}
