package com.ttvz.se2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Temporary: fails on purpose to check that CI uploads the backend test
// reports. The next commit removes it.
class CiReportUploadCheckTest {

    @Test
    void failsOnPurpose() {
        assertEquals(1, 2, "Deliberate failure to check the test report upload in CI");
    }
}
