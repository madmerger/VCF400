package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
/** Java implementation of RPG program BEEMOVIE. */
public class BeeMovieService {
    private final BeeMovieLineRepository lines;
    public BeeMovieService(BeeMovieLineRepository lines) { this.lines=lines; }
    /** RPG: BEGSR LOADSR; BR-BEEMOVIE-01/02. */
    public List<BeeMovieLine> all() { return lines.findAllByOrderByLine(); }
}
