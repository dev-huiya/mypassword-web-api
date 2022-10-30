package me.huiya.project.Repository;

import me.huiya.project.Entity.Password;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PasswordRepository extends JpaRepository<Password, Integer> {
    public Password findAllByIdAndUserId(Integer id, Integer userId);

    // 리스트에서는 비밀번호를 감춘다.
    @Query("SELECT new Password(p.id, p.userId, p.url, p.username) FROM Password p WHERE p.userId = :userId")
    public Page<Password> getListByUserId(Integer userId, Pageable pageable);
}