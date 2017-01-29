package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Users;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Users entity.
 */
@SuppressWarnings("unused")
public interface UsersRepository extends JpaRepository<Users,Long> {

}
