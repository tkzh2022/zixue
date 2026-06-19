# 图书馆管理系统 设计文档

- 文档版本：v1.0
- 撰写日期：2026-06-18
- 适用版本：MVP 0.1.0
- 范围：中型学校 / 社区图书馆，~50,000 读者、~500,000 册书

## 1. 项目目标与范围

### 1.1 目标
为中型学校 / 社区图书馆提供一套**可投入轻量生产**的图书借阅管理系统，覆盖：图书目录、读者管理、借/还/续借、规则配置、罚款、控制台统计。

### 1.2 用户角色
| 角色 | 描述 | 权限 |
|------|------|------|
| READER 读者 | 持证读者，可查阅书目、查看自己的借阅记录 | 自助查询 |
| LIBRARIAN 管理员 | 图书馆工作人员，处理借/还、维护书目与读者、配置规则 | 全部业务操作 |

读者注册由读者自助提交后由管理员二次审核（或开关启用自动激活）。

### 1.3 非目标（V1 范围外）
- 多分馆/多机构
- 预约排队
- 自助借还机硬件接入（仅预留扫码 API）
- 图书馆典藏迁移、盘点
- 复杂报表导出（仅控制台基本统计）

### 1.4 规模与性能预期
- 数据量：500,000 册书、50,000 读者、年借还流水 ~1,500,000 条
- 在线管理员并发：≤ 50
- 高频查询：图书检索、读者借阅记录、控制台统计 → 引入 Redis 缓存
- 响应时间 P95：列表查询 < 300ms，借/还事务 < 500ms

## 2. 整体架构

### 2.1 物理拓扑
```
[Vue 3 SPA] --(HTTPS)--> [Spring Boot API] --(JDBC)--> [MySQL 8]
                                |--(RESP)----> [Redis 7]   (缓存 + 限流 + 黑名单可选升级)
```

### 2.2 模块结构（DDD 经典 4 层 + Web）
单 Maven 项目下的包分层（不分多 Maven 模块，但 ArchUnit 强约束分层依赖）：

```
com.library
├── interfaces            # Web 接入层
│   ├── rest              # @RestController
│   ├── advice            # GlobalExceptionHandler
│   ├── filter            # JwtAuthFilter, RateLimitInterceptor, TraceIdFilter
│   └── assembler         # DTO <-> Domain
├── application           # 应用服务层（用例编排，事务边界）
│   ├── service
│   ├── command           # *Command DTO
│   ├── query             # *Query DTO + 读模型
│   └── event             # 应用事件
├── domain                # 领域层（无框架依赖）
│   ├── book              # 聚合：Book, BookCopy
│   ├── reader            # 聚合：Reader
│   ├── borrowing         # 聚合：Borrowing, Fine
│   ├── policy            # 聚合：BorrowPolicy
│   ├── user              # 聚合：UserAccount, RefreshToken
│   ├── shared            # 值对象 Money, ReaderNo, Barcode, Isbn
│   └── repository        # 领域仓储接口
└── infrastructure        # 基础设施
    ├── persistence       # JPA/MyBatis 实现，Repository 实现
    ├── security          # JwtService, BCrypt, TokenBlacklist
    ├── cache             # CacheService（Caffeine + Redis 适配）
    ├── ratelimit         # Bucket4j 实现
    └── alert             # AlertSender
```

### 2.3 依赖方向（ArchUnit 守护）
```
interfaces → application → domain
infrastructure → domain (实现接口)
interfaces → infrastructure（仅安全/拦截器配置）
domain  ⟶ 不依赖任何其它包
```

