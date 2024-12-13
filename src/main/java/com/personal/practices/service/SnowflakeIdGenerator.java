package com.personal.practices.service;

import com.personal.practices.config.security.ServiceConfig;
import com.personal.practices.entity.GeneralEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Objects;
import java.util.Properties;

@Slf4j
@Data
public class SnowflakeIdGenerator implements IdentifierGenerator, Configurable {

    private static final long serialVersionUID = 1L;
    private final ServiceConfig serviceConfig;
    private final SnowflakeIdGeneratorService snowflakeIdGeneratorService;
    public String machineIdField;


    public SnowflakeIdGenerator(@Qualifier("serviceConfig") ServiceConfig serviceConfig,
                                SnowflakeIdGeneratorService snowflakeIdGeneratorService) {
        this.serviceConfig = serviceConfig;
        this.snowflakeIdGeneratorService = snowflakeIdGeneratorService;
    }

    @Override
    public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry)
            throws MappingException {
        machineIdField = parameters.getProperty("machineIdField");
        log.info("Configured machineIdField: " + machineIdField);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        int machineId = resolveMachineId(object);

        // Set the machineId in SnowflakeIdGeneratorService
        snowflakeIdGeneratorService.setMachineId(machineId);
        return snowflakeIdGeneratorService.generateId();
    }

    /**
     * Resolves the machineId using the following priority:
     * 1. SpEL expression from the machineIdField property (if defined).
     * 2. machineId from the entity (if the object is an instance of GeneralEntity).
     * 3. Default machineId from ServiceConfig.
     *
     * @param object The entity instance.
     * @return Resolved machineId.
     */
    private int resolveMachineId(Object object) {
        int machineId = 0;
        // Fallback to machineId from GeneralEntity (if applicable)
        if (object instanceof GeneralEntity entity) {
            machineId = entity.getMachineId();
        }
        // Evaluate machineId using SpEL if machineIdField is defined
        if (machineId == 0 && Objects.nonNull(machineIdField)) {
            try {
                ExpressionParser parser = new SpelExpressionParser();
                Expression expression = parser.parseExpression(machineIdField);
                machineId = expression.getValue(object, Integer.class);
                log.info("Resolved machineId from SpEL: " + machineId);
            } catch (Exception e) {
                log.warn("Failed to evaluate SpEL for machineIdField: " + e.getMessage());
            }
        }

        // Fallback to machineId from GeneralEntity (if applicable)
        if (machineId == 0 && object instanceof GeneralEntity entity) {
            machineId = entity.getMachineId();
        }

        // Default to machineId from ServiceConfig
        if (machineId == 0) {
            machineId = serviceConfig.getMachineId();
        }

        return machineId;
    }
}
