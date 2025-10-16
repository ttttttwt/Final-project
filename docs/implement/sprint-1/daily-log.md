# LEXIA Sprint 1 - Daily Log

**Date**: October 16, 2025
**Sprint Day**: 1/10

## 🎯 Today's Focus

Database Setup - Flyway Migration Scripts

## ✅ Completed Tasks

### Database Schema Migration ✅

- **Task**: Create Flyway migration scripts for initial schema (users, user_profiles, roles, user_roles, refresh_tokens)
- **Details**:
  - Created `V1__Initial_schema.sql` with complete table definitions
  - Added UUID extension support
  - Implemented proper foreign key relationships
  - Added indexes for performance
  - Created trigger for automatic `updated_at` timestamps
  - Inserted default roles (LEARNER, ADMIN, CONTENT_MANAGER)
- **Files Modified**:
  - `src/main/resources/db/migration/V1__Initial_schema.sql` (created)
  - `build.gradle` (added Flyway dependencies)
  - `src/main/resources/application.properties` (added Flyway config)
  - `src/test/resources/application.properties` (created for H2 testing)
- **Testing**: Build passes with H2 test database
- **Time Spent**: 45 minutes

### JPA Entities Creation ✅

- **Task**: Create JPA entities (User, UserProfile, Role, UserRole, RefreshToken)
- **Details**:
  - Created `User` entity with UUID primary key, relationships, and enums
  - Created `UserProfile` entity with one-to-one relationship to User
  - Created `Role` entity with enum for role names
  - Created `UserRole` junction entity for many-to-many relationship
  - Created `RefreshToken` entity with token management methods
  - Added proper JPA annotations, Lombok annotations, and validation
  - Configured bidirectional relationships and cascading
- **Files Created**:
  - `src/main/java/com/lexia/backend/entity/User.java`
  - `src/main/java/com/lexia/backend/entity/UserProfile.java`
  - `src/main/java/com/lexia/backend/entity/Role.java`
  - `src/main/java/com/lexia/backend/entity/UserRole.java`
  - `src/main/java/com/lexia/backend/entity/RefreshToken.java`
- **Testing**: Compilation successful, tests pass with H2 database
- **Time Spent**: 30 minutes

### Repository Interfaces Creation ✅

- **Task**: Create repository interfaces for all JPA entities
- **Details**:
  - Created UserRepository with email-based queries and auth provider counting
  - Created UserProfileRepository for user profile management
  - Created RoleRepository with name-based lookups
  - Created UserRoleRepository with composite key queries and role management
  - Created RefreshTokenRepository with token lifecycle management (expiration, revocation, family rotation)
  - Added custom query methods for business logic requirements
  - Included @Modifying annotations for update/delete operations
- **Files Created**:
  - `src/main/java/com/lexia/backend/repository/UserRepository.java`
  - `src/main/java/com/lexia/backend/repository/UserProfileRepository.java`
  - `src/main/java/com/lexia/backend/repository/RoleRepository.java`
  - `src/main/java/com/lexia/backend/repository/UserRoleRepository.java`
  - `src/main/java/com/lexia/backend/repository/RefreshTokenRepository.java`
- **Testing**: Compilation successful, all tests pass
- **Time Spent**: 20 minutes

## 🔄 Current Status

- **Database Layer**: ✅ Complete (Schema + Entities + Repositories)
- **Next Priority**: JWT Provider implementation
- **Blockers**: None

## 📝 Notes

- Flyway configured to run automatically on startup
- Test configuration uses H2 in-memory database
- Migration scripts follow Flyway naming conventions
- All tables include proper constraints and indexes
- All entities use Lombok for boilerplate reduction
- Proper JPA relationships configured (OneToOne, OneToMany, ManyToOne, ManyToMany)
- RefreshToken includes utility methods for expiration and revocation checks
- Entities compile successfully and integrate with Hibernate
- Test database shows proper table creation/dropping
- Added Jakarta Validation annotations for input validation (@Email, @NotBlank, @Size, @NotNull)
- Configured orphanRemoval for proper cascading behavior
- UserRole uses @EmbeddedId with composite key for many-to-many relationship
- Validation constraints match database schema constraints for consistency
- Repository interfaces provide comprehensive data access layer with custom queries
- RefreshTokenRepository includes token lifecycle management (rotation, expiration, revocation)

## 🎯 Tomorrow's Plan

- Implement JWT token provider service
- Create JWT utility classes for token generation/validation
- Start working on authentication endpoints
