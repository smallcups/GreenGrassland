package com.greengrassland.service;

import com.greengrassland.dto.PostCreateDTO;
import com.greengrassland.dto.PostDTO;
import com.greengrassland.dto.PostSearchDTO;
import com.greengrassland.dto.PageDTO;
import com.greengrassland.entity.PostType;
import com.greengrassland.repository.*;
import com.greengrassland.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceImplTest {

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostRepository postRepository;

    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("Test Post")
                .content("Test content")
                .type(PostType.BALL_GAME)
                .maxPeople(10)
                .location("Test location")
                .build();
        postService.createPost(userId, dto);
    }

    @Test
    void testCreatePost() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("New Post")
                .content("New content")
                .type(PostType.BOARD_GAME)
                .maxPeople(5)
                .location("Board game room")
                .build();
        PostDTO result = postService.createPost(userId, dto);
        assertNotNull(result.getId());
        assertEquals("New Post", result.getTitle());
        assertEquals(PostType.BOARD_GAME, result.getType());
    }

    @Test
    void testCreatePostWithGeo() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("Geo Post")
                .content("Geo content")
                .type(PostType.BALL_GAME)
                .maxPeople(8)
                .location("Somewhere")
                .latitude(39.981)
                .longitude(116.347)
                .build();
        PostDTO result = postService.createPost(userId, dto);
        assertEquals(39.981, result.getLatitude());
        assertEquals(116.347, result.getLongitude());
    }

    @Test
    void testSearchPosts() {
        PostSearchDTO searchDTO = PostSearchDTO.builder()
                .keyword("Test")
                .page(1)
                .pageSize(10)
                .build();
        PageDTO<PostDTO> result = postService.searchPosts(searchDTO, userId);
        assertTrue(result.getTotalElements() > 0);
    }

    @Test
    void testSearchPostsByDistance() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("Nearby Post")
                .content("Nearby")
                .type(PostType.BALL_GAME)
                .maxPeople(5)
                .location("Nearby Place")
                .latitude(39.99)
                .longitude(116.35)
                .build();
        postService.createPost(userId, dto);

        PostSearchDTO searchDTO = PostSearchDTO.builder()
                .userLat(39.981)
                .userLng(116.347)
                .page(1)
                .pageSize(10)
                .build();
        PageDTO<PostDTO> result = postService.searchPosts(searchDTO, userId);
        assertTrue(result.getContent().stream().anyMatch(p -> p.getDistance() != null));
    }

    @Test
    void testGetPostDetail() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("Detail Post")
                .content("Detail content")
                .type(PostType.BALL_GAME)
                .maxPeople(3)
                .location("Detail place")
                .build();
        PostDTO created = postService.createPost(userId, dto);

        PostDTO result = postService.getPostDetail(created.getId(), userId);
        assertEquals("Detail Post", result.getTitle());
        assertEquals("Detail content", result.getContent());
    }

    @Test
    void testGetPostList() {
        var posts = postService.getPostList(userId);
        assertFalse(posts.isEmpty());
    }

    @Test
    void testDeletePost() {
        PostCreateDTO dto = PostCreateDTO.builder()
                .title("Delete Me")
                .content("To be deleted")
                .type(PostType.BALL_GAME)
                .maxPeople(2)
                .location("Nowhere")
                .build();
        PostDTO created = postService.createPost(userId, dto);
        postService.deletePost(created.getId(), userId);
        assertThrows(Exception.class, () -> postService.getPostDetail(created.getId(), userId));
    }
}
