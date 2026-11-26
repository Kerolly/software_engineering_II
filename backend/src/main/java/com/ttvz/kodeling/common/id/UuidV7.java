package com.ttvz.kodeling.common.id;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;

import java.util.UUID;

public final class UuidV7 {

    private static final TimeBasedEpochRandomGenerator GENERATOR = Generators.timeBasedEpochRandomGenerator();

    private UuidV7() {
    }

    public static UUID generate() {
        return GENERATOR.generate();
    }
}
