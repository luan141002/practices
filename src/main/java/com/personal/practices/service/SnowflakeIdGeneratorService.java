package com.personal.practices.service;

import lombok.Data;

@Data
public class SnowflakeIdGeneratorService {
    private static final long TIMESTAMP_BITS = 22L;
    private static final long MACHINE_BITS = 12L;
    private static final long SEQUENCE_BITS = 10L;

    private long epoch;
    private long machineId;
    private int sequence;
    private long sequenceMask;

    public SnowflakeIdGeneratorService(int machineId) {
        this.machineId = machineId;
        this.epoch = 1733988555108L;
        this.sequence = 100;
        this.sequenceMask = ((1L << SEQUENCE_BITS) - 1);
    }

    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis() - epoch;

        var id = (timestamp << TIMESTAMP_BITS | (this.machineId << MACHINE_BITS) | this.sequence);

        var generated =
                String.format(
                        "{timestamp: %s, machineId: %s, sequence: %s}",
                        timestamp, this.machineId, this.sequence);

        System.out.println("Before generating: " + generated);

        this.sequence++;

        return id;
    }

    public String resolve(long id) {
        return String.format(
                "{timestamp: %s, machineId: %s, sequence: %s}",
                (id >> TIMESTAMP_BITS),
                ((id >> MACHINE_BITS) & this.sequenceMask),
                (id & this.sequenceMask));
    }
}
