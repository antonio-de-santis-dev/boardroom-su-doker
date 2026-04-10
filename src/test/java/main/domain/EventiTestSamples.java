package main.domain;

import java.util.UUID;

public class EventiTestSamples {

    public static Eventi getEventiSample1() {
        return new Eventi().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).titolo("titolo1");
    }

    public static Eventi getEventiSample2() {
        return new Eventi().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).titolo("titolo2");
    }

    public static Eventi getEventiRandomSampleGenerator() {
        return new Eventi().id(UUID.randomUUID()).titolo(UUID.randomUUID().toString());
    }
}
