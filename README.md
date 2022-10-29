<div align="center">

  <h1>Spring Boot core for SPA partial CSR</h1>
  
  <p>
    Bolierplate Template for SpringBoot + JPA + Hibernate
  </p>

  <img alt="GitHub tag (latest by date)" src="https://img.shields.io/github/v/tag/dev-huiya/spring-boot-core?label=version">
  <img src="https://img.shields.io/github/stars/dev-huiya/spring-boot-core" alt="stars" />
  <img src="https://img.shields.io/github/issues/dev-huiya/spring-boot-core" alt="open issues" />
  <img src="https://img.shields.io/github/license/dev-huiya/spring-boot-core" alt="license" />

  <br />
</div>


## :gear: Required
Java >= 17

## :scroll: Documentation
추가 예정

## :key: Environment Variables
이 프로젝트를 작동하기 위해서는 `src/main/resources/application.properties` 경로에 아래 변수 설정이 :exclamation:필수적으로 꼭:exclamation: 필요합니다.

이 외에도 다양한 변수가 있습니다. 주석과 변수명을 참조하여 필요한 변수를 입력해주세요

- `core.SERVER.UI` http/https를 포함한 SPA UI 서버 URL
- `core.SERVER.API` http/https를 포함한 API 서버 URL
- `core.AES-key` DB 암호화에 사용되는 AES key
- `core.AES-iv` DB 암호화에 사용되는 AES iv
- `spring.datasource.(...etc)` DB 연결 정보

## :compass: Feature
- [x] ~~JWT Manager 개발~~
- [x] ~~Attachment File Manager 개발~~
- [ ] 다국어 기능 개발
    - [ ] DB에서 메세지 값 관리할것
    - [ ] HTTP Accept-Language 값을 기본 값으로 사용
    - [ ] 쿠키 값으로 언어값 수동 설정 기능
    - [ ] 이메일, 에러 메세지 등 서버단 메세지 다국어 처리
    - [ ] SPA용 클라이언트단 다국어 API 개발
- [ ] Gmail API 연동되는 Email Service 개발
- [ ] SPA 연동 기능
    - [ ] API Controller 404 예외처리
    - [ ] SPA를 위해 API 경로를 제외하고는 모두 /index로 연결
    - [ ] SPA SEO용 meta 데이터 입력 기능 
