package com.ttvz.kodeling.system;

import java.time.Instant;

public record PingResponse(String status, Instant timestamp) {
}
