package cass.service;

import cass.domain.Hotel;
import cass.domain.HotelByLetter;
import cass.repository.HotelByLetterRepository;
import cass.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelByLetterRepository hotelByLetterRepository;
    private final WebClient bookingClient = WebClient
            .create("https://www.google.ro/");

    public HotelServiceImpl(HotelRepository hotelRepository,
                            HotelByLetterRepository hotelByLetterRepository) {
        this.hotelRepository = hotelRepository;
        this.hotelByLetterRepository = hotelByLetterRepository;
    }

    @Override
    public Mono<Hotel> save(Hotel hotel) {
        if (hotel.getId() == null) {
            hotel.setId(UUID.randomUUID());
        }
        Mono<Hotel> saved = this.hotelRepository.save(hotel);
        saved.then(this.hotelByLetterRepository.save(new HotelByLetter(hotel))).subscribe();
        return saved;
    }

    @Override
    public Mono<Hotel> update(Hotel hotel) {
        return this.hotelRepository.findById(hotel.getId())
                .flatMap(existingHotel ->
                        this.hotelByLetterRepository.delete(new HotelByLetter(existingHotel).getHotelByLetterKey())
                                .then(this.hotelByLetterRepository.save(new HotelByLetter(hotel)))
                                .then(this.hotelRepository.save(hotel)));
    }

    @Override
    public Mono<Hotel> findOne(UUID uuid) {
        return this.hotelRepository.findById(uuid);
    }

    @Override
    public Mono<Boolean> delete(UUID uuid) {
        Mono<Hotel> hotelMono = this.hotelRepository.findById(uuid);
        return hotelMono
                .flatMap((Hotel hotel) -> this.hotelRepository.delete(hotel)
                        .then(this.hotelByLetterRepository
                                .delete(new HotelByLetter(hotel).getHotelByLetterKey())));
    }

    @Override
    public Flux<HotelByLetter> findHotelsStartingWith(String letter) {
        return this.hotelByLetterRepository.findByFirstLetter(letter);
    }

    @Override
    public Flux<Hotel> findHotelsInState(String state) {
        return this.hotelRepository.findByState(state);
    }

    @Override
    public Mono<Hotel> bookOneHotel(String state) {
        return findHotelsInState(state)
                .concatMap(hotel -> book(hotel))
                .next();
    }

    private Mono<Hotel> book(Hotel hotel) {
        return bookingClient.get()
                .uri("search?q=book+me+this+hotel+{hotel}", hotel.getName())
                .exchange()
                .flatMap(response -> response.bodyToFlux(String.class).next())
                .doOnSuccess(text -> System.out.println(text))
                .filter(text -> isBookedSuccesful(text))
                .map(text -> hotel);
    }

    private boolean isBookedSuccesful(String text) {
        return text.hashCode() % 21 != 0;
    }
}
