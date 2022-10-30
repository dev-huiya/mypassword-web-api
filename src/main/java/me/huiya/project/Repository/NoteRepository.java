package me.huiya.project.Repository;

import me.huiya.project.Entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    public Note findAllByIdAndUserId(Integer id, Integer userId);

    public Page<Note> findNotesByUserId(Integer userId, Pageable pageable);
}