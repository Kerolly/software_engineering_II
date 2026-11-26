package com.ttvz.kodeling.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@Tag(name = "system")
public class PingController {

    private final Clock clock;

    public PingController() {
        this(Clock.systemUTC());
    }

    PingController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/ping")
    @Operation(operationId = "getPing", summary = "Wake up the backend service")
    public PingResponse getPing() {
        return new PingResponse("ok", Instant.now(clock));
    }
}
