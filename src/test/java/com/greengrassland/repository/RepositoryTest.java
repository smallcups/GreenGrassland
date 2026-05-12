package com.greengrassland.repository;

import com.greengrassland.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryTest {

    @Autowired private PostRepository postRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private ReportRepository reportRepository;

    @Test
    void testPostWithNewFields() {
        Post post = Post.builder().userId(1L).title("Series Event").content("Weekly").type(PostType.BALL_GAME)
                .maxPeople(10).location("Gym").activityTime(LocalDateTime.now().plusDays(7))
                .approvalMode(true).isSeries(true).build();
        Post saved = postRepository.save(post);
        assertTrue(saved.getApprovalMode());
        assertTrue(saved.getIsSeries());
    }

    @Test
    void testUserBlock() {
        UserBlock block = UserBlock.builder().userId(1L).blockedUserId(2L).build();
        userBlockRepository.save(block);
        assertTrue(userBlockRepository.existsByUserIdAndBlockedUserId(1L, 2L));
        userBlockRepository.deleteByUserIdAndBlockedUserId(1L, 2L);
        assertFalse(userBlockRepository.existsByUserIdAndBlockedUserId(1L, 2L));
    }

    @Test
    void testReport() {
        Report report = Report.builder().reporterId(1L).targetType("POST").targetId(1L).reason("spam").build();
        Report saved = reportRepository.save(report);
        assertNotNull(saved.getId());
        assertEquals("POST", saved.getTargetType());
    }
}
