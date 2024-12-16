package com.personal.practices.service.machine_id_handler;

import com.personal.practices.entity.GeneralEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMachineIdResolver implements MachineIdResolver {
    @Override
    public Integer resolveMachineId(GeneralEntity<?> entity) {
        try {
            if (entity.getMachineId() instanceof Integer resolvedMachineId && resolvedMachineId > 0) {
                log.info("Resolved machineId from GeneralEntity: {}", resolvedMachineId);
                return resolvedMachineId;
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast machineId to Integer: {}", e.getMessage());
            throw new RuntimeException("Failed to cast machineId to Integer: " + e.getMessage());
        }
        return null;
    }
}
