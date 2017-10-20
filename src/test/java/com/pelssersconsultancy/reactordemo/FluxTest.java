package com.pelssersconsultancy.reactordemo;


import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.Arrays;

import static com.pelssersconsultancy.reactordemo.UserPredicates.hasName;

public class FluxTest {

    @Test
    public void printFlux() {
        Flux<String> flux = Flux.just("Robby", "Davy");
        flux.subscribe(System.out::println);
    }

    @Test
    public void createEmptyFlux() {
        Flux<String> emptyFlux = Flux.empty();
    }

    @Test
    public void createFluxFromDataValues() {
        Flux<String> flux = Flux.just("foo", "bar");
        StepVerifier.create(flux)
                .expectNext("foo")
                .expectNext("bar")
                .expectComplete()
                .verify();
    }

    @Test
    public void createFluxFromIterable() {
       Flux<String> flux = Flux.fromIterable(Arrays.asList("Hello", "world"));
       StepVerifier.create(flux)
               .expectNext("Hello")
               .expectNext("world")
               .expectComplete()
               .verify();
    }

    @Test
    public void createFluxError() {
        Flux<Integer> divide100ByNumberFlux = Flux.just(2, 4, 0).map(number -> 100 / number);
        StepVerifier.create(divide100ByNumberFlux)
                .expectNext(50)
                .expectNext(25)
                .expectError(ArithmeticException.class)
                .verify();
    }


    @Test
    public void verifyUsersInFlux() {
        StepVerifier.create(createUsersFlux())
                .expectNextMatches(hasName("Robby"))
                .expectNextMatches(hasName("Davy"))
                .expectComplete()
                .verify();
    }

    @Test
    public void upperCaseUserNames() {
        Flux<User> upperCased = createUsersFlux().map(user -> new User(user.getName().toUpperCase()));
        StepVerifier.create(upperCased)
                .expectNextMatches(hasName("ROBBY"))
                .expectNextMatches(hasName("DAVY"))
                .expectComplete()
                .verify();
    }

    @Test
    public void upperCaseAsync() {
        Flux<User> upperCased = createUsersFlux().flatMap(this::asyncToUpperCase);
        StepVerifier.create(upperCased)
                .expectNextMatches(hasName("ROBBY"))
                .expectNextMatches(hasName("DAVY"))
                .expectComplete()
                .verify();
    }

    public Mono<User> asyncToUpperCase(User user) {
        return Mono.just(new User(user.getName().toUpperCase()));
    }

    @Test
    public void createFluxAndCount() {
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1)).take(2);
        StepVerifier.create(flux)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    public void createFluxAndCountWithVirtualTime() {
        VirtualTimeScheduler.getOrSet();
        //It would take 10 seconds for this test to run normally so we use a virtual scheduler which we can manipulate
        int amountOfElements = 2;
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(5)).take(amountOfElements);

        StepVerifier.withVirtualTime(() -> flux)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(10))
                .expectNextCount(2)
                .verifyComplete();
    }



    private Flux<User> createUsersFlux() {
        return Flux.just(new User("Robby"), new User("Davy"));
    }


}
