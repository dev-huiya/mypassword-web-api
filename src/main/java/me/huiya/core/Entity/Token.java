package me.huiya.core.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
// 빈 변수는 저장하거나 업데이트 하지 않도록 설정
@DynamicInsert
@DynamicUpdate
@Table(name = "token")
@ToString(exclude = {"privateKey", "publicKey"})
@Entity
@Data
public class Token {

    @Id
    @Column(length = 1024)
    private String token;

    @Column(nullable = false)
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer userId;

    @Column(length = 84) // IPv6 45자 암호화시 84
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    @Convert(converter = AESCryptConverter.class) // 암호화
    private String ipAddress;

    @Column(length = 255, nullable = false)
    @Convert(converter = AESCryptConverter.class) // 암호화
    @NotNull
    private String browser;

    @Column(length = 3072, nullable = false)
    @Convert(converter = AESCryptConverter.class) // 암호화
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String privateKey;

    @Column(length = 2048, nullable = false)
    @Convert(converter = AESCryptConverter.class) // 암호화
    @NotNull
    private String publicKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = true, updatable = false)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Date expire;

    @Column(length = 64, nullable = false)
    @Convert(converter = AESCryptConverter.class) // 암호화
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = true, updatable = false)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Date refreshExpire;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Seoul")
    private Date createTime;
}
