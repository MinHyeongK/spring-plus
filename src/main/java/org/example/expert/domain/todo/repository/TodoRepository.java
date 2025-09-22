package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoQueryRepository {

    @Query("select t from Todo t " +
            "where (:weather is null or t.weather = :weather) " +
            "and (:startDate is null or t.modifiedAt >= :startDate)" +
            "and (:endDate is null or t.modifiedAt <= :endDate )")
    Page<Todo> getTodos(Pageable pageable,
                        @Param("weather") String weather,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

    //@Query("SELECT t FROM Todo t " +
    //        "LEFT JOIN t.user " +
    //        "WHERE t.id = :todoId")
    //Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
