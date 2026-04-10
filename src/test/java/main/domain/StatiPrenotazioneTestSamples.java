package main.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StatiPrenotazioneTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static StatiPrenotazione getStatiPrenotazioneSample1() {
        return new StatiPrenotazione()
            .id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .descrizione("descrizione1")
            .ordineAzione(1);
    }

    public static StatiPrenotazione getStatiPrenotazioneSample2() {
        return new StatiPrenotazione()
            .id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .descrizione("descrizione2")
            .ordineAzione(2);
    }

    public static StatiPrenotazione getStatiPrenotazioneRandomSampleGenerator() {
        return new StatiPrenotazione()
            .id(UUID.randomUUID())
            .descrizione(UUID.randomUUID().toString())
            .ordineAzione(intCount.incrementAndGet());
    }
}
