package me.huiya.project.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
// 빈 변수는 저장하거나 업데이트 하지 않도록 설정
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "passwords")
@Data
@NoArgsConstructor
public class Password {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
//    @JsonIgnore
    private Integer id;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Controller에서 값 자동 리턴시 Json에 포함되지 않도록 예외처리
    private Integer userId;

    @Column
    private String url; // full url ex) http://www.example.com/login?redirect=/about

    @Column
    private String protocol; // http
    @Column
    private String host; // www.example.com
    @Column
    private Integer port; // 80
    @Column
    private String path; // /login
    @Column
    private String query; // redirect=/about

    @Column(length = 1024, nullable = true)
    private String username;

    @Column(length = 1024, nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    public Password(Integer id, Integer userId,
        String url, String protocol, String host, Integer port, String path, String query,
        String username
    ) {
        setId(id);
        setUserId(userId);
        setUrl(url);
        setProtocol(protocol);
        setHost(host);
        setPort(port);
        setPath(path);
        setQuery(query);
        setUsername(username);
    }
}
