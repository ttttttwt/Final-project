package com.lexia.backend.service;

import com.lexia.backend.dto.ActivityDTO;
import com.lexia.backend.dto.AdminDashboardDTO;
import com.lexia.backend.repository.CourseRepository;
import com.lexia.backend.repository.EnrollmentRepository;
import com.lexia.backend.repository.LessonRepository;
import com.lexia.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AdminDashboardDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalLessons = lessonRepository.count();
        long activeEnrollments = enrollmentRepository.count(); // TODO: Filter by active status if applicable

        // Mock recent activities for now
        List<ActivityDTO> recentActivities = new ArrayList<>();

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalLessons(totalLessons)
                .activeEnrollments(activeEnrollments)
                .recentActivities(recentActivities)
                .build();
    }
}
