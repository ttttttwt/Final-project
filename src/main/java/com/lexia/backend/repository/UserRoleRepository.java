package com.lexia.backend.repository;

import com.lexia.backend.entity.User;
import com.lexia.backend.entity.UserRole;
import com.lexia.backend.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * Find all roles for a specific user
     * 
     * @param user the user
     * @return list of user roles
     */
    List<UserRole> findByUser(User user);

    /**
     * Find all roles for a specific user by user ID
     * 
     * @param userId the user ID
     * @return list of user roles
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") UUID userId);

    /**
     * Find all users with a specific role
     * 
     * @param roleId the role ID
     * @return list of user roles
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Integer roleId);

    /**
     * Delete all roles for a specific user
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Get role IDs for a user
     * 
     * @param userId the user ID
     * @return set of role IDs
     */
    @Query("SELECT ur.id.roleId FROM UserRole ur WHERE ur.id.userId = :userId")
    Set<Integer> findRoleIdsByUserId(@Param("userId") UUID userId);
}