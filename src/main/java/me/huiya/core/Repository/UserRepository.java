package me.huiya.core.Repository;

import me.huiya.core.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value="SELECT salt FROM User WHERE email = :email")
    public String findSaltByEmail(String email);

    public Integer countByEmail(String email);

    public Integer countByNickName(String nickname);

    public User findUserByEmailAndPassword(String email, String saltedPassword);

    public User findUserByUserId(Integer id);

    public User findUserByEmailAndEmailVerifyFalse(String email);

    public User findUserByEmail(String email);
}
