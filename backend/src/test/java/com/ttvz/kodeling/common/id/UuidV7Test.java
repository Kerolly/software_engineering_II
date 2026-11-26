package com.ttvz.kodeling.common.id;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UuidV7Test {

    @Test
    void generatesVersion7Rfc9562UuidsInMonotonicOrder() {
        UUID first = UuidV7.generate();
        UUID second = UuidV7.generate();

        assertThat(first.version()).isEqualTo(7);
        assertThat(first.variant()).isEqualTo(2);
        assertThat(second.version()).isEqualTo(7);
        assertThat(second.variant()).isEqualTo(2);
        assertThat(second.toString()).isGreaterThan(first.toString());
    }

}
