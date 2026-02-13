package com.koreanit.spring.post.dto.response;

import java.util.List;

public class PostListResponse {
  private final List<PostResponse> items;
  private final int page;
  private final int limit;
  private final long totalCount;
  private final int totalPages;

  public PostListResponse(List<PostResponse> items, int page, int limit, long totalCount, int totalPages) {
    this.items = items;
    this.page = page;
    this.limit = limit;
    this.totalCount = totalCount;
    this.totalPages = totalPages;
  }

  public List<PostResponse> getItems() {
    return items;
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
