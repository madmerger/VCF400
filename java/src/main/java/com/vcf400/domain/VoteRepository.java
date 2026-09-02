package com.vcf400.domain;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VoteRepository extends JpaRepository<Vote, Integer> { long countByAwardNbr(Integer award); long countByExhNbr(String exhibit); }
