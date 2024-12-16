package com.personal.practices.service;

import com.personal.practices.entity.GeneralEntity;
import com.personal.practices.service.machine_id_handler.DefaultMachineIdResolver;
import com.personal.practices.service.machine_id_handler.MachineIdResolver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Qualifier;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Slf4j
@Data
public class SnowflakeIdGenerator implements IdentifierGenerator, Configurable {

    private static final long serialVersionUID = 1L;
    private final Map<Class<?>, MachineIdResolver> machineIdHandlers;

    public SnowflakeIdGenerator(
            @Qualifier("handlerRegister")
            Map<Class<?>, MachineIdResolver> machineIdHandlers) {
        this.machineIdHandlers = machineIdHandlers;
    }

    @Override
    public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry)
            throws MappingException {
        // No parameter
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        Integer resolvedMachineId = getMachineId(object);
        JdbcConnectionAccess jdbcConnectionAccess = null;
        String query = prepareSqlQueryFrom(resolvedMachineId);
        try {
            jdbcConnectionAccess = session.getJdbcConnectionAccess();
            Connection connection = jdbcConnectionAccess.obtainConnection();
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            Long id = null;

            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }

            resultSet.close();
            statement.close();

            jdbcConnectionAccess.releaseConnection(connection);

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String prepareSqlQueryFrom(Integer resolvedMachineId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT generate_snowflake_id(");
        if (Objects.nonNull(resolvedMachineId)) {
            queryBuilder.append(resolvedMachineId);
        }
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    private Integer getMachineId(Object object) {
        if (object instanceof GeneralEntity<?> entity) {
            var resolver = getMachineIdResolver(entity);
            return resolver.resolveMachineId(entity);
        }
        return null;
    }

    public MachineIdResolver getMachineIdResolver(GeneralEntity<?> entity) {
        if (entity == null) {
            return new DefaultMachineIdResolver();
        }
        return machineIdHandlers.getOrDefault(entity.getClass(), new DefaultMachineIdResolver());
    }

}
