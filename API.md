# API Doc

<details markdown="1" style="margin-left:14px">
<summary>유저</summary>

<br />

<details markdown="1" style="margin-left:14px">
<summary>POST /account/signup</summary>

**회원가입**
----
신규 계정을 생성합니다.

* **URL**

  /account/signup

* **Method:**

  `POST` multipart/form-data

* **Request**

  **Required:**  
  `email=[String] - 사용자 아이디`  
  `password=[String] - 사용자 비밀번호`  
  `nickName=[String] - 닉네임`

```
POST /account/signup HTTP/1.1
Host: localhost:8080
Content-Length: 322
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="email"

test@test.com
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="password"

1234
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="nickName"

테스트 계정
----WebKitFormBoundary7MA4YWxkTrZu0gW
```

* **Response**

  **Optional:**   
  `profile=[File] - 프로필이미지`

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "join": true
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>POST /auth/signin</summary>

**로그인**
----
기존 계정으로 로그인합니다.

* **URL**

  /account/signup

* **Method:**

  `POST`

* **Request**

  **Required:**  
  `email=[String] - 사용자 아이디`  
  `password=[String] - 사용자 비밀번호`  
  `autoLogin=[Boolean] - 자동 로그인 여`

  **Optional:**

```
POST /auth/signin HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Content-Length: 83

{
    "email": "test@test.com",
    "password": "1234",
    "autoLogin": true
}
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "token": "...(생략)...",
        "browser": "Other null / Other null",
        "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...(생략)...QAB\n-----END PUBLIC KEY-----\n",
        "createTime": "2022-10-31 06:49:30.593"
    }
}
```
</details>

</details>

<details markdown="1" style="margin-left:14px">
<summary>비밀번호</summary>

<br />

<details markdown="1" style="margin-left:14px">
<summary>GET /password/all</summary>

**비밀번호 목록**
----
저장된 비밀번호 목록을 host로 그룹지어 불러옵니다.

* **URL**

  /password/all

* **Method:**

  `GET`

* **Request**
  **Required:**

  `Authorization=[Header,String] - 엑세스 토큰`

  **Optional:**

```
GET /password/all HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "content": [
            {
                "id": 1,
                "url": "https://nid.naver.com/nidlogin.login?mode=form&url=https%3A%2F%2Fwww.naver.com",
                "protocol": "https",
                "host": "nid.naver.com",
                "port": null,
                "path": "/nidlogin.login",
                "query": "mode=form&url=https%3A%2F%2Fwww.naver.com",
                "username": "+qwHp6EwTBhYo2EZi8JNNaD0XO1RFDOcXLvLWzQ6Ip8=", // 암호화
                "count": 1
            }
        ],
        "pageable": {
            "sort": {
                "empty": false,
                "sorted": true,
                "unsorted": false
            },
            "offset": 0,
            "pageNumber": 0,
            "pageSize": 10,
            "unpaged": false,
            "paged": true
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 1,
        "size": 10,
        "number": 0,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "first": true,
        "numberOfElements": 1,
        "empty": false
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>GET /password/search</summary>

**비밀번호 목록**
----
저장된 비밀번호 목록을 host로 그룹지어 검색합니다.

* **URL**

  /password/search

* **Method:**

  `GET`

* **Request**
  **Required:**
  `Authorization=[Header,String] - 엑세스 토큰`
  `value=[String] - 검색어`

  **Optional:**

```
GET /password/search?value=com HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...

```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "content": [
            {
                "id": 1,
                "url": "https://nid.naver.com/nidlogin.login?mode=form&url=https%3A%2F%2Fwww.naver.com",
                "protocol": "https",
                "host": "nid.naver.com",
                "port": null,
                "path": "/nidlogin.login",
                "query": "mode=form&url=https%3A%2F%2Fwww.naver.com",
                "username": "+qwHp6EwTBhYo2EZi8JNNaD0XO1RFDOcXLvLWzQ6Ip8=", // 암호화
                "count": 1
            }
        ],
        "pageable": {
            "sort": {
                "empty": false,
                "sorted": true,
                "unsorted": false
            },
            "offset": 0,
            "pageNumber": 0,
            "pageSize": 10,
            "unpaged": false,
            "paged": true
        },
        "last": true,
        "totalPages": 1,
        "totalElements": 1,
        "size": 10,
        "number": 0,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "first": true,
        "numberOfElements": 1,
        "empty": false
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>GET /password/host</summary>

**비밀번호 목록**
----
저장된 비밀번호 목록을 host로 검색합니다.

* **URL**

  /password/host

* **Method:**

  `GET`

* **Request**
  **Required:**
  `Authorization=[Header,String] - 엑세스 토큰`
  `value=[String] - 검색어`

  **Optional:**

```
GET /password/host?value=nid.naver.com HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...

