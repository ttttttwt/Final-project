package com.lexia.backend.repository;

import com.lexia.backend.entity.PlacementTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementTestQuestionRepository extends JpaRepository<PlacementTestQuestion, UUID> {
    
    @Query(value = "SELECT * FROM placement_test_questions ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementTestQuestion> findRandomQuestions(int limit);
}
