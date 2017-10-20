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
        StepVerifier.create(createMaleUsers())
                .expectNextMatches(hasName("Robby"))
                .expectNextMatches(hasName("Davy"))
                .expectComplete()
                .verify();
    }

    @Test
    public void upperCaseUserNames() {
        Flux<User> upperCased = createMaleUsers().map(user -> new User(user.getName().toUpperCase()));
        StepVerifier.create(upperCased)
                .expectNextMatches(hasName("ROBBY"))
                .expectNextMatches(hasName("DAVY"))
                .expectComplete()
                .verify();
    }

    @Test
    public void upperCaseAsync() {
        Flux<User> upperCased = createMaleUsers().flatMap(this::asyncToUpperCase);
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


    @Test
    public void mergeUserNames() {
        Flux<String> merged = createMaleUsers().mergeWith(createFemaleUsers()).map(User::getName);
        StepVerifier.create(merged)
                .expectNext("Robby", "Davy", "Anita", "Riet")
                .verifyComplete();
    }

    @Test
    public void mergeTimedFluxesAsIs() {
        produceStringsEach2Seconds()
                .mergeWith(produceStringsEach3Seconds())
                .toStream()
                .forEach(System.out::println);
    }

    @Test
    public void mergeTimedFluxesPreservingMergeOrder() {
        produceStringsEach2Seconds()
                .concatWith(produceStringsEach3Seconds())
                .toStream()
                .forEach(System.out::println);
    }


    @Test
    public void requestTest() {
        int requestCount = 5;
        StepVerifier.create(integers())         //we setup a flux with 10 numbers
                .thenRequest(requestCount)      //but our verifier subscriber only requests 5 numbers
                .expectNextCount(requestCount)  //verify we only are invoked with onNext 5 times
                .expectComplete()               //verify the flux calls onComplete
                .verify();                      //start the actual subscription by calling verify
    }

    @Test
    public void request2numbersOneByOneAndCancelFluxAfterwards() {
        StepVerifier.create(integers())
                .thenRequest(1)
                .expectNext(1)
                .thenRequest(1)
                .expectNext(2)
                .thenCancel();
    }


    private Flux<Integer> integers() {
        return Flux.just(1,2,3,4,5,6,7,8,9,10);
    }

    private Flux<String> produceStringsEach2Seconds() {
        //should produce [t=2s -> "flux_each_2_seconds_1", t=4s ->  "flux_each_2_seconds_2", t=6s -> "flux_each_2_seconds_3"]
        return stringsWithXSecondDelays("flux_each_2_seconds_", 2).take(3);
    }

    private Flux<String> produceStringsEach3Seconds() {
        //should produce [t=3s -> "flux_each_3_seconds_1", t=6s ->  "flux_each_3_seconds_2", t=9s -> "flux_each_3_seconds_3"]
        return stringsWithXSecondDelays("flux_each_3_seconds_", 3).take(3);
    }

    private Flux<String> stringsWithXSecondDelays(String fluxName, int seconds) {
        return Flux.interval(Duration.ofSeconds(seconds)).map(x -> fluxName +  + x);
    }

    private Flux<User> createMaleUsers() {
        return Flux.just(new User("Robby"), new User("Davy"));
    }

    private Flux<User> createFemaleUsers() {
        return Flux.just(new User("Anita"), new User("Riet"));
    }


}
