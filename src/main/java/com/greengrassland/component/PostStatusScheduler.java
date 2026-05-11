package com.greengrassland.component;

import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostStatus;
import com.greengrassland.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostStatusScheduler {

    private final PostRepository postRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updatePostStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<PostStatus> activeStatuses = List.of(PostStatus.RECRUITING, PostStatus.FULL, PostStatus.ONGOING);

        int page = 0;
        int updated = 0;
        while (true) {
            Page<Post> postPage = postRepository.findByStatusInAndActivityTimeBefore(
                    activeStatuses, now, PageRequest.of(page, 100));
            for (Post post : postPage.getContent()) {
                post.setStatus(PostStatus.FINISHED);
                postRepository.save(post);
                updated++;
            }
            if (!postPage.hasNext()) break;
            page++;
        }

        if (updated > 0) {
            log.info("Auto-updated {} posts to FINISHED", updated);
        }
    }
}
