package com.vcf400.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface Lrn400PageRepository extends JpaRepository<Lrn400Page, Lrn400PageId> { List<Lrn400Page> findByOwnerOrderByPageNbr(String owner); }
