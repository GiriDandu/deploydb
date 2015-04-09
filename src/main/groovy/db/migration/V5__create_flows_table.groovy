package db.migration

import java.sql.DatabaseMetaData


/**
 * Example of a Java-based migration.
 */
class V5__create_flows_table extends DeployDBMigration {

    /** Return migration number to differentiate from other versions */
    @Override
    Integer getChecksum() {
        return 5
    }

    /**
     * Gather sql commands for this migration
     *
     * @param metadata
     * @return List of sql commands
     */
    List<String> prepareCommands(DatabaseMetaData metadata) {

        /* Sql commands */
        List<String> commands = []

        /*
         * Create flows table
         */
        if (isPostgres(metadata.driverName)) {
            commands += """
            CREATE SEQUENCE flows_id_seq;
            CREATE TABLE flows (
                id BIGINT DEFAULT nextval('flows_id_seq'),

                artifactId BIGINT NOT NULL,
                service TEXT NOT NULL,

                createdAt TIMESTAMP,
                deletedAt TIMESTAMP NULL,

                PRIMARY KEY (id)
            );
        """
        } else {
        commands += """
            CREATE TABLE flows (
                id BIGINT AUTO_INCREMENT,

                artifactId BIGINT NOT NULL,
                service TEXT NOT NULL,

                createdAt TIMESTAMP(3),
                deletedAt TIMESTAMP NULL,

                PRIMARY KEY (id)
            );
        """
        }

        /* Add flowId to deployments table */
        commands += """
            ALTER TABLE deployments ADD COLUMN flowId BIGINT;
        """

        /* Add foreign key */
        commands += """
            ALTER TABLE deployments ADD FOREIGN KEY (flowId) REFERENCES flows(id);
        """

        return commands
    }
}


