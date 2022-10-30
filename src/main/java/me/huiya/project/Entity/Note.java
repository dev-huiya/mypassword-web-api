package me.huiya.project.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.huiya.project.Type.NoteType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
// 빈 변수는 저장하거나 업데이트 하지 않도록 설정
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "notes")
@Data
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @JsonIgnore
    private Integer id;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    private Integer userId;

    // 노트 타입 (CARD, NOTE, ...)
    @Column(name="`type`")
    @Enumerated(EnumType.STRING)
    private NoteType type;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    private Date createTime;

    //    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    private Date updateTime;
}
