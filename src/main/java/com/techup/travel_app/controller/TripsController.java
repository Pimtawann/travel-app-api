package com.techup.travel_app.controller;

import com.techup.travel_app.entity.Trips;
import com.techup.travel_app.service.TripsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripsController {

    private final TripsService tripsService;

    // GET /api/trips - ดู trips ทั้งหมด
    // GET /api/trips?query=xxx - ค้นหา trips
    @GetMapping
    public List<Trips> getTrips(@RequestParam(required = false) String query) {
        if (query != null && !query.isEmpty()) {
            return tripsService.searchTrips(query);
        }
        return tripsService.getAllTrips();
    }

    // GET /api/trips/{id} - ดู trip ตาม id
    @GetMapping("/{id}")
    public Trips getTripById(@PathVariable Long id) {
        return tripsService.getTripById(id);
    }
}
