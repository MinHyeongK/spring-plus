package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(Pageable pageable,
                                                String title,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                String nickname){

        BooleanBuilder builder = new BooleanBuilder();

        // 제목 조건 (부분 일치)
        if (title != null && !title.isBlank()) {
            builder.and(todo.title.contains(title));
        }

        // 생성일 범위 조건
        if (startDate != null && endDate != null) {
            builder.and(todo.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        } else if (startDate != null) {
            builder.and(todo.createdAt.goe(startDate.atStartOfDay()));
        } else if (endDate != null) {
            builder.and(todo.createdAt.loe(endDate.atTime(23, 59, 59)));
        }

        // 담당자 닉네임 조건 (부분 일치)
        if (nickname != null && !nickname.isBlank()) {
            builder.and(user.nickname.contains(nickname));
        }

        List<TodoSearchResponse> results = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.id,
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .join(todo.user, user)
                .where(builder)
                .groupBy(todo.id) // count 때문에 groupBy 필요
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리
        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .join(todo.user)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0L : total);
    }
}
