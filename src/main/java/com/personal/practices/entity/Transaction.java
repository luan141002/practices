package com.personal.practices.entity;

import com.personal.practices.common.constant.AuthPersistentConst;
import com.personal.practices.service.SnowflakeIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Data
@Entity
@Table(name = "transaction")
public class Transaction implements GeneralEntity {
    @Id
    @Column
    @GenericGenerator(name = AuthPersistentConst.SNOWFLAKE_ID_GENERATOR,
            type = SnowflakeIdGenerator.class,
            parameters = {
                    @Parameter(name = "machineIdField", value = "testMachineId")
            }
    )
    @GeneratedValue(
            generator = AuthPersistentConst.SNOWFLAKE_ID_GENERATOR,
            strategy = GenerationType.SEQUENCE
    )
    private Long id;

    @Column
    private int machineId;

    @Column
    private int testMachineId;

    @Override
    public int getMachineId() {
        return machineId;
    }
}
