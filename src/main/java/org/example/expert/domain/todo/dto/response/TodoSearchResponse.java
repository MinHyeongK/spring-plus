package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final Long id;
    private final String title;
    private final Long managersCount;
    private final Long commentsCount;

    public TodoSearchResponse(Long id, String title, Long managersCount, Long commentsCount) {
        this.id = id;
        this.title = title;
        this.managersCount = managersCount;
        this.commentsCount = commentsCount;
    }
}
