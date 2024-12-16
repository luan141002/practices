package com.personal.practices.service.machine_id_handler;

import com.personal.practices.entity.GeneralEntity;

public interface MachineIdResolver {
    Integer resolveMachineId(GeneralEntity<?> entity);
}
