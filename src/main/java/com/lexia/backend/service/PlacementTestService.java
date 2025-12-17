package com.lexia.backend.service;

import com.lexia.backend.dto.placement.PlacementQuestionResponseDTO;
import com.lexia.backend.dto.placement.PlacementTestResultDTO;
import com.lexia.backend.dto.placement.PlacementTestSubmissionDTO;
import com.lexia.backend.entity.User;

import java.util.List;

public interface PlacementTestService {
    List<PlacementQuestionResponseDTO> getPlacementQuestions();
    PlacementTestResultDTO submitPlacementTest(User user, PlacementTestSubmissionDTO submission);
}