```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
   "success":true,
   "message":"OK",
   "resultData":[
      {
         "id":4,
         "url":"https://nid.naver.com",
         "protocol":"https",
         "host":"nid.naver.com",
         "port":null,
         "path":"",
         "query":null,
         "username":"2oYZbkpkkUPfkoa1D2yp0Q==", // 암호화
         "password":"M+KUrAVPoC0P7ML0IYjrMQ==", // 암호화
         "createTime":null,
         "updateTime":null,
         "count":null
      }
   ]
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>POST /password</summary>

**비밀번호 추가**
----
새로운 비밀번호를 저장합니다.

* **URL**

  /password

* **Method:**

  `POST`

* **Request**

  **Required:**  
  `Authorization=[Header,String] - 엑세스 토큰`
  `url=[String] - 비밀번호를 사용할 사이트 주소`  
  `username=[Boolean] - 사용자 아이디`
  `password=[String] - 사용자 비밀번호`

  **Optional:**
```
POST /password HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...
Content-Type: application/json
Content-Length: 207

{
    "url": "https://nid.naver.com/nidlogin.login?mode=form&url=https%3A%2F%2Fwww.naver.com",
    "username": "/FRCVpNGfAbfjJ9N5I29diosDjpzHDtkIl/U3MUl9HI=", // 주어진 sharedKey로 암호화
    "password": "iF6Eco2Opds9/iLflQGxMw==" // 주어진 sharedKey로 암호화
}
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "token": "...(생략)...",
        "browser": "Other null / Other null",
        "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...(생략)...QAB\n-----END PUBLIC KEY-----\n",
        "createTime": "2022-10-31 06:49:30.593"
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>GET /password/{id}</summary>

**비밀번호 상세보기**
----
지정된 비밀번호를 상세하게 불러옵니다

* **URL**

  /password/{id}

* **Method:**

  `GET`

* **Request**

  **Required:**  
  `Authorization=[Header,String] - 엑세스 토큰`
  `id=[Integer:PathVariable] - 비밀번호 번호`

  **Optional:**

```
GET /password/1 HTTP/1.1
Host: localhost:8080
Authorization:  Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "id": 1,
        "url": "https://nid.naver.com/nidlogin.login?mode=form&url=https%3A%2F%2Fwww.naver.com",
        "protocol": "https",
        "host": "nid.naver.com",
        "port": null,
        "path": "/nidlogin.login",
        "query": "mode=form&url=https%3A%2F%2Fwww.naver.com",
        "username": "OO0tPlhIsn1sVU/9FNvTCQ==", // 암호화
        "password": "7CeTu+5DHb4SBJqjv0FaIw==" // 암호화
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>PUT /password/{id}</summary>

**비밀번호 수정**
----
지정된 비밀번호의 정보를 수정합니다.

* **URL**

  /password/{id}

* **Method:**

  `PUT`

* **Request**

  **Required:**  
  `Authorization=[Header,String] - 엑세스 토큰`
  `id=[Integer:PathVariable] - 비밀번호 번호`

  **Optional:**
  `url=[String] - 로그인 페이지 주소`
  `username=[String] - 유저 아이디`
  `password=[String] - 유저 비밀번호`

```
PUT /password/1 HTTP/1.1
Host: localhost:8080
Authorization:  Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...
Content-Type: application/json
Content-Length: 93

{
    "username": "OO0tPlhIsn1sVU/9FNvTCQ==",
    "password": "7CeTu+5DHb4SBJqjv0FaIw=="
}
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "update": true
    }
}
```
</details>

<details markdown="1" style="margin-left:14px">
<summary>DELETE /password/{id}</summary>

**비밀번호 삭제**
----
지정된 비밀번호를 삭제합니다.

* **URL**

  /password/{id}

* **Method:**

  `DELETE`

* **Request**

  **Required:**  
  `Authorization=[Header,String] - 엑세스 토큰`
  `id=[Integer:PathVariable] - 비밀번호 번호`

  **Optional:**

```
DELETE /password/1 HTTP/1.1
Host: localhost:8080
Authorization:  Bearer eyJ0eX...(로그인시 받은 엑세스 토큰)...
```

* **Response**

* **Success Response:**
```
HTTP/1.1 200 OK
Content-type: application/json;charset=UTF-8
{
    "success": true,
    "message": "OK",
    "resultData": {
        "delete": true
    }
}
```
</details>

</details>

