package me.huiya.core.Repository;

import me.huiya.core.Entity.Token;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query(value = "SELECT t FROM Token t WHERE t.refreshToken = :refreshToken AND t.refreshExpire >= current_timestamp ")
    public Token findTokenByRefreshToken(String refreshToken);

    @Cacheable("tokenKey")
    public Token getTokenByToken(String token);

    @Modifying
    @Transactional
    @Query(value="DELETE FROM Token t WHERE t.token = :token")
    @CacheEvict("tokenKey")
    public void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query(value="DELETE FROM Token t WHERE t.token = :token AND t.refreshToken = :refreshToken")
    @CacheEvict("tokenKey")
    public void deleteByTokenAndRefreshToken(String token, String refreshToken);

    public Token findFirstByUserIdAndBrowserOrderByRefreshExpireDesc(Integer userId, String browser);
}
