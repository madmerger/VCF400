package com.vcf400.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Lrn400PageRepository extends JpaRepository<Lrn400Page, Lrn400PageId> {

    List<Lrn400Page> findByOwnerOrderByPageNbr(String owner);
}
