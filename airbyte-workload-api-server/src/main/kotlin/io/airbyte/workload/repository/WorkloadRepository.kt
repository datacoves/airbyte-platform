package io.airbyte.workload.repository

import io.airbyte.db.instance.configs.jooq.generated.enums.WorkloadStatus
import io.airbyte.workload.repository.domain.Workload
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.PageableRepository
import java.time.OffsetDateTime
import java.util.Optional

@JdbcRepository(dialect = Dialect.POSTGRES)
interface WorkloadRepository : PageableRepository<Workload, String> {
  @Join(value = "workloadLabels", type = Join.Type.LEFT_FETCH)
  override fun findById(
    @Id id: String,
  ): Optional<Workload>

  @Query(
    """
      SELECT * FROM workload
      WHERE ((:dataplaneIds) IS NULL OR dataplane_id IN (:dataplaneIds))
      AND ((:statuses) IS NULL OR status =  ANY(CAST(ARRAY[:statuses] AS workload_status[])))
      AND (CAST(:updatedBefore AS timestamptz) IS NULL OR updated_at < CAST(:updatedBefore AS timestamptz))
      
      """,
  )
  fun search(
    dataplaneIds: List<String>?,
    statuses: List<WorkloadStatus>?,
    updatedBefore: OffsetDateTime?,
  ): List<Workload>

  fun update(
    @Id id: String,
    status: WorkloadStatus,
  )

  fun update(
    @Id id: String,
    status: WorkloadStatus,
    lastHeartbeatAt: OffsetDateTime,
  )

  fun update(
    @Id id: String,
    dataplaneId: String,
    status: WorkloadStatus,
  )
}
