## Getting Started

### application.properties 설정

```properties
# JWT 토큰 비밀키
spring.jwt.secret={SECRET_KEY}

# MySQL 데이터베이스 설정
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/{DB명}?useSSL=false&useUnicode=true&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
spring.datasource.username={유저네임}
spring.datasource.password={비밀번호}

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.properties.hibernate.default_batch_fetch_size=100

#S3 설정
cloud.aws.region.static=ap-northeast-2
cloud.aws.stack.auto-=false

cloud.aws.s3.bucket={버킷이름}
cloud.aws.credentials.accessKey={발급받은 엑세스키}
cloud.aws.credentials.secretKey={발급받은 시크릿키}}

cloud.aws.s3.defaultUserImageUrl={기본 유저 이미지 URL}
cloud.aws.s3.defaultChatRoomImageUrl={기본 채팅방 이미지 URL}

# GMail Settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username={발송하는 이메일}
spring.mail.password={구글 앱 키}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Youtube api 설정
youtube.apikey={youtube apikey}
```

