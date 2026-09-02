package com.vcf400.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BeeMovieLineRepository extends JpaRepository<BeeMovieLine, String> { List<BeeMovieLine> findAllByOrderByLine(); }
