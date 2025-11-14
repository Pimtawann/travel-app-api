package com.techup.travel_app.controller;

import com.techup.travel_app.entity.Trips;
import com.techup.travel_app.security.JwtService;
import com.techup.travel_app.service.TripsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripsController {

    private final TripsService tripsService;
    private final JwtService jwtService;

    // GET /api/trips - ดู trips ทั้งหมด (public)
    // GET /api/trips?query=xxx - ค้นหา trips (public)
    @GetMapping
    public List<Trips> getTrips(@RequestParam(required = false) String query) {
        if (query != null && !query.isEmpty()) {
            return tripsService.searchTrips(query);
        }
        return tripsService.getAllTrips();
    }

    // GET /api/trips/{id} - ดู trip ตาม id (public)
    @GetMapping("/{id}")
    public Trips getTripById(@PathVariable Long id) {
        return tripsService.getTripById(id);
    }

    // GET /api/trips/mine - ดู trips ของตัวเอง (protected)
    @GetMapping("/mine")
    public List<Trips> getMyTrips(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return tripsService.getMyTrips(email);
    }

    // POST /api/trips - สร้าง trip ใหม่ (protected)
    @PostMapping
    public Trips createTrip(
        @RequestBody Trips trip,
        @RequestHeader("Authorization") String header
    ) {
        String token = header.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return tripsService.createTrip(trip, email);
    }

    // PUT /api/trips/{id} - แก้ไข trip (protected + ownership validation)
    @PutMapping("/{id}")
    public Trips updateTrip(
        @PathVariable Long id,
        @RequestBody Trips trip,
        @RequestHeader("Authorization") String header
    ) {
        String token = header.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return tripsService.updateTrip(id, trip, email);
    }

    // DELETE /api/trips/{id} - ลบ trip (protected + ownership validation)
    @DeleteMapping("/{id}")
    public String deleteTrip(
        @PathVariable Long id,
        @RequestHeader("Authorization") String header
    ) {
        String token = header.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        tripsService.deleteTrip(id, email);
        return "Trip deleted successfully";
    }
}
