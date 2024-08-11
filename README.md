## Getting Started

### application.properties 설정

```properties
# JWT 토큰 비밀키
spring.jwt.secret=vmfhaltmskdlstkfkdgodyroqkfwkdbalroqkfwkdbalaaaaaaaaaaaaaaaabbbbb

# MySQL 데이터베이스 설정
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/{DB명}?useSSL=false&useUnicode=true&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
spring.datasource.username={유저네임}
spring.datasource.password={비밀번호}

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

#S3 설정
cloud.aws.s3.bucket={버킷이름}
cloud.aws.credentials.accessKey={발급받은 엑세스키}
cloud.aws.credentials.secretKey={발급받은 시크릿키}}
```
