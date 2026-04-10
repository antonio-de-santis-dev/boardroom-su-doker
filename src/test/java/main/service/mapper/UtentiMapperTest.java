package main.service.mapper;

import static main.domain.UtentiAsserts.*;
import static main.domain.UtentiTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtentiMapperTest {

    private UtentiMapper utentiMapper;

    @BeforeEach
    void setUp() {
        utentiMapper = new UtentiMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getUtentiSample1();
        var actual = utentiMapper.toEntity(utentiMapper.toDto(expected));
        assertUtentiAllPropertiesEquals(expected, actual);
    }
}
