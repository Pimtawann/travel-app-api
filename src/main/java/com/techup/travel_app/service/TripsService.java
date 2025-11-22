package com.techup.travel_app.service;

import com.techup.travel_app.entity.Trips;
import com.techup.travel_app.entity.User;
import com.techup.travel_app.exception.ResourceNotFoundException;
import com.techup.travel_app.exception.UnauthorizedException;
import com.techup.travel_app.repository.TripsRepository;
import com.techup.travel_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripsService {

    private final TripsRepository tripsRepository;
    private final UserRepository userRepository;

    // ดู trips ทั้งหมด (public) - เรียงจาก id มากไปน้อย
    public List<Trips> getAllTrips() {
        return tripsRepository.findAllByOrderByIdDesc();
    }

    // ดู trip ตาม id (public)
    public Trips getTripById(Long id) {
        return tripsRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));
    }

    // ค้นหา trips ตาม query (public)
    public List<Trips> searchTrips(String query) {
        return tripsRepository.searchByTitleOrTags(query);
    }

    // ดู trips ของตัวเอง (protected)
    public List<Trips> getMyTrips(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return tripsRepository.findByAuthorId(user.getId());
    }

    // สร้าง trip ใหม่ (protected)
    public Trips createTrip(Trips trip, String email) {
        User author = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        trip.setAuthor(author);
        return tripsRepository.save(trip);
    }

    // แก้ไข trip (protected + ownership validation)
    public Trips updateTrip(Long id, Trips updatedTrip, String email) {
        Trips trip = tripsRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));

        if (!trip.getAuthor().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not the owner of this trip");
        }

        // อัปเดตเฉพาะฟิลด์ที่ส่งมา (ไม่เป็น null หรือ empty)
        if (updatedTrip.getTitle() != null && !updatedTrip.getTitle().isEmpty()) {
            trip.setTitle(updatedTrip.getTitle());
        }
        if (updatedTrip.getDescription() != null && !updatedTrip.getDescription().isEmpty()) {
            trip.setDescription(updatedTrip.getDescription());
        }
        if (updatedTrip.getPhotos() != null && !updatedTrip.getPhotos().isEmpty()) {
            trip.setPhotos(updatedTrip.getPhotos());
        }
        if (updatedTrip.getTags() != null && !updatedTrip.getTags().isEmpty()) {
            trip.setTags(updatedTrip.getTags());
        }
        if (updatedTrip.getLatitude() != null) {
            trip.setLatitude(updatedTrip.getLatitude());
        }
        if (updatedTrip.getLongitude() != null) {
            trip.setLongitude(updatedTrip.getLongitude());
        }

        return tripsRepository.save(trip);
    }

    // ลบ trip (protected + ownership validation)
    public void deleteTrip(Long id, String email) {
        Trips trip = tripsRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));

        if (!trip.getAuthor().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not the owner of this trip");
        }

        tripsRepository.delete(trip);
    }
}
