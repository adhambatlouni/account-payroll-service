package com.adham.accountservice.audit.repository;

import com.adham.accountservice.audit.model.SecurityEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findAllByOrderByIdAsc();
}
