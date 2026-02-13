package com.koreanit.spring.post;

import com.koreanit.spring.common.response.ApiResponse;
import com.koreanit.spring.post.dto.request.PostCreateRequest;
import com.koreanit.spring.post.dto.request.PostUpdateRequest;
import com.koreanit.spring.post.dto.response.PostListResponse;
import com.koreanit.spring.post.dto.response.PostResponse;
import com.koreanit.spring.security.SecurityUtils;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping
  public ApiResponse<PostResponse> create(@RequestBody @Valid PostCreateRequest req) {
    Long userId = SecurityUtils.currentUserId();
    Post p = postService.create(userId, req.getTitle(), req.getContent());
    return ApiResponse.ok(PostMapper.toResponse(p));
  }

  @GetMapping
  public ApiResponse<PostListResponse> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    PostListResult result = postService.list(page, limit);

    return ApiResponse.ok(new PostListResponse(
        PostMapper.toResponseList(result.getPosts()),
        result.getPage(),
        result.getLimit(),
        result.getTotalCount(),
        result.getTotalPages()));
  }

  @GetMapping("/{id}")
  public ApiResponse<PostResponse> get(@PathVariable long id) {
    Post p = postService.get(id);
    return ApiResponse.ok(PostMapper.toResponse(p));
  }

  @PutMapping("/{id}")
  public ApiResponse<PostResponse> update(@PathVariable long id, @RequestBody @Valid PostUpdateRequest req) {
    Post p = postService.update(id, req.getTitle(), req.getContent());
    return ApiResponse.ok(PostMapper.toResponse(p));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    postService.delete(id);
    return ApiResponse.ok();
  }
}