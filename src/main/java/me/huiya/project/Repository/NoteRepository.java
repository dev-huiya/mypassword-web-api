package me.huiya.project.Repository;

import me.huiya.project.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    public Note findAllById(Integer id);
}