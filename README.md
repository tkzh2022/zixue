# 图书馆管理系统 (Library Management System)

基于领域驱动设计 (DDD) 四层架构的全栈图书馆管理系统，采用 Spring Boot 3 + Vue 3 技术栈，支持 Docker Compose 一键部署。

---

## 目录

- [主要功能](#主要功能)
- [技术选型](#技术选型)
- [架构设计](#架构设计)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [API 接口清单](#api-接口清单)
- [安全机制](#安全机制)
- [部署方式](#部署方式)
- [本地开发](#本地开发)
- [测试体系](#测试体系)
- [默认账号](#默认账号)

---

## 主要功能

### 管理员端（LIBRARIAN）

| 模块 | 功能说明 |
|------|---------|
| **控制台** | 实时统计看板：图书总数、读者总数、在借数量、未缴罚款金额 |
| **图书管理** | 图书的新增、编辑、删除、搜索（ISBN/书名/作者）；物理复本的入库和删除 |
| **读者管理** | 读者建档、资料编辑（姓名/电话/邮箱）、启用/禁用状态管理 |
| **借阅管理** | 凭读者编号 + 图书条码办理借书/还书；自动计算逾期天数并生成罚款 |
| **罚款管理** | 罚款列表查看、按精确金额线下缴费后系统核销 |
| **借阅规则** | 动态配置每种读者类型的最大借阅天数、可借数量、续借次数、每日罚金 |

### 读者端（READER）

| 模块 | 功能说明 |
|------|---------|
| **控制台** | 个人统计：在借数量、逾期图书、未缴罚款 |
| **图书目录** | 公开图书搜索（书名/作者/ISBN），查看图书详情和可借状态 |
| **我的借阅** | 查看借阅记录、在线续借 |
| **我的罚款** | 查看罚款记录及缴费状态 |

### 通用功能

| 功能 | 说明 |
|------|------|
| **认证与授权** | 注册、登录、JWT 令牌认证、刷新令牌轮换、注销（令牌黑名单） |
| **国际化** | 前端支持中文/英文双语切换（默认跟随浏览器语言） |
| **接口限流** | 基于 IP/用户的 API 访问频率限制 |
| **登录保护** | 连续 5 次密码错误后锁定 15 分钟 |

---

## 技术选型

### 后端（library-api）

| 分类 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.2.5 |
| 编程语言 | Java | 17（编译）/ 21（运行时） |
| 持久化 | Spring Data JPA + Hibernate | — |
| 数据库 | MySQL | 8.0 |
| 数据库迁移 | Flyway | — |
| 安全认证 | Spring Security + JWT (JJWT) | HS384 签名 |
| 缓存 | Caffeine | — |
| 接口限流 | Bucket4j | 8.10.1 |
| 对象映射 | MapStruct | 1.5.5 |
| 架构守护 | ArchUnit | 1.3.0 |
| 集成测试 | Testcontainers (MySQL) | 1.20.6 |

### 前端（library-web）

| 分类 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Vue 3 (Composition API) | 3.4.21 |
| 构建工具 | Vite | 5.2.8 |
| 类型系统 | TypeScript | 5.4 |
| 状态管理 | Pinia | 2.1.7 |
| 路由 | Vue Router | 4.3 |
| UI 组件库 | Element Plus | 2.7.2 |
| 国际化 | Vue I18n | 9.10.2 |
| HTTP 客户端 | Axios | 1.6.8 |
| 单元测试 | Vitest | 1.4 |
| E2E 测试 | Playwright | 1.61 |

### 部署

| 分类 | 技术 |
|------|------|
| 容器化 | Docker + Docker Compose |
| Web 服务器 | Nginx（前端静态文件 + API 反向代理） |
| 数据持久化 | Docker Volume (mysql_data) |

---

## 架构设计

### DDD 四层架构

后端代码严格遵循 DDD 分层，并通过 ArchUnit 测试强制执行层间依赖规则：

```
┌─────────────────────────────────────────────────┐
│  Interfaces 接口层                               │
│  REST Controller, DTO, Filter, ExceptionHandler │
├─────────────────────────────────────────────────┤
│  Application 应用层                              │
│  ApplicationService, Command, View              │
├─────────────────────────────────────────────────┤
│  Domain 领域层 (纯 Java，不依赖 Spring/JPA)      │
│  Entity, ValueObject, Repository(接口), Enum    │
├─────────────────────────────────────────────────┤
│  Infrastructure 基础设施层                       │
│  JPA 实现, Security, RateLimit, Cache           │
└─────────────────────────────────────────────────┘
```

**依赖方向**：Interfaces → Application → Domain ← Infrastructure

**架构守护规则**：
- 领域层不允许引用 Spring Framework 或 Jakarta Persistence
- 接口层不能直接访问基础设施层
- 应用层不能依赖接口层

### 前端架构

```
┌──────────────────────────────────────────┐
│  Views (页面组件)                         │
│  ├── 管理员端: Dashboard, Books, Readers │
│  │   Borrows, Fines, Rules               │
│  └── 读者端: Catalog, MyBorrows, MyFines │
├──────────────────────────────────────────┤
│  Stores (Pinia 状态管理)                  │
│  ├── useUserStore (用户/认证)             │
│  └── useAppStore (应用设置)               │
├──────────────────────────────────────────┤
│  API Layer (Axios 请求层)                 │
│  ├── request.ts (统一拦截/解包/刷新令牌)  │
│  └── auth/books/borrows/... .ts          │
├──────────────────────────────────────────┤
│  Router + Guards (路由 + 导航守卫)        │
│  └── 角色鉴权 + Token 校验               │
└──────────────────────────────────────────┘
```

### 前后端通信

所有 API 响应统一使用 `Result<T>` 信封格式：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "traceId": "abc123"
}
```

前端 Axios 拦截器自动解包 `data` 字段，业务代码直接使用内层数据。

### 认证流程

```
登录: POST /auth/login → { accessToken (15min), refreshToken (7天) }
     ↓
请求: Authorization: Bearer <accessToken>
     ↓
过期: 拦截器自动调用 POST /auth/refresh 轮换令牌
     ↓ (并发请求排队等待刷新完成)
注销: POST /auth/logout → 黑名单 accessToken + 吊销 refreshToken
```

---

## 项目结构

```
my-library-system/
├── pom.xml                          # Maven 父工程（多模块管理）
├── docker-compose.yml               # 容器编排（MySQL + API + Nginx）
├── README.md                        # 项目文档
├── scripts/
│   └── dev-env.sh                   # 开发环境设置脚本
│
├── library-api/                     # 后端 Spring Boot 模块
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/library/
│       │   ├── LibraryApplication.java
│       │   ├── config/              # 应用配置（DataInitializer, WebConfig）
│       │   ├── application/         # 应用层（Service, Command, View）
│       │   │   ├── auth/
│       │   │   ├── book/
│       │   │   ├── borrow/
│       │   │   ├── dashboard/
│       │   │   ├── fine/
│       │   │   └── reader/
│       │   ├── domain/              # 领域层（实体, 仓储接口, 枚举）
│       │   │   ├── book/            # Book, BookCopy
│       │   │   ├── borrow/          # BorrowRecord, BorrowRule
│       │   │   ├── fine/            # FineRecord
│       │   │   ├── reader/          # Reader
│       │   │   ├── user/            # UserAccount, RefreshToken
│       │   │   └── shared/          # Result<T>, BusinessException, ResultCode
│       │   ├── infrastructure/      # 基础设施层
│       │   │   ├── persistence/jpa/ # JPA 实体 + Repository 实现 + Mapper
│       │   │   ├── security/        # JWT, 登录保护, 令牌黑名单
│       │   │   ├── ratelimit/       # Bucket4j 限流
│       │   │   └── alert/           # 告警发送
│       │   └── interfaces/          # 接口层
│       │       ├── rest/            # 11 个 REST Controller
│       │       ├── dto/             # 请求/响应 DTO
│       │       ├── filter/          # JWT 过滤器, TraceId 过滤器
│       │       ├── security/        # @RequireLibrarian, @RequireReader
│       │       ├── annotation/      # @RateLimit
│       │       └── advice/          # 全局异常处理, TraceId 响应
│       └── main/resources/
│           ├── application.yml      # 主配置
│           ├── application-dev.yml  # 开发环境配置
│           └── db/migration/        # Flyway 迁移脚本（V1~V5）
│
├── library-business/                # 实验性业务模块（未接入运行时）
│   └── src/.../BorrowServiceImpl.java
│
└── library-web/                     # 前端 Vue 3 项目
    ├── Dockerfile
    ├── nginx.conf                   # Nginx 配置（SPA + API 代理）
    ├── package.json
    ├── vite.config.ts
    ├── vitest.config.ts
    ├── playwright.config.js
    └── src/
        ├── App.vue
        ├── main.ts
        ├── api/                     # API 请求模块（8 个文件）
        ├── components/global/       # 全局组件（LangSwitch, ErrorBoundary）
        ├── i18n/                    # 国际化（zh/en）
        ├── layouts/                 # 布局（Admin, Reader, Blank）
        ├── router/                  # 路由 + 导航守卫
        ├── stores/                  # Pinia 状态管理
        ├── utils/                   # 工具函数（auth, trace）
        └── views/                   # 页面组件（16 个文件）
            ├── auth/                # 登录, 注册
            ├── dashboard/           # 控制台
            ├── books/               # 图书管理, 复本管理
            ├── readers/             # 读者管理
            ├── borrows/             # 借阅管理
            ├── fines/               # 罚款管理
            ├── settings/            # 借阅规则
            ├── catalog/             # 图书目录（公开）
            ├── my/                  # 我的借阅, 我的罚款
            └── error/               # 403, 404
```

---

## 数据库设计

共 12 张表，通过 5 个 Flyway 迁移脚本创建：

### ER 关系概览

```
user_account ──1:1── reader ──1:N── borrow_record ──1:1── fine_record
                                        │
                                    N:1─┘
book ──1:N── book_copy ─────────────────┘
  │
  ├── M:N ── author     (通过 book_author)
  └── M:N ── category   (通过 book_category)

borrow_rule （独立配置表）
refresh_token ──N:1── user_account
```

### 核心表结构

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user_account` | 系统账户 | username(UK), password_hash, role, status |
| `refresh_token` | 刷新令牌 | token_hash(UK), expires_at, revoked_at |
| `book` | 图书元数据 | isbn(UK), title, total_copies, available_copies |
| `book_copy` | 物理复本 | barcode(UK), book_id(FK), status |
| `author` | 作者 | name(UK) |
| `category` | 分类 | code(UK), name |
| `reader` | 读者档案 | reader_no(UK), user_account_id(UK), status |
| `borrow_record` | 借阅记录 | reader_id, book_copy_id, borrow_time, due_date, status |
| `borrow_rule` | 借阅规则 | reader_type(UK), max_borrow_days/count, fine_per_day |
| `fine_record` | 罚款记录 | borrow_record_id(UK), amount, status |

### 默认借阅规则

| 读者类型 | 最大天数 | 最大数量 | 续借次数 | 每日罚金 |
|---------|---------|---------|---------|---------|
| DEFAULT | 30 天 | 5 本 | 1 次 | ¥0.50 |

---

## API 接口清单

基础路径：`/api/v1`，共 11 个控制器、约 35 个端点。

### 认证模块（公开）

| 方法 | 路径 | 说明 | 限流 |
|------|------|------|------|
| POST | `/auth/login` | 登录 | 10次/分 |
| POST | `/auth/register` | 注册（自动创建读者档案） | 5次/分 |
| POST | `/auth/refresh` | 刷新令牌（轮换旧令牌） | 30次/分 |
| POST | `/auth/logout` | 注销（需认证） | — |
| GET | `/auth/me` | 获取当前用户信息（需认证） | — |

### 图书目录（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/catalog/books` | 搜索图书（keyword, category, 分页） |
| GET | `/catalog/books/{id}` | 图书详情 |

### 图书管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/books` | 图书列表（搜索 + 分页） |
| POST | `/books` | 新增图书 |
| GET | `/books/{id}` | 图书详情 |
| PUT | `/books/{id}` | 编辑图书 |
| DELETE | `/books/{id}` | 删除图书 |
| POST | `/books/{id}/copies` | 添加复本 |
| GET | `/books/{id}/copies` | 复本列表 |
| DELETE | `/books/copies/{copyId}` | 删除复本 |

### 读者管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/readers` | 读者列表（搜索 + 分页） |
| POST | `/readers` | 新增读者 |
| GET | `/readers/{id}` | 读者详情 |
| PUT | `/readers/{id}` | 编辑读者资料 |
| PUT | `/readers/{id}/status` | 启用/禁用读者 |

### 借阅管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/borrows` | 借阅记录列表 |
| POST | `/borrows` | 办理借书（readerNo + barcode） |
| PUT | `/borrows/return/{barcode}` | 办理还书 |

### 罚款管理（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/fines` | 罚款列表 |
| PUT | `/fines/{id}/pay` | 核销罚款 |

### 借阅规则（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/rules` | 规则列表 |
| PUT | `/rules/{id}` | 更新规则 |

### 看板（按角色）

| 方法 | 路径 | 角色 | 返回 |
|------|------|------|------|
| GET | `/dashboard/admin` | 管理员 | totalBooks, totalReaders, activeBorrows, unpaidFines |
| GET | `/dashboard/reader` | 读者 | activeBorrows, overdueBorrows, unpaidFines |

### 读者个人（读者）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/my/borrows` | 我的借阅记录 |
| PUT | `/my/borrows/{id}/renew` | 续借 |
| GET | `/my/fines` | 我的罚款 |

---

## 安全机制

### 认证架构

| 组件 | 说明 |
|------|------|
| `JwtAuthFilter` | 请求拦截器，解析 Bearer Token，校验黑名单，注入认证信息和 `userId` |
| `JwtService` | JWT 签发（HS384）、解析、验证 |
| `CaffeineTokenBlacklist` | 注销后的 Access Token 黑名单（内存，按过期时间自动清理） |
| `RefreshToken` (DB) | 刷新令牌持久化，支持轮换和吊销 |
| `LoginAttemptService` | 登录失败计数，5 次锁定 15 分钟 |
| `@RequireLibrarian` / `@RequireReader` | 方法级角色鉴权注解 |

### 令牌生命周期

| 令牌类型 | 有效期 | 存储 |
|---------|--------|------|
| Access Token | 15 分钟 | 前端 localStorage |
| Refresh Token | 7 天 | 前端 localStorage + 后端数据库 |

### 接口限流

通过 `@RateLimit` 注解配置，支持 `ip`、`user`、`ip+user` 三种限流维度，使用 Bucket4j 令牌桶算法。

### 前端安全

- **路由守卫**：`meta.requiresAuth` + `meta.roles` 控制页面访问权限
- **令牌自动刷新**：401 响应触发刷新令牌，并发请求排队等待
- **请求追踪**：每个请求自动附带 `X-Trace-Id`

---

## 部署方式

### Docker Compose（推荐）

```yaml
services:
  mysql:        # MySQL 8.0，端口 3306，数据卷 mysql_data
  library-api:  # Spring Boot API，端口 8080，依赖 MySQL 健康检查
  library-web:  # Nginx 静态文件 + API 反向代理，端口 80
```

#### 启动步骤

```bash
# 1. 构建并启动所有服务
docker compose up --build -d

# 2. 查看服务状态
docker compose ps

# 3. 访问应用
#    前端: http://localhost
#    API:  http://localhost:8080/api/v1/
```

#### Nginx 配置要点

- SPA 路由：所有非文件请求回退到 `index.html`
- API 代理：`/api/` → `http://library-api:8080/api/`
- 静态资源缓存：JS/CSS/图片 30 天，`index.html` 不缓存
- 请求头转发：`X-Real-IP`、`X-Forwarded-For`

### 手动部署

#### 后端

```bash
cd library-api
mvn clean package -DskipTests
java -jar target/library-api-*-exec.jar \
  --spring.profiles.active=dev \
  --spring.datasource.url=jdbc:mysql://localhost:3306/library_db
```

#### 前端

```bash
cd library-web
npm ci && npm run build
# 将 dist/ 目录部署到 Nginx 或其他 Web 服务器
```

---

## 本地开发

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 21 |
| Maven | 3.8+ |
| Node.js | 20+ |
| MySQL | 8.0 |
| Docker | 可选（用于集成测试和部署） |

### 后端开发

```bash
# 设置 JAVA_HOME（如有需要）
source scripts/dev-env.sh

# 启动后端（dev 配置使用本地 MySQL）
cd library-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端 `dev` 环境配置：
- 数据库：`jdbc:mysql://localhost:3306/library_db`（用户 `root` / 密码 `root`）
- 显示 SQL：开启
- 日志级别：`com.library: DEBUG`
- JWT 密钥：开发专用密钥

### 前端开发

```bash
cd library-web
npm install
npm run dev
```

Vite 开发服务器运行在 `http://localhost:5173`，自动将 `/api` 代理到 `http://localhost:8080`。

---

## 测试体系

### 后端测试

| 类型 | 数量 | 框架 | 说明 |
|------|------|------|------|
| 单元测试 | 14 个文件 | JUnit 5 + Mockito | 覆盖认证/借阅/罚款/安全组件 |
| 集成测试 | 4 个文件 | Testcontainers + MySQL | 数据库映射/并发借阅/API 安全 |
| 架构测试 | 1 个文件 | ArchUnit | DDD 层间依赖守护 |

运行方式：

```bash
cd library-api
mvn test                    # 运行所有测试（IT 需 Docker）
mvn test -Dtest=*Test       # 仅单元测试
```

### 前端测试

| 类型 | 数量 | 框架 | 说明 |
|------|------|------|------|
| 单元测试 | 3 个文件 | Vitest | 请求拦截器/路由守卫/用户 Store |
| E2E 测试 | 2 个文件 | Playwright | 登录流程 + 全栈借还书链路 |

运行方式：

```bash
cd library-web
npm run test:unit                        # Vitest 单元测试
npx playwright install chromium          # 安装浏览器
npm run test:e2e                         # Playwright E2E
E2E_FULLSTACK_URL=http://localhost npm run test:e2e  # 全栈 E2E
```

---

## 默认账号

系统启动时通过 `DataInitializer`（`dev` Profile）自动创建：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `librarian` | `librarian123` | 完整的后台管理权限 |
| 读者 | `reader1` | `reader123` | 读者端浏览/借阅/续借 |

也可通过注册页面自行创建读者账号（自动分配 READER 角色并创建读者档案）。

---

## 项目统计

| 指标 | 数量 |
|------|------|
| Maven 模块 | 2 个（+ 父工程） |
| 后端 Java 源文件 | ~135 个 |
| REST 控制器 | 11 个 |
| API 端点 | ~35 个 |
| 领域实体 | 8 个（+ 5 个枚举） |
| 应用服务 | 6 个 |
| 数据库表 | 12 张 |
| Flyway 迁移脚本 | 5 个 |
| 前端 Vue 组件 | 22 个 |
| Pinia Store | 2 个 |
| API 模块 | 8 个 |
| Docker 服务 | 3 个 |
