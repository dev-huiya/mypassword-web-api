package me.huiya.core.Repository;

import me.huiya.core.Entity.Verify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;

public interface VerifyRepository extends JpaRepository<Verify, Integer> {
    public Integer countByCode(String code);

    @Modifying
    @Transactional
    public void deleteAllByUserId(Integer userId);

    Verify findVerifyByTypeAndCode(String type, String code);

    Verify findVerifyByCode(String code);
}
