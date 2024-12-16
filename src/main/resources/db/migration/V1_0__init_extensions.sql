-- Create a sequence for generating unique IDs
CREATE SEQUENCE IF NOT EXISTS snowflake_seq
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 4096
    MINVALUE 1
    CACHE 1000
    CYCLE ;

-- Main function to generate Snowflake IDs
CREATE OR REPLACE FUNCTION generate_snowflake_id(machine_id INT DEFAULT 1)
    RETURNS bigint AS $$
DECLARE
    MAX_MACHINE_ID CONSTANT INT := (1 << 10) - 1;
    current_time_ms BIGINT;
    sequence_id BIGINT;
    snowflake_id BIGINT;
BEGIN
    IF machine_id < 0 OR machine_id > MAX_MACHINE_ID THEN
        RAISE EXCEPTION 'Machine ID must be between 0 and %', MAX_MACHINE_ID;
    END IF;
    current_time_ms := (EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::BIGINT;
    sequence_id := nextval('snowflake_seq') & ((1 << 12) - 1);
    snowflake_id := (current_time_ms << 22)
                    | (machine_id << 12)
                    | sequence_id;
    RETURN snowflake_id;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION get_epoch(snowflake_id BIGINT)
    RETURNS TIMESTAMP AS $$
BEGIN
    RETURN to_timestamp((snowflake_id >> 22) / 1000); -- 22 = MACHINE_ID_BITS (10) + SEQUENCE_BITS (12)
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_machine_id(snowflake_id BIGINT)
    RETURNS INT AS $$
BEGIN
    RETURN (snowflake_id >> 12) & 1023; -- Mask for 10 bits (2^10 - 1 = 1023)
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_sequence(snowflake_id BIGINT)
    RETURNS INT AS $$
BEGIN
    RETURN snowflake_id & 4095; -- Mask for 12 bits (2^12 - 1 = 4095)
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION format_snowflake_id(snowflake_id BIGINT)
    RETURNS jsonb AS $$
BEGIN
    RETURN jsonb_build_object(
            'timestamp', get_epoch(snowflake_id),
            'machine_id', get_machine_id(snowflake_id),
            'sequence', get_sequence(snowflake_id)
           );
END;
$$ LANGUAGE plpgsql;
