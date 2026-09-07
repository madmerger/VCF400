package com.vcf400.web;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Setting;
import com.vcf400.domain.Vote;
import com.vcf400.repository.GuestbookRepository;
import com.vcf400.repository.SettingsRepository;
import com.vcf400.repository.VoteRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * クロス検証 (フェーズ5) 用の DB 状態参照/テストデータ操作 API。
 * PUB400 側の SQL (db2) 照会と同じ内容を JSON で返す。業務画面からは使用しない。
 */
@RestController
@RequestMapping("/api/db")
@ConditionalOnProperty(name = "vcf.db-api.enabled", havingValue = "true", matchIfMissing = false)
public class DbApiController {
    private final VoteRepository votes;
    private final GuestbookRepository comments;
    private final SettingsRepository settings;

    public DbApiController(VoteRepository votes, GuestbookRepository comments, SettingsRepository settings) {
        this.votes = votes;
        this.comments = comments;
        this.settings = settings;
    }

    @GetMapping("/votes")
    public List<Vote> votes(HttpServletRequest request) {
        requireLocal(request);
        return votes.findAll();
    }

    @DeleteMapping("/votes/{badge}")
    public Map<String, Integer> deleteVote(@PathVariable int badge, HttpServletRequest request) {
        requireLocal(request);
        return Map.of("deleted", votes.deleteByBadge(badge));
    }

    @GetMapping("/comments")
    public List<GuestbookComment> comments(HttpServletRequest request) {
        requireLocal(request);
        return comments.findAll();
    }

    @DeleteMapping("/comments/{id}")
    public Map<String, Integer> deleteComment(@PathVariable int id, HttpServletRequest request) {
        requireLocal(request);
        return Map.of("deleted", comments.deleteById(id));
    }

    @PutMapping("/comments/{id}/visible")
    public Map<String, Integer> setVisible(@PathVariable int id, @RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        requireLocal(request);
        return Map.of("updated", comments.updateVisible(id, body.getOrDefault("value", "Y")));
    }

    @GetMapping("/settings")
    public List<Setting> settings(HttpServletRequest request) {
        requireLocal(request);
        return settings.findAll();
    }

    @PutMapping("/settings/{name}")
    public ResponseEntity<Map<String, Integer>> setSetting(@PathVariable String name,
                                                           @RequestBody Map<String, String> body,
                                                           HttpServletRequest request) {
        requireLocal(request);
        return ResponseEntity.ok(Map.of("updated", settings.update(name.toUpperCase(), body.getOrDefault("value", ""))));
    }

    private static void requireLocal(HttpServletRequest request) {
        try {
            if (!InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Remote address is not local", e);
        }
    }
}
