## 任务 A1：创建 Maven 父工程与 library-api 模块骨架

**Files:**
- Create: `pom.xml` (父工程)
- Create: `library-api/pom.xml`
- Create: `library-api/src/main/java/com/library/LibraryApplication.java`
- Create: `library-api/src/main/resources/application.yml`
- Create: `library-api/src/main/resources/application-dev.yml`

- [ ] **Step 1: 创建父 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.library</groupId>
  <artifactId>library-system</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <modules><module>library-api</module></modules>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
    <relativePath/>
  </parent>
  <properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <jjwt.version>0.12.5</jjwt.version>
    <bucket4j.version>8.10.1</bucket4j.version>
    <archunit.version>1.3.0</archunit.version>
    <testcontainers.version>1.19.7</testcontainers.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
  </properties>
</project>
```

- [ ] **Step 2: 创建 library-api/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.library</groupId>
    <artifactId>library-system</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </parent>
  <artifactId>library-api</artifactId>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
    <dependency><groupId>com.github.ben-manes.caffeine</groupId><artifactId>caffeine</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>com.bucket4j</groupId><artifactId>bucket4j-core</artifactId><version>${bucket4j.version}</version></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId><version>${mapstruct.version}</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><version>${testcontainers.version}</version><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>mysql</artifactId><version>${testcontainers.version}</version><scope>test</scope></dependency>
    <dependency><groupId>com.tngtech.archunit</groupId><artifactId>archunit-junit5</artifactId><version>${archunit.version}</version><scope>test</scope></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
  </dependencies>
</project>
```

- [ ] **Step 3: 创建 LibraryApplication.java**

```java
package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml（含 fail-fast 校验）**

```yaml
spring:
  profiles:
    active: dev
  application:
    name: library-api
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/library?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USER:library}
    password: ${DB_PASSWORD:library}
  jpa:
    hibernate.ddl-auto: validate
    open-in-view: false
    properties.hibernate.jdbc.time_zone: Asia/Shanghai
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${SERVER_PORT:8080}

library:
  jwt:
    secret: ${LIBRARY_JWT_SECRET:}
    access-ttl: PT15M
    refresh-ttl: P7D
    issuer: library-system
  rate-limit:
    enabled: true
  token-blacklist:
    type: caffeine

logging:
  pattern.level: '%5p [%X{traceId:-},%X{userId:-}]'
```

- [ ] **Step 5: 创建 application-dev.yml**

```yaml
library:
  jwt:
    secret: dev-secret-must-be-at-least-32-bytes-long-please
spring:
  jpa:
    show-sql: true
```

- [ ] **Step 6: 验证骨架可启动**

Run: `cd library-api && mvn -q -DskipTests spring-boot:run`
Expected: 打印 Spring Boot Banner 后，因为 JWT secret 校验、DB 不可用而 fail（这一步还没做校验，先确认能编译）。Ctrl+C 终止。

- [ ] **Step 7: Commit**

```bash
git add pom.xml library-api/pom.xml library-api/src/main/java/com/library/LibraryApplication.java library-api/src/main/resources/
git commit -m "feat(api): scaffold maven multi-module + spring boot application"
```

---

## 任务 A2：DDD 包目录与 ArchUnit 占位测试

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/.gitkeep`
- Create: `library-api/src/main/java/com/library/application/.gitkeep`
- Create: `library-api/src/main/java/com/library/domain/.gitkeep`
- Create: `library-api/src/main/java/com/library/infrastructure/.gitkeep`
- Create: `library-api/src/test/java/com/library/ArchitectureTest.java`

- [ ] **Step 1: 创建 ArchitectureTest（占位，4 条规则）**

```java
package com.library;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "com.library", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule LAYERS = Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Interfaces").definedBy("com.library.interfaces..")
            .layer("Application").definedBy("com.library.application..")
            .layer("Domain").definedBy("com.library.domain..")
            .layer("Infrastructure").definedBy("com.library.infrastructure..")
            .whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Interfaces");
}
```

- [ ] **Step 2: Run test**

`mvn -pl library-api test -Dtest=ArchitectureTest`
Expected: PASS（空工程，分层规则平凡成立）

- [ ] **Step 3: Commit**

```bash
git add library-api/src/test/java/com/library/ArchitectureTest.java library-api/src/main/java/com/library/
git commit -m "test(arch): add ArchUnit layered architecture rule"
```
