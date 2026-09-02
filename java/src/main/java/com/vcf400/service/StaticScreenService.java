package com.vcf400.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
/** Java implementation of RPG programs CREDITS, NTRSTIT and PARAMETER. */
public class StaticScreenService {
    /** RPG: CREDITS SCREEN1; BR-CREDITS-01. */
    public Map<String,String> credits() { return Map.of("title","VCF/400 - Credits and Copyright","text","VCF/400 V1R0 - Copyright (c) 2023--2024 The Little Beige Box"); }
    /** RPG: NTRSTIT INTERTEST; BR-NTRSTIT-01. */
    public Map<String,String> help() { return Map.of("title","Instructions","text","Use F5 to submit and F12 to cancel."); }
    /** RPG: PARAMETER TESTPARM; BR-PARAMETER-01. */
    public Map<String,String> parameters(String first,String second) { return Map.of("first",first==null?"":first,"second",second==null?"":second); }
}
