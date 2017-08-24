package cass.repository;

import cass.domain.Hotel;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface HotelRepository extends ReactiveCrudRepository<Hotel, UUID> {
    @Query("SELECT * FROM hotels_by_state WHERE state = ?0")
    Flux<Hotel> findByState(String state);
}
