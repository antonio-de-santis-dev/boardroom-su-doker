package main.service.mapper;

import static main.domain.EventiAsserts.*;
import static main.domain.EventiTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventiMapperTest {

    private EventiMapper eventiMapper;

    @BeforeEach
    void setUp() {
        eventiMapper = new EventiMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEventiSample1();
        var actual = eventiMapper.toEntity(eventiMapper.toDto(expected));
        assertEventiAllPropertiesEquals(expected, actual);
    }
}
