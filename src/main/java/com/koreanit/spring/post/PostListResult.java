package com.koreanit.spring.post;

import java.util.List;

public class PostListResult {
  private final List<Post> posts;
  private final int page;
  private final int limit;
  private final long totalCount;
  private final int totalPages;

  public PostListResult(List<Post> posts, int page, int limit, long totalCount, int totalPages) {
    this.posts = posts;
    this.page = page;
    this.limit = limit;
    this.totalCount = totalCount;
    this.totalPages = totalPages;
  }

  public List<Post> getPosts() {
    return posts;
  }

  public int getPage() {
    return page;
  }

  public int getLimit() {
    return limit;
  }

  public long getTotalCount() {
    return totalCount;
  }

  public int getTotalPages() {
    return totalPages;
  }
}
