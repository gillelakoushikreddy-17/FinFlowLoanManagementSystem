package com.finflow.admin_service.repository;

import com.finflow.admin_service.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
