package com.coddicted.buzzma.shared.common;

import java.io.Serializable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable, Serializable {

  private final int limit;
  private final long offset;
  private final Sort sort;

  public OffsetBasedPageRequest(int limit, long offset, Sort sort) {
    if (limit < 1) {
      throw new IllegalArgumentException("Limit must be at least 1");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must not be negative");
    }
    this.limit = limit;
    this.offset = offset;
    this.sort = sort;
  }

  @Override
  public int getPageNumber() {
    return (int) (offset / limit);
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    return new OffsetBasedPageRequest(limit, offset + limit, sort);
  }

  @Override
  public Pageable previousOrFirst() {
    return hasPrevious() ? new OffsetBasedPageRequest(limit, offset - limit, sort) : first();
  }

  @Override
  public Pageable first() {
    return new OffsetBasedPageRequest(limit, 0, sort);
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return new OffsetBasedPageRequest(limit, (long) pageNumber * limit, sort);
  }

  @Override
  public boolean hasPrevious() {
    return offset > 0;
  }
}
