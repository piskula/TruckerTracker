package sk.momosilabs.truckTrack.checklist.persistence

import liquibase.change.core.CreateIndexChange
import liquibase.changelog.ChangeLogParameters
import liquibase.database.core.PostgresDatabase
import liquibase.parser.ChangeLogParserFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChecklistMigrationTest {

    @Test
    fun `checklist indexes define valid PostgreSQL columns`() {
        ClassLoaderResourceAccessor().use { resourceAccessor ->
            val path = "db/changelog/2026/11/11-23-checklist.yaml"
            val changeLog = ChangeLogParserFactory.getInstance()
                .getParser(path, resourceAccessor)
                .parse(path, ChangeLogParameters(), resourceAccessor)
            val indexes = changeLog.changeSets
                .flatMap { it.changes }
                .filterIsInstance<CreateIndexChange>()

            assertThat(indexes.associate { index ->
                index.indexName to index.columns.map { it.name }
            }).containsExactlyInAnyOrderEntriesOf(
                mapOf(
                    "ix_checklist_response_truck_answered_at" to listOf("truck_id", "answered_at_utc"),
                    "ix_checklist_response_trailer_answered_at" to listOf("trailer_id", "answered_at_utc"),
                    "ix_checklist_response_answered_by_answered_at" to listOf("answered_by_id", "answered_at_utc")
                )
            )
            indexes.forEach { index ->
                assertThat(index.validate(PostgresDatabase()).errorMessages).isEmpty()
            }
        }
    }
}
