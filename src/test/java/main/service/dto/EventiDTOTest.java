package main.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventiDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventiDTO.class);
        EventiDTO eventiDTO1 = new EventiDTO();
        eventiDTO1.setId(UUID.randomUUID());
        EventiDTO eventiDTO2 = new EventiDTO();
        assertThat(eventiDTO1).isNotEqualTo(eventiDTO2);
        eventiDTO2.setId(eventiDTO1.getId());
        assertThat(eventiDTO1).isEqualTo(eventiDTO2);
        eventiDTO2.setId(UUID.randomUUID());
        assertThat(eventiDTO1).isNotEqualTo(eventiDTO2);
        eventiDTO1.setId(null);
        assertThat(eventiDTO1).isNotEqualTo(eventiDTO2);
    }
}
