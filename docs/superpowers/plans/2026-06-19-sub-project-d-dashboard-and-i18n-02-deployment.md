# Sub-project D 实现计划：Dashboard、i18n 与部署 (2/2) - 部署与容器化

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为整个系统提供 Docker 容器化支持，并编写 `docker-compose.yml` 以便一键启动。

---

## 任务 D4：后端 Dockerfile

**Files:**
- Create: `library-api/Dockerfile`

- [ ] **Step 1: 编写后端 Dockerfile**

```dockerfile
# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
# Copy parent pom and api pom
COPY pom.xml .
COPY library-api/pom.xml library-api/
# Download dependencies
RUN mvn -B -f library-api/pom.xml dependency:go-offline
# Copy source code
COPY library-api/src library-api/src
# Build
RUN mvn -B -f library-api/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/library-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: Commit**
```bash
git add library-api/Dockerfile
git commit -m "chore(api): add dockerfile for backend"
```

## 任务 D5：前端 Dockerfile 与 Nginx 配置

**Files:**
- Create: `library-web/Dockerfile`
- Create: `library-web/nginx.conf`

- [ ] **Step 1: 编写 Nginx 配置**

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://library-api:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

- [ ] **Step 2: 编写前端 Dockerfile**

```dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

- [ ] **Step 3: Commit**
```bash
git add library-web/Dockerfile library-web/nginx.conf
git commit -m "chore(web): add dockerfile and nginx config for frontend"
```

## 任务 D6：Docker Compose 编排

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: 编写 docker-compose.yml**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: library_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  library-api:
    build:
      context: .
      dockerfile: library-api/Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/library_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      mysql:
        condition: service_healthy

  library-web:
    build:
      context: ./library-web
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - library-api

volumes:
  mysql_data:
```

- [ ] **Step 2: Commit**
```bash
git add docker-compose.yml
git commit -m "chore: add docker-compose for the whole system"
```

---

## 验证与交接

1. **后端验证**：`mvn verify` 确保测试通过。
2. **前端验证**：`npm run build` 确保 TypeScript 编译通过。
3. **部署测试**：运行 `docker-compose up -d --build`，访问 `http://localhost` 检查系统是否正常运行。

**Plan complete and saved to `docs/superpowers/plans/2026-06-19-sub-project-d-dashboard-and-i18n-*.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration
**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
