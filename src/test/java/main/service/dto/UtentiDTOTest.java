package main.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UtentiDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(UtentiDTO.class);
        UtentiDTO utentiDTO1 = new UtentiDTO();
        utentiDTO1.setId(UUID.randomUUID());
        UtentiDTO utentiDTO2 = new UtentiDTO();
        assertThat(utentiDTO1).isNotEqualTo(utentiDTO2);
        utentiDTO2.setId(utentiDTO1.getId());
        assertThat(utentiDTO1).isEqualTo(utentiDTO2);
        utentiDTO2.setId(UUID.randomUUID());
        assertThat(utentiDTO1).isNotEqualTo(utentiDTO2);
        utentiDTO1.setId(null);
        assertThat(utentiDTO1).isNotEqualTo(utentiDTO2);
    }
}