### 2.4 技术栈
- 后端：Java 17、Spring Boot 3.2.x、Spring Security 6、Spring Data JPA、Hibernate 6、Bucket4j、Caffeine、JWT (jjwt 0.12)、Flyway、MapStruct、Lombok、ArchUnit
- 数据库：MySQL 8 + Flyway
- 缓存：Redis 7（生产）/ Caffeine（开发）
- 前端：Vue 3.4 + TypeScript + Vite 5、Pinia、Vue Router 4、Element Plus 2、vue-i18n 9、ECharts 5、axios
- 测试：JUnit5、Mockito、Testcontainers、REST-Assured、Vitest、Playwright
- 工具：Maven 3.9、Docker Compose、GitHub Actions（可选）

## 3. 数据模型

### 3.1 ER 概览
```
user_account 1—1 reader / librarian       账号与角色拆分
user_account 1—n refresh_token            刷新 token
book         1—n book_copy                 一本书多个复本
book         n—n author      via book_author
book         n—n category    via book_category
book_copy    1—n borrowing
reader       1—n borrowing
borrow_policy             历史可追溯，全局当前生效一条
borrowing    1—n fine                      一次借阅可有多笔罚款记录
operation_log                              管理员关键操作审计
```

### 3.2 表结构（核心字段）

**user_account**
- id BIGINT PK
- username VARCHAR(64) UNIQUE
- password_hash VARCHAR(72)
- role ENUM('READER','LIBRARIAN')
- status ENUM('ACTIVE','DISABLED','LOCKED')
- created_at, updated_at
- 索引：username (unique)

**reader**
- id, user_account_id (1:1)
- reader_no VARCHAR(32) UNIQUE
- name, phone, email
- status ENUM('ACTIVE','DISABLED')
- register_date
- 软删除字段 deleted_at
- 索引：reader_no, name

**librarian**
- id, user_account_id (1:1)
- name, phone

**book**
- id, isbn VARCHAR(20)
- title, publisher, publish_year
- total_copies INT, available_copies INT
- location VARCHAR(64)
- summary TEXT
- version INT (乐观锁)
- created_by, updated_by, created_at, updated_at, deleted_at
- 索引：isbn, title, FULLTEXT(title)

**author** (id, name UNIQUE)
**book_author** (book_id, author_id) 复合主键

**category** (id, code UNIQUE, name, parent_id)
**book_category** (book_id, category_id) 复合主键

**book_copy**
- id, book_id, barcode VARCHAR(32) UNIQUE
- status ENUM('IN_LIBRARY','BORROWED','LOST','MAINTENANCE')
- created_at, deleted_at
- 索引：barcode, (book_id, status)

**borrow_policy**
- id, max_borrow_count INT
- borrow_days INT
- renewable_count INT
- renew_days INT
- fine_per_day_cents BIGINT
- effective_from DATETIME
- created_by
- 设计：插入新策略不删旧策略，"当前策略 = effective_from <= now() 中最大者"

**borrowing**
- id, reader_id, book_copy_id, book_id (冗余便于查询)
- borrow_date DATE, due_date DATE, return_date DATE NULL
- renew_count INT
- status ENUM('BORROWED','RETURNED','OVERDUE_RETURNED')
- fine_amount_cents BIGINT
- fine_paid BOOLEAN
- 索引：(reader_id, status), (book_copy_id, status), (due_date, status)

**fine**
- id, borrowing_id, amount_cents
- status ENUM('UNPAID','PAID','WAIVED')
- paid_at, waived_by, waive_reason
- 设计：每次还书产生一条 UNPAID 记录；"借阅是否未结清" = 该借阅存在 UNPAID 的 fine

**refresh_token**
- id, user_account_id
- token_hash CHAR(64) UNIQUE
- issued_at, expires_at
- revoked_at, revoked_reason
- user_agent, ip
- 索引：user_account_id, expires_at

**operation_log**
- id, operator_id, role, action, target_type, target_id
- payload TEXT, ip, occurred_at, trace_id
- 索引：(target_type, target_id, occurred_at)

### 3.3 关键索引补充
- borrowing(reader_id, status)：读者在借列表
- borrowing(due_date, status)：每日逾期扫描
- book_copy(book_id, status)：选可借复本
- book FULLTEXT(title)：模糊搜索

