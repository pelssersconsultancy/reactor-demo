package com.pelssersconsultancy.reactordemo;

import org.junit.Test;
import reactor.core.publisher.Mono;

public class MonoTest {


    @Test
    public void createEmptyMono() {
        logMono("createEmptyMono",Mono.empty());
    }

    @Test
    public void createMonoThatEmitsNothing() {
        logMono("createMonoThatEmitsNothing", Mono.never());
    }

    @Test
    public void createMonoFromSingleValue() {
        logMono("createMonoFromSingleValue", Mono.just("foo"));
    }

    @Test
    public void createErrorMono() {
       logMono("createErrorMono", Mono.error(new IllegalStateException()));
    }


    public void logMono(String testName, Mono<?> mono) {
        System.out.println("*************** Start of " + testName + "*************");
        mono.log().subscribe(System.out::println);
        System.out.println("*************** End of " + testName + "*************");
    }
}
