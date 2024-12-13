-- Create a sequence for generating unique IDs
CREATE SEQUENCE IF NOT EXISTS snowflake_seq
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 1024
    MINVALUE 1
    CACHE 10
    CYCLE;

-- Function to get constant values
CREATE OR REPLACE FUNCTION get_constant(key TEXT)
    RETURNS BIGINT AS
$$
BEGIN
    CASE key
        WHEN 'TIMESTAMP_BITS' THEN RETURN 22;
        WHEN 'MACHINE_BITS' THEN RETURN 12;
        WHEN 'SEQUENCE_BITS' THEN RETURN 10;
        WHEN 'MAX_MACHINE_ID' THEN RETURN (1 << 12) - 1;
        WHEN 'MAX_SEQUENCE' THEN RETURN (1 << 10) - 1;
        WHEN 'EPOCH' THEN RETURN 1733988555108;
        ELSE RAISE EXCEPTION 'Invalid constant key: %', key;
        END CASE;
END;
$$ LANGUAGE plpgsql;

-- Main function to generate Snowflake IDs
CREATE OR REPLACE FUNCTION next_id(machine_id INT)
    RETURNS bigint AS
$$
DECLARE
    TIMESTAMP_BITS CONSTANT INT    := get_constant('TIMESTAMP_BITS'); -- Bits for timestamp
    MACHINE_BITS   CONSTANT INT    := get_constant('MACHINE_BITS'); -- Bits for machine ID
    SEQUENCE_BITS  CONSTANT INT    := get_constant('SEQUENCE_BITS'); -- Bits for sequence

    MAX_MACHINE_ID CONSTANT INT    := get_constant('MAX_MACHINE_ID');
    MAX_SEQUENCE   CONSTANT BIGINT := get_constant('MAX_SEQUENCE');
    EPOCH          CONSTANT BIGINT := get_constant('EPOCH'); -- Custom epoch

    -- Variables for Snowflake ID components
    current_time_ms         BIGINT;
    sequence_id             BIGINT;
    snowflake_id            BIGINT;
BEGIN
    -- Validate input
    IF machine_id < 0 OR machine_id > MAX_MACHINE_ID THEN
        RAISE EXCEPTION 'Machine ID must be between 0 and %', MAX_MACHINE_ID;
    END IF;

    -- Get the current timestamp in milliseconds
    current_time_ms := FLOOR(EXTRACT(EPOCH FROM clock_timestamp()) * 1000) - EPOCH;

    -- Fetch the next sequence value
    sequence_id := nextval('snowflake_seq') & MAX_SEQUENCE;

    -- Construct the Snowflake ID
    snowflake_id := (current_time_ms << (MACHINE_BITS + SEQUENCE_BITS))
                        | (machine_id << SEQUENCE_BITS)
        | sequence_id;

    RETURN snowflake_id;
END;
$$ LANGUAGE plpgsql;

-- Helper function for generating IDs for specific machines
CREATE OR REPLACE FUNCTION next_id_for_machine(machine_id INT)
    RETURNS bigint AS
$$
BEGIN
    RETURN next_id(machine_id);
END;
$$ LANGUAGE plpgsql;

-- Helper function for generating IDs with default machine ID
CREATE OR REPLACE FUNCTION next_default_id()
    RETURNS bigint AS
$$
BEGIN
    RETURN next_id(1); -- Default machine ID is 1
END;
$$ LANGUAGE plpgsql;

-- Function to parse and format a Snowflake ID into its components
CREATE OR REPLACE FUNCTION format(snowflake_id BIGINT)
    RETURNS jsonb AS
$$
DECLARE
    TIMESTAMP_BITS  CONSTANT INT := get_constant('TIMESTAMP_BITS');
    MACHINE_BITS    CONSTANT INT := get_constant('MACHINE_BITS');
    SEQUENCE_BITS   CONSTANT INT := get_constant('SEQUENCE_BITS');
    TIMESTAMP_SHIFT CONSTANT INT := MACHINE_BITS + SEQUENCE_BITS;

    -- Variables for parsed components
    timestamp_ms             BIGINT;
    machine_id               INT;
    sequence                 INT;
BEGIN
    -- Parse components from the Snowflake ID
    timestamp_ms := (snowflake_id >> TIMESTAMP_SHIFT) + get_constant('EPOCH');
    machine_id := (snowflake_id >> SEQUENCE_BITS) & ((1 << MACHINE_BITS) - 1);
    sequence := snowflake_id & ((1 << SEQUENCE_BITS) - 1);

    -- Return the parsed components as JSON
    RETURN jsonb_build_object(
            'timestamp', to_timestamp(timestamp_ms / 1000),
            'machine_id', machine_id,
            'sequence', sequence
           );
END;
$$ LANGUAGE plpgsql;
