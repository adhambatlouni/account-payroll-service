package com.adham.accountservice.auth.repository;

import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.model.Role;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from UserAccount user where user.email = :email")
    Optional<UserAccount> findForUpdateByEmail(@Param("email") String email);

    @Query("""
            select case when count(user) > 0 then true else false end
            from UserAccount user join user.roles role
            where role = :role
            """)
    boolean existsByRole(@Param("role") Role role);

    boolean existsByEmailIgnoreCase(String email);

    List<UserAccount> findAllByOrderByIdAsc();
}