### 3.4 并发安全
- 借书：在事务内对选中的 book_copy 行 SELECT … FOR UPDATE，更新 status；同时对 book 用乐观锁（version）-- available_copies
- 还书：对 borrowing + book_copy 行级锁
- 续借：仅更新 borrowing，单行加锁

### 3.5 金额
- 单位 cents (BIGINT)，避免 Double
- 领域 Money 值对象 + Currency（V1 仅 CNY）


## 4. API 设计

### 4.1 通用约定
- 路径前缀：/api/v1
- 响应体：`{ code, message, data, traceId }`
- 分页：page (1-based), size, sort=field,desc；返回 `{ items, total, page, size }`
- 鉴权：除 /auth/login、/auth/register、/auth/refresh、/api/v1/catalog/** 之外均需 `Authorization: Bearer <jwt>`

### 4.2 错误响应
- 业务错误：HTTP 200 + code=8xxx
- 401：code=8401(无token) / 8402(access过期) / 8403(refresh无效) / 8405(账号锁定)
- 403：code=8404 权限不足
- 400：code=8001 参数校验
- 409：code=8002 数据完整性冲突
- 429：code=8003 超出限流
- 500：code=9000

### 4.3 完整接口列表

#### 认证
| Method | Path | 说明 |
|--------|------|------|
| POST | /auth/register | 读者自助注册（可配置自动激活）|
| POST | /auth/login | 登录，返回 access + refresh |
| POST | /auth/refresh | 用 refresh 换 access，并轮换 refresh |
| POST | /auth/logout | access 入黑名单 + refresh 作废 |
| POST | /auth/logout-all | 当前用户所有 refresh 作废 |
| GET  | /auth/me | 当前用户信息 |

#### 图书（管理员）
| GET    | /books?keyword=&category=&page=&size= |
| POST   | /books |
| GET    | /books/{id} |
| PUT    | /books/{id} |
| DELETE | /books/{id} | 仅当无未还复本 |
| POST   | /books/{id}/copies | 增加复本 |
| DELETE | /copies/{id} | 报损/删除单复本 |

#### 图书（读者公开）
| GET | /catalog/books?keyword=&category=&page=&size= |
| GET | /catalog/books/{id} |

#### 读者
| GET    | /readers?keyword=&status=&page=&size= |
| POST   | /readers |
| GET    | /readers/{id} |
| PUT    | /readers/{id} |
| PUT    | /readers/{id}/status |

#### 借阅
| POST | /borrowings | body: {readerId, copyId} |
| POST | /borrowings/by-barcode | body: {readerNo, copyBarcode} |
| POST | /borrowings/{id}/return |
| POST | /borrowings/return-by-barcode | body: {copyBarcode} |
| POST | /borrowings/{id}/renew |
| POST | /borrowings/{id}/pay-fine | body: {amountCents} |
| GET  | /borrowings?readerId=&status=&overdue=&page=&size= |
| GET  | /borrowings/my | 当前读者本人 |

#### 规则
| GET | /policies/borrow | 当前生效策略 |
| GET | /policies/borrow/history?page=&size= |
| PUT | /policies/borrow | 生成新生效记录 |

#### 控制台
| GET | /dashboard/overview | 总书数、在借、今日借/还、逾期数 |
| GET | /dashboard/hot-books?days=30&limit=10 |
| GET | /dashboard/borrow-trend?days=30 |

### 4.4 资源鉴权
- READER 只能访问 /catalog/**、/borrowings/my、/auth/**
- LIBRARIAN 可访问全部
- 自动鉴权：`@PreAuthorize` + 服务层二次校验（防止越权访问他人借阅）


## 5. 认证与授权

### 5.1 Token 体系
- Access Token（JWT）：HS256，TTL 15 分钟，claims: `{sub:userAccountId, role, jti, iat, exp}`
- Refresh Token：256-bit 随机，TTL 7 天；服务端存储 SHA-256 hash；不是 JWT，不可被解码出业务信息
- 密钥：`LIBRARY_JWT_SECRET` 从环境变量读取，启动时校验长度 >= 32 字节，缺失或为默认值则启动失败

### 5.2 登录流程
1. POST /auth/login {username, password}
2. 服务端用 BCrypt 校验密码（strength=10）
3. 校验失败：账号失败计数器 +1（本地缓存，TTL 15min）；连续 5 次锁定 15 分钟，返回 8405
4. 校验成功：清除失败计数；签发 access + refresh，refresh 写库（明文计算 hash 后存）
5. 返回 `{ accessToken, refreshToken, accessExpiresIn, refreshExpiresIn, user }`

### 5.3 鉴权流程
1. JwtAuthFilter 解析 Bearer token
2. 校验签名、exp、jti 不在黑名单
3. 加载 UserDetails（role、status），status 非 ACTIVE 直接 401
4. 设置 SecurityContext

### 5.4 刷新流程
1. POST /auth/refresh {refreshToken}
2. 计算 hash 查 refresh_token：必须未 revoked、未过期
3. 轮换：旧 refresh 立即 revoked_at=now()；签发新 access + 新 refresh
4. 重放检测：若收到的 refresh 已被 revoked，视为可疑，**作废该用户全部 refresh** + 写 ERROR + 触发告警（防止 token 泄漏）

### 5.5 登出
- /auth/logout：access jti 加入黑名单（TTL = access 剩余有效期）；当前 refresh revoked
- /auth/logout-all：用户所有 refresh revoked

### 5.6 黑名单实现
- 接口 `TokenBlacklist { void add(String jti, Duration ttl); boolean contains(String jti); }`
- V1 默认 Caffeine 实现（单机）
- application.yml 切换到 Redis 实现：`library.token-blacklist.type=redis`

### 5.7 限流
- Bucket4j 本地实现，抽象 `RateLimiter` 接口
- 拦截器读取自定义 `@RateLimit(key="ip|user|ip+user", limit, period)` 注解
- 关键接口：

| 接口 | 规则 |
|------|------|
| POST /auth/login | IP 60/min + username 10/min |
| POST /auth/register | IP 5/min |
| POST /auth/refresh | userAccount 30/min |
| GET /catalog/books | IP 300/min |
| POST /borrowings, /return, /renew | LIBRARIAN 60/min |

- 超限：HTTP 429 + code=8003，响应 header `X-RateLimit-Remaining`、`Retry-After`
- 开关：`library.rate-limit.enabled=true`

### 5.8 密码安全
- BCrypt strength=10
- 注册接口要求密码 >= 8 位、包含字母+数字
- 重置密码暂未提供（V1 由管理员后台手动重置）


## 6. 核心业务流程

### 6.1 借书 BorrowingApplicationService.borrow(cmd)
**参数**：`{readerId, copyId}` 或 `{readerNo, copyBarcode}`

**流程**（事务内）：
1. 加载当前生效 BorrowPolicy
2. 加载 reader：必须 ACTIVE，否则抛 `BusinessException(8101)`
3. 计数读者在借数 = `count(borrowing where reader=? and status=BORROWED)`，>= maxBorrowCount → `8102`
4. 检查读者是否存在 status=BORROWED 且 due_date < today → `8103`
5. 检查读者是否有未结清 fine → `8202`
6. `SELECT … FOR UPDATE` 选定 copy，必须 status=IN_LIBRARY，否则 `8203`
7. 事务内：
   - 创建 borrowing(status=BORROWED, borrow_date=today, due_date=today+borrow_days)
   - book_copy.status=BORROWED
   - book.available_copies-=1（乐观锁 version）
8. 发布 BookBorrowedEvent
9. 返回 BorrowingVO

### 6.2 还书 BorrowingApplicationService.returnBook(cmd)
**参数**：`{borrowingId}` 或 `{copyBarcode}`（按 barcode 找 status=BORROWED 的活跃借阅）

**流程**：
1. 加锁加载 borrowing，必须 status=BORROWED → 否则 `8201`
2. 计算逾期天数 = max(0, today - due_date)；fineCents = 逾期天数 * fine_per_day_cents
3. 如果有逾期：fine 表插入一条 UNPAID 记录；borrowing.status=OVERDUE_RETURNED
4. 否则：borrowing.status=RETURNED
5. borrowing.return_date=today, fine_amount_cents=fineCents
6. book_copy.status=IN_LIBRARY
7. book.available_copies+=1（乐观锁）
8. 发布 BookReturnedEvent
9. 返回 ReturnResultVO {fineDueCents, overdueDays}

> **业务决策**：本期允许带欠费还书，但不允许带欠费再借（步骤 6.1 第 5 条）

### 6.3 续借 BorrowingApplicationService.renew(borrowingId)
1. 加锁加载 borrowing，必须 status=BORROWED 且 due_date >= today → 否则 `8301`
2. renew_count >= policy.renewable_count → `8302`
3. 读者无未结清 fine → 否则 `8202`
4. due_date += policy.renew_days
5. renew_count++

### 6.4 交罚款 BorrowingApplicationService.payFine(borrowingId, amountCents)
1. 找该 borrowing 下 UNPAID 的 fine
2. amountCents != fine.amountCents → `8701`
3. fine.status=PAID, paid_at=now()
4. 同步更新 borrowing.fine_paid=true（V1 暂只支持单笔全额支付）

### 6.5 逾期扫描定时任务 OverdueScheduler
- @Scheduled cron `0 0 2 * * *`，凌晨 2 点
- ShedLock 锁防多实例重复
- 逻辑：扫描 borrowing(status=BORROWED, due_date < today)
- V1 仅打印 INFO 日志和写 operation_log（NotificationService 留接口，未启用消息发送）

### 6.6 规则变更 PolicyApplicationService.update(cmd)
- 不修改旧记录，插入一条新 borrow_policy(effective_from=now())
- 之后所有借/还/续操作读"当前生效策略"
- 历史可追溯，已发生借阅按其当时策略保留 due_date 不回改

### 6.7 领域事件
- BookBorrowedEvent / BookReturnedEvent / FinePaidEvent
- 通过 Spring ApplicationEventPublisher 发布
- V1 仅有审计日志监听器；预留接口供 Notification、Stats 接入

## 7. 错误处理与可观测性

### 7.1 异常体系
- `BusinessException(ResultCode, message)` - 应用层抛业务错误
- `AuthenticationException`、`AccessDeniedException` 由 Spring Security 抛，全局处理器映射
- `MethodArgumentNotValidException` → 8001
- `DataIntegrityViolationException` → 8002，message 经 SqlExceptionTranslator 翻译为友好提示
- 未识别 Exception → 9000 + ERROR 日志 + AlertSender.sendError()

### 7.2 GlobalExceptionHandler
- 所有响应统一 Result 包装
- 不向外吐露堆栈；message 限定为业务友好文案

### 7.3 严禁吞异常
- Service 层只允许 catch 特定异常并重抛为 BusinessException 或 RuntimeException
- 任何 `catch (Exception e) {}` 在 PR 阶段必须给出注释和 alert 调用，否则 ArchUnit + checkstyle 拦截

### 7.4 日志
- Logback JSON 输出
- access log：method, path, status, durationMs, userId, userRole, traceId
- traceId：请求头 `X-Trace-Id` 透传，无则生成 UUID；MDC 注入；响应头返回
- 业务关键点（borrow/return/renew/payFine/policy update/reader status）打 INFO

### 7.5 错误码总表

| code | 含义 |
|------|------|
| 0 | 成功 |
| 8001 | 参数校验失败 |
| 8002 | 数据完整性冲突 |
| 8003 | 超出限流 |
| 8101 | 读者被禁用 |
| 8102 | 在借数超过上限 |
| 8103 | 存在逾期未归还记录 |
| 8104 | 读者不存在 |
| 8105 | 读者证号已存在 |
| 8201 | 借阅记录状态不可还 |
| 8202 | 未付清罚款不能进一步操作 |
| 8203 | 未找到可借/可还复本 |
| 8301 | 该套记录不可续借 |
| 8302 | 续借次数超过上限 |
| 8401 | 未提供有效 token |
| 8402 | access token 过期 |
| 8403 | refresh token 无效/过期/已 revoke |
| 8404 | 权限不足 |
| 8405 | 账号被锁定 |
| 8501 | ISBN 重复 |
| 8502 | 复本条码重复 |
| 8503 | 存在未归还复本，不能删除该书 |
| 8504 | 复本被借出，不能报损/删除 |
| 8601 | 策略参数越界 |
| 8701 | 付款金额不一致 |
| 9000 | 系统内部错误 |
| 9001 | 依赖服务调用失败 |

### 7.6 告警
- AlertSender 接口；V1 LoggingAlertSender 仅写 ERROR 日志
- 生产可替换为 HubbleAlertSender / DingTalkAlertSender
- 未捕获异常、refresh token 重放、连续告警阈值 → 触发


## 8. 前端设计

### 8.1 技术栈
- Vue 3.4 + TypeScript + Vite 5
- Pinia + Vue Router 4
- Element Plus 2.x（主 UI）
- vue-i18n 9（zh / en 双语）
- ECharts 5（控制台图表）
- axios + axios-retry
- 工具：ESLint + Prettier + Stylelint + Husky + lint-staged

### 8.2 项目结构
```
library-web/
  src/
    api/
      request.ts           axios 实例 + 拦截器 + 自动 refresh
      auth.ts books.ts readers.ts borrowings.ts policies.ts dashboard.ts
    stores/
      user.ts              token / 用户信息 / 角色 / 权限
      app.ts               侧栏折叠 / 语言 / 主题
    router/
      index.ts             路由表 + 异步加载
      guards.ts            认证 / 角色 / 越权
    layouts/
      AdminLayout.vue
      ReaderLayout.vue
      BlankLayout.vue
    views/
      auth/Login.vue Register.vue
      books/BookList.vue BookForm.vue BookDetail.vue
      readers/ReaderList.vue ReaderForm.vue ReaderDetail.vue
      borrowings/
        BorrowList.vue OverdueList.vue MyBorrowings.vue
        components/BorrowDialog.vue ReturnDialog.vue PayFineDialog.vue
      catalog/CatalogSearch.vue CatalogBookDetail.vue
      settings/RuleConfig.vue
      dashboard/Dashboard.vue
    components/global/
      ErrorBoundary.vue PageContainer.vue LangSwitch.vue
      RoleGuard.vue PermissionButton.vue
    composables/
      useTable.ts useForm.ts useAuth.ts usePermission.ts useRefreshToken.ts
    i18n/
      index.ts
      locales/
        zh/common.json zh/menu.json zh/error.json zh/business.json
        en/common.json en/menu.json en/error.json en/business.json
    utils/
      auth.ts permission.ts datetime.ts money.ts trace.ts
    styles/
      element-overrides.scss variables.scss
```

### 8.3 axios 拦截器
- 请求拦截：注入 Authorization、X-Trace-Id（前端 nanoid 生成）、Accept-Language
- 响应拦截：
  - code=0 返回 data
  - 8401 → 跳登录
  - 8402 → 调用 refresh，串行队列防重复刷新；refresh 失败 → 清 token + 跳登录
  - 8403 / 8405 → 清 token + 跳登录 + Toast
  - 8404 → ElMessage.error('error.8404')，不跳转
  - 8003 → ElMessage.warning('error.8003')
  - 其它 8xxx → ElMessage.warning(i18n('error.${code}', message))
  - 9xxx / 网络 → ElMessage.error + Sentry.captureException（如启用）

### 8.4 路由与权限
- meta: `{ requiresAuth: true, roles: ['LIBRARIAN'] }`
- guards 顺序：登录态 → 角色 → 单页钩子（`beforeEnter`）
- READER 登录后默认跳 /catalog；LIBRARIAN 跳 /dashboard
- 菜单：根据 role + 权限点动态生成（v-permission 指令控制按钮可见性）

### 8.5 i18n
- 默认 zh，启动时读取 localStorage `library:lang`，否则 `navigator.language` 选 fallback
- 文案模块化拆分：common / menu / error / business
- 业务错误码对照 i18n key：`error.${code}`，message 作为兜底
- 日期/数字/货币：使用 vue-i18n 内置 `$d / $n` 配合 dayjs

### 8.6 表格与表单
- el-table-v2 大数据虚拟滚动
- useTable composable 统一封装：分页、排序、加载、关键字、刷新
- el-form 配合 async-validator；统一表单容器组件 PageForm

### 8.7 响应式
- 默认桌面端；读者端 (/catalog, /borrowings/my) 提供移动端简化 layout
- 断点：768px / 1200px

### 8.8 端到端冒烟测试（Playwright）
- 场景：登录 → 添加书 → 借书 → 还书 → 看板出数
- CI 中跑无头浏览器；本地可有头调试


## 9. 测试策略

### 9.1 测试金字塔
```
                  E2E (Playwright)        ~10 条核心剧本
                Integration Tests         ~80 条
              Unit Tests (domain+app)     ~300 条
```

### 9.2 后端
- **单元测试**（JUnit 5 + Mockito + AssertJ）
  - domain：纯业务规则，无 Spring 上下文
  - application：mock Repository / EventPublisher
- **集成测试**（@SpringBootTest + Testcontainers MySQL 8 + REST-Assured）
  - 控制器层：正常路径、参数错误、未认证、越权、限流、JWT 过期、refresh、并发借同一复本（多线程）
  - 数据迁移：Flyway 跑通
- **架构测试**（ArchUnit）
  - 包依赖方向
  - controller 不直接 new entity / 不直接调 repository
  - 严禁 catch (Exception e) {} 空块（结合 Spotbugs / Checkstyle）

### 9.3 前端
- **组件测试**（Vitest + @vue/test-utils）
  - LoginForm、BookForm、BorrowDialog、ReturnDialog、RuleConfigForm
- **Store 测试**：user store token 自动刷新逻辑
- **API mock 测试**：MSW 模拟后端响应，测试拦截器分支

### 9.4 E2E
- Playwright；登录 → 加书 → 借 → 还 → 看板
- docker-compose 起 mysql + api + web，CI 内执行

### 9.5 覆盖率目标
- domain ≥ 80%（指令覆盖率）
- application ≥ 70%
- 整体 ≥ 60%
- CI 失败阈值：低于上述任一即 fail

## 10. 部署与运维

### 10.1 配置
- application.yml + application-{profile}.yml
- 必须从环境变量读取的：DB url/user/pwd、JWT secret、Redis（如启用）、CORS allowed origins
- 启动时校验关键变量缺失即 fail-fast

### 10.2 Docker Compose（开发/演示用）
- mysql:8.0
- redis:7-alpine（可选）
- library-api: 多阶段构建，jdk17-slim
- library-web: nginx 静态托管 + 反向代理 /api → library-api
- 一键命令：`docker compose up -d`

### 10.3 CI（GitHub Actions / 内部 Jenkins）
- Job 1：后端 mvn verify（含 Testcontainers）+ 上传 JaCoCo 报告
- Job 2：前端 pnpm lint + pnpm test + pnpm build
- Job 3：E2E（仅 main / release/* 分支）
- artifact：jar + dist.zip + docker image

### 10.4 上线检查清单
- [ ] JWT_SECRET 非默认
- [ ] DB 连接池/慢查询日志已配置
- [ ] Redis 已配置（如启用）
- [ ] 限流开启
- [ ] 黑名单切到 Redis 实现
- [ ] 慢接口与 5xx 监控告警接入
- [ ] 备份策略已就绪（每日全量 + binlog）

## 11. 交付计划（Sub-projects）

将整个系统按你选择的"全量交付"目标拆为 4 个 sub-project，每个独立走 brainstorming → spec → plan → 实现 → 验证。

### Sub-project A：底座 + 认证（"骨架可跑通"）
- 后端：项目骨架、Flyway、user_account、JWT、Refresh、黑名单、限流、全局异常、ArchUnit、TraceId、AlertSender 抽象
- 前端：项目骨架、Vite/ESLint/Prettier、登录/注册页、布局、路由守卫、i18n 框架
- 验收：本地 docker compose 起来后能注册、登录、刷新、登出，能看到空白控制台

### Sub-project B：图书 + 读者
- 表：book、book_copy、author、book_author、category、book_category、reader
- API：图书 CRUD + 复本管理；读者 CRUD + 启用/禁用；读者端 /catalog/books
- 前端：BookList/BookForm/BookDetail/CopyManage、ReaderList/ReaderForm、CatalogSearch
- 验收：管理员可加书加复本、读者能搜书

### Sub-project C：借阅 + 规则 + 罚款
- 表：borrow_policy、borrowing、fine
- API：borrow / by-barcode / return / renew / pay-fine、policies/borrow
- 定时任务：OverdueScheduler + ShedLock
- 前端：BorrowDialog/ReturnDialog/PayFineDialog、BorrowList、OverdueList、MyBorrowings、RuleConfig
- 验收：完整借还闭环 + 规则变更 + 罚款支付

### Sub-project D：看板 + i18n + 部署
- 表：operation_log
- API：dashboard/overview、hot-books、borrow-trend
- 前端：Dashboard（ECharts 图表）+ 双语全量 + 操作日志页
- 部署：docker-compose、nginx 配置、README、一键脚本
- 验收：从零到上线 5 分钟内完成；E2E 全部通过

每个 sub-project 完成后单独 commit + 总结报告，确保中间可回退、可对照。

## 12. 风险与未决项

| 风险 | 影响 | 缓解 |
|------|------|------|
| 中型规模并发借阅热点书 | 死锁 / 行锁等待 | book_copy 行锁 + book 乐观锁；监控慢事务 |
| token 泄漏 | 越权访问 | refresh 重放检测 + 黑名单 + IP/UA 记录 |
| 罚款规则变更后历史数据混乱 | 计算口径不一致 | borrow_policy 历史记录 + 借阅产生时锁定 due_date |
| 单机黑名单失效 | 多实例登出不同步 | 抽象接口预留 Redis 升级，文档显式说明 |
| Flyway 演进 | 联机 DDL 风险 | 拒绝在生产做 alter；新表/可回退迁移 |

未决项（V2+）：
- 多分馆 / 库位精细化
- 预约排队
- 邮件 / 短信通知
- 报表导出
- ESL（电子书架标签）/ 自助机硬件接入

## 附录 A：术语
- Borrowing 借阅记录（一次借出 → 还回）
- BookCopy 物理复本
- BorrowPolicy 借阅策略（全局生效）
- Fine 罚款流水

## 附录 B：本设计未做的决定（默认值）
- 单 Maven 项目（非多模块），ArchUnit 强约束
- 不引入预约排队
- Token 黑名单 V1 用 Caffeine
- 罚款 V1 仅支持单笔全额
- 操作日志默认开启但仅写关键动作
