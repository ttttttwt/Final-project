# LEXIA - Current Sprint Status

**Sprint**: 1 / 6  
**Duration**: —  
**Status**: 🟡 In Progress

## Sprint Goals

- [x] Environment setup complete
- [x] Database schema migration complete
- [x] JPA entities complete
- [x] Repository interfaces complete
- [ ] JWT authentication complete
- [ ] User registration API
- [ ] User login API
- [ ] Unit tests (70%+ coverage)

## Story Breakdown

| Story                      | Status  | Owner | %Complete | Notes                                                                           |
| -------------------------- | ------- | ----- | --------- | ------------------------------------------------------------------------------- |
| Setup Spring Boot Project  | ✅ Done | You   | 100%      | All dependencies installed                                                      |
| Database Schema Migration  | ✅ Done | You   | 100%      | Flyway scripts created and configured                                           |
| JPA Entities               | ✅ Done | You   | 100%      | User, UserProfile, Role, UserRole, RefreshToken with validation & relationships |
| Repository Interfaces      | ✅ Done | You   | 100%      | All repositories with custom queries for business logic                         |
| Implement JWT Provider     | ✅ Done | You   | 100%      | JwtTokenProvider service with token generation, validation, and hashing         |
| Implement AuthService      | ✅ Done | You   | 100%      | BCrypt password hashing (cost 12), user registration, authentication            |
| User Registration Endpoint | 🔵 Todo | You   | 0%        | Blocked: waiting AuthService complete                                           |
| User Login Endpoint        | 🔵 Todo | You   | 0%        | Depends on registration                                                         |
| Unit Tests                 | 🔵 Todo | You   | 0%        | Will write after endpoints                                                      |

## Daily Standup Log
