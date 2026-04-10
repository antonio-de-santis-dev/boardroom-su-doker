package main.domain;

import java.util.UUID;

public class PrenotazioniTestSamples {

    public static Prenotazioni getPrenotazioniSample1() {
        return new Prenotazioni().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"));
    }

    public static Prenotazioni getPrenotazioniSample2() {
        return new Prenotazioni().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"));
    }

    public static Prenotazioni getPrenotazioniRandomSampleGenerator() {
        return new Prenotazioni().id(UUID.randomUUID());
    }
}
