package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    // The audit log grows without bound, so unlike other lists it is searched and paged in SQL.
    // q is never null: an untyped null parameter reaches PostgreSQL as bytea and lower() fails.
    @Query("""
           select a from AuditEntry a
           where :q = ''
              or lower(a.actor) like lower(concat('%', :q, '%'))
              or lower(a.path) like lower(concat('%', :q, '%'))
           order by a.at desc, a.auditId desc
           """)
    Page<AuditEntry> search(@Param("q") String q, Pageable pageable);
}
