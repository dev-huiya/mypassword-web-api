package me.huiya.core.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.huiya.core.Entity.AESCryptConverter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

//@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter
@Setter
// 빈 변수는 저장하거나 업데이트 하지 않도록 설정
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    private Integer userId;

    @Column(length = 255, nullable = false)
    @Convert(converter = AESCryptConverter.class) // 암호화
    @Email
    @NotNull
    private String email;

    @Column(length = 65, nullable = false)
    @JsonIgnore // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    @NotNull
    private String password;

    @Column(length = 65, nullable = false)
    @JsonIgnore // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    private String salt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    private Date createTime;

    @Column(length = 255, nullable = true)
    @Convert(converter = AESCryptConverter.class) // 암호화
    private String nickName;

    @Column(length = 110, nullable = true)
    @Convert(converter = AESCryptConverter.class) // 암호화
    private String profileImage;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0", length = 1)
    private boolean emailVerify;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    private String masterKey;
}
