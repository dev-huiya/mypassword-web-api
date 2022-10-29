package me.huiya.core.Repository;

import me.huiya.core.Entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {
    public File findAllById(Integer id);

    public File findByHash(String hash);
}