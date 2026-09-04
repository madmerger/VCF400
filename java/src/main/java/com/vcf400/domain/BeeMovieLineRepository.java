package com.vcf400.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeeMovieLineRepository extends JpaRepository<BeeMovieLine, String> {

    List<BeeMovieLine> findAllByOrderByLine();
}
