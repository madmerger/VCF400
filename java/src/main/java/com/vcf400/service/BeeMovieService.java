package com.vcf400.service;

import java.util.List;

import com.vcf400.domain.BeeMovieLine;
import com.vcf400.domain.BeeMovieLineRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program BEEMOVIE. */
public class BeeMovieService {

    private final BeeMovieLineRepository lines;

    public BeeMovieService(BeeMovieLineRepository lines) {
        this.lines = lines;
    }

    /** RPG: BEGSR LOADSR; BR-BEEMOVIE-01, BR-BEEMOVIE-02. */
    public List<BeeMovieLine> all() {
        return lines.findAllByOrderByLine();
    }
}
