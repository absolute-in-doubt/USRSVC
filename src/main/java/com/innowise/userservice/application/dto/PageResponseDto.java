package com.innowise.userservice.application.dto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        boolean empty,
        boolean first,
        boolean last,
        int number,
        int numberOfElements,
        int size,
        long totalElements,
        int totalPages
) {


}

/*
{
  "content": [
    {
      "id": 2,
      "firstName": null,
      "lastName": null,
      "email": "user2@example.com",
      "active": false
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 6,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "totalElements": 6,
  "totalPages": 1
}
 */
