package me.huiya.project.Repository;

import me.huiya.project.Entity.Password;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<Password, Integer> {
    public Password findAllById(Integer id);
}