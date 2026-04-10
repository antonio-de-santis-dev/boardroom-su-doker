package main.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SaleTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Sale getSaleSample1() {
        return new Sale().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).nome("nome1").capienza(1).descrizione("descrizione1");
    }

    public static Sale getSaleSample2() {
        return new Sale().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).nome("nome2").capienza(2).descrizione("descrizione2");
    }

    public static Sale getSaleRandomSampleGenerator() {
        return new Sale()
            .id(UUID.randomUUID())
            .nome(UUID.randomUUID().toString())
            .capienza(intCount.incrementAndGet())
            .descrizione(UUID.randomUUID().toString());
    }
}
