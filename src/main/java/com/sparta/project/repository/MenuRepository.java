package com.sparta.project.repository;

import com.sparta.project.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, String>, QuerydslPredicateExecutor<Menu> {
}