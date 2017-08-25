package cass;

import cass.domain.Hotel;
import cass.service.HotelService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleCassandraApplication {

    public static void main(String[] args) {
        SpringApplication
                .run(SampleCassandraApplication.class, args)
                .getBean(HotelService.class)
                .bookOneHotel("OR")
                .doOnError(err -> err.printStackTrace())
                .doOnSuccess(bookedHotel ->
                        System.out.println("Booked hotel: " + bookedHotel.getName()))
                .subscribe();
    }

}
