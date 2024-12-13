# Base image with PostgreSQL
FROM pgedge/pgedge:98350c8

# Set environment variables for PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=mydb

## Install Python3 and necessary tools
#RUN apt-get update && apt-get install -y \
#    python3 \
#    curl \
#    && rm -rf /var/lib/apt/lists/*

# Install PGEdge
RUN pgedge  install pg16 --start && \
                   pgedge install snowflake
#        /pgedge/pgedge install pg16 --start && \
#        /pgedge/pgedge install snowflake
#
### Add SQL script to initialize Snowflake extension
RUN echo "CREATE EXTENSION snowflake;" > /docker-entrypoint-initdb.d/init-snowflake.sql

# Expose PostgreSQL default port
EXPOSE 5432

# Set default command to start PostgreSQL server
CMD ["postgres"]
