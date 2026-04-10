package main.domain;

import java.util.UUID;

public class UtentiTestSamples {

    public static Utenti getUtentiSample1() {
        return new Utenti()
            .id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .nome("nome1")
            .nomeAzienda("nomeAzienda1")
            .numeroDiTelefono("numeroDiTelefono1");
    }

    public static Utenti getUtentiSample2() {
        return new Utenti()
            .id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .nome("nome2")
            .nomeAzienda("nomeAzienda2")
            .numeroDiTelefono("numeroDiTelefono2");
    }

    public static Utenti getUtentiRandomSampleGenerator() {
        return new Utenti()
            .id(UUID.randomUUID())
            .nome(UUID.randomUUID().toString())
            .nomeAzienda(UUID.randomUUID().toString())
            .numeroDiTelefono(UUID.randomUUID().toString());
    }
}
