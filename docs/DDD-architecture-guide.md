# DDD 架构设计指南

本文档以图书馆管理系统为案例，详细讲解 DDD（领域驱动设计）四层架构的设计理念和实践方法。

---

## 目录

- [一、什么是 DDD](#一什么是-ddd)
- [二、四层架构](#二四层架构)
- [三、各层详解](#三各层详解)
- [四、依赖规则](#四依赖规则)
- [五、核心概念](#五核心概念)
- [六、贫血模型 vs 充血模型](#六贫血模型-vs-充血模型)
- [七、DDD vs MVC 对比](#七ddd-vs-mvc-对比)
- [八、数据库解耦](#八数据库解耦)
- [九、适用场景](#九适用场景)
- [十、以"图书"模块为例的完整映射](#十以图书模块为例的完整映射)

---

## 一、什么是 DDD

**领域驱动设计（Domain-Driven Design）** 是由 Eric Evans 在 2003 年提出的软件设计方法论。

核心思想：**让代码结构反映业务结构，而不是技术结构。**

- MVC 按技术维度分层（Controller / Service / DAO）
- DDD 按业务职责分层（接口 / 应用 / 领域 / 基础设施）

一句话概括 MVC 和 DDD 的关系：

```
MVC 的 Service = DDD 的 Application 层 + Domain 层
```

DDD 将 MVC 中"什么都做"的 Service 层拆成两半：
- **Application 层**：负责流程编排（先查什么→再查什么→最后存什么）
- **Domain 层**：负责业务规则（能不能借书、怎么算罚款）

---

## 二、四层架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    Interfaces 接口层                             │
│  职责: 接收外部请求，返回响应                                      │
│  内容: Controller, DTO, Filter, 异常处理, 权限注解                │
│  类比: "前台接待" — 只负责接客和转达，不做任何业务判断               │
├─────────────────────────────────────────────────────────────────┤
│                    Application 应用层                            │
│  职责: 编排业务流程，管理事务                                      │
│  内容: ApplicationService, Command, View                        │
│  类比: "项目经理" — 协调各部门完成任务，自己不制定规则               │
├─────────────────────────────────────────────────────────────────┤
│                    Domain 领域层（核心）                          │
│  职责: 封装业务规则和状态变更逻辑                                  │
│  内容: Entity, ValueObject, Repository(接口), Enum               │
│  类比: "业务专家" — 知道所有规则，纯 Java，零框架依赖              │
├─────────────────────────────────────────────────────────────────┤
│                    Infrastructure 基础设施层                      │
│  职责: 提供技术能力（数据库、缓存、安全、外部服务）                  │
│  内容: JPA Entity + Repository实现, JWT, 限流, 缓存               │
│  类比: "IT部门" — 提供技术工具，但不做业务决策                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、各层详解

### 3.1 Interfaces 接口层

**职责**：接收外部请求（HTTP），校验参数格式，将请求转发给 Application 层，将结果格式化返回。

**包含内容**：
- REST Controller（`BookController`, `AuthController` 等）
- DTO（`BookRequest`, `BookResponse` 等）
- Filter（`JwtAuthFilter`, `TraceIdFilter`）
- 异常处理（`GlobalExceptionHandler`）
- 权限注解（`@RequireLibrarian`, `@RequireReader`）

**不做的事**：
- 不包含业务规则
- 不直接访问数据库
- 不调用 Infrastructure 层

### 3.2 Application 应用层

**职责**：编排业务流程，管理事务边界。像"项目经理"一样协调领域对象完成工作。

**包含内容**：
- ApplicationService（`BookApplicationService`, `BorrowApplicationService` 等）
- Command 命令对象（`CreateBookCommand`, `UpdateBookCommand` 等）
- View 视图对象（`AdminDashboardView`, `ReaderDashboardView`）

**关键原则**：
- Application Service **不包含业务规则**
- 只负责"先查什么→再调什么→最后存什么"的编排
- 使用 `@Transactional` 管理事务

### 3.3 Domain 领域层（核心）

**职责**：封装所有业务规则和业务概念。这是整个系统最重要的一层。

**包含内容**：
- Entity 领域实体（`Book`, `Reader`, `BorrowRecord` 等）
- Repository 仓储接口（`BookRepository`, `ReaderRepository` 等）
- Enum 枚举（`CopyStatus`, `BorrowStatus`, `FineStatus` 等）
- Shared（`Result<T>`, `BusinessException`, `ResultCode`）

**关键原则**：
- **零框架依赖**：不使用 Spring、JPA 或任何第三方框架
- 只导入 `java.*` 标准库
- Entity 是"充血模型"：不仅有数据，还有业务方法
- Repository 只是接口定义，不关心底层存储

**重要区分**：Domain 层的 Entity 和数据库 Entity 是**两个不同的类**：

| | Domain Entity | JPA Entity |
|---|---|---|
| 所在层 | Domain | Infrastructure |
| 代表什么 | 业务概念 | 数据库表的一行 |
| 有注解吗 | 没有 | 有 `@Entity`, `@Table`, `@Column` |
| 有业务方法吗 | 有 | 没有（只有 getter/setter） |
| 本项目示例 | `Book.java` | `BookJpaEntity.java` |

### 3.4 Infrastructure 基础设施层

**职责**：为上层提供技术能力的具体实现。

**包含内容**：
- JPA Entity（`BookJpaEntity`, `ReaderJpaEntity` 等）— 数据库表映射
- Repository 实现（`BookRepositoryImpl` 等）— 实现 Domain 层定义的接口
- Mapper（`BookMapper` 等）— Domain Entity ↔ JPA Entity 转换
- Security（`JwtService`, `LoginAttemptService`, `CaffeineTokenBlacklist`）
- RateLimit（`LocalBucketRateLimiter`）— 令牌桶限流
- Alert（`LoggingAlertSender`）— 告警发送

**关键原则**：
- 实现 Domain 层定义的接口（依赖倒置）
- 所有技术细节封装在这里
- 可替换性：换数据库只需换这一层

---

## 四、依赖规则

```
Interfaces → Application → Domain ← Infrastructure
                             ↑
                        核心层，被依赖
                        但自己不依赖任何层
```

**依赖倒置原则**：
- 高层模块（Domain）定义接口（`BookRepository`）
- 低层模块（Infrastructure）提供实现（`BookRepositoryImpl`）
- Application 层只依赖 Domain 层的接口，不知道底层用什么数据库

**架构守护**：本项目使用 ArchUnit 测试强制执行依赖规则：

```java
layeredArchitecture()
    .layer("Interfaces").definedBy("..interfaces..")
    .layer("Application").definedBy("..application..")
    .layer("Domain").definedBy("..domain..")
    .layer("Infrastructure").definedBy("..infrastructure..")
    .whereLayer("Domain").mayNotAccessAnyLayer()
```

各层对 Spring 的依赖情况：

| 层 | 依赖 Spring？ | 原因 |
|---|---|---|
| Domain | **不依赖** | 保持业务逻辑纯粹 |
| Application | 依赖 | 需要 `@Service`, `@Transactional` |
| Interfaces | 依赖 | 需要 `@RestController`, `@GetMapping` |
| Infrastructure | 依赖 | 需要 JPA, Spring Security |

---

## 五、核心概念

| 概念 | 说明 | 本项目示例 |
|------|------|-----------|
| **Entity（实体）** | 有唯一标识 + 生命周期 + 业务行为 | `Book`, `BorrowRecord`, `Reader` |
| **Value Object（值对象）** | 无唯一标识，按值比较，不可变 | `ResultCode`, `BorrowStatus` |
| **Repository（仓储）** | 领域对象的存取接口 | `BookRepository`（接口） |
| **Application Service** | 用例编排器（不含业务规则） | `BorrowApplicationService` |
| **Command（命令）** | 操作意图的不可变数据载体 | `UpdateBookCommand` (Java record) |
| **Aggregate（聚合）** | 一组实体的一致性边界 | `Book` + `BookCopy` 构成一个聚合 |

### Command 对象的作用

Command 是 Application 层的输入参数封装，用于隔离接口层和应用层：

```
Controller       →     Command       →    ApplicationService
(接收 DTO)              (传递意图)          (编排流程)
BookRequest    →   UpdateBookCommand  →   BookApplicationService
```

Command 使用 Java `record`，天然不可变：

```java
public record UpdateBookCommand(
    Long id,
    String title,
    String publisher,
    Integer publishYear,
    String location,
    String summary,
    List<String> authorNames,
    List<String> categoryCodes
) {}
```

---

## 六、贫血模型 vs 充血模型

这是 DDD 最核心的设计理念。

### 贫血模型（MVC 常见）

Entity 只是数据容器，所有逻辑在 Service：

```java
// Entity — 只有 getter/setter
public class Book {
    private int availableCopies;
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int c) { availableCopies = c; }
}

// Service — 包含所有规则
public class BookService {
    public void borrowBook(Long bookId) {
        Book book = bookDao.findById(bookId);
        if (book.getAvailableCopies() <= 0) {
            throw new Exception("没有可借复本");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookDao.save(book);
    }
}
```

问题：规则散落在 Service 各处，Entity 是"贫血"的数据袋。

### 充血模型（DDD）

Entity 自己封装规则，保护自己的状态：

```java
// Entity — 有业务方法，不暴露 setter
public class Book {
    private int availableCopies;

    public void borrowCopy() {
        if (availableCopies <= 0) {
            throw new IllegalStateException("No available copies");
        }
        availableCopies--;
    }
    // 没有 setAvailableCopies() — 外部不能随意修改
}

// Service — 只编排，不写规则
public class BorrowApplicationService {
    public void borrowBook(Long bookId) {
        Book book = bookRepository.findById(bookId)...;
        book.borrowCopy();  // 调用实体方法
        bookRepository.save(book);
    }
}
```

优势：业务规则集中在 Entity 内部，Service 只负责协调。

---

## 七、DDD vs MVC 对比

| 维度 | MVC | DDD |
|------|-----|-----|
| 分层 | 3 层（Controller/Service/DAO） | 4 层 |
| 业务逻辑位置 | 堆在 Service 层 | 封装在 Entity 内部 |
| Entity | = 数据库表映射 | ≠ 数据库映射（独立的领域模型） |
| Service 职责 | 什么都做 | 只编排流程 |
| 框架依赖 | 全层依赖 Spring | 领域层零框架依赖 |
| 可测试性 | 需要 Mock 数据库 | 领域层可纯单元测试 |
| 代码量 | 少 | 多（多了一层 + 两套模型） |
| 适用场景 | 简单 CRUD | 业务规则复杂的系统 |

### 类比理解

```
MVC:  前台(Controller) → 全能员工(Service) → 文件柜(DAO)
DDD:  前台(Interfaces) → 经理(Application) → 专家(Domain) ← IT(Infrastructure)
```

---

## 八、数据库解耦

### 解耦原理

Domain 层定义接口，Infrastructure 层实现。Application 层只依赖接口：

```
Domain 层（定义接口）              Infrastructure 层（实现接口）
┌─────────────────────┐       ┌──────────────────────────────┐
│ BookRepository      │       │ BookRepositoryImpl           │
│ (interface)         │◄──────│ @Repository                  │
│                     │       │ implements BookRepository     │
│ findById(Long)      │       │   ├── 用 JPA 查 MySQL        │
│ save(Book)          │       │   └── BookMapper 做转换       │
└─────────────────────┘       └──────────────────────────────┘
```

### 替换数据库的影响范围

| 层 | 文件数 | 需要改？ |
|----|--------|---------|
| Domain | 25 | 0 个 |
| Application | 17 | 0 个 |
| Interfaces | 42 | 0 个 |
| **Infrastructure** | **46** | **约 30 个** |
| 配置文件 | 7 | 2-3 个 |

换数据库只需要改 Infrastructure 层，其余代码完全不受影响。

### 诚实地说

如果 MVC 也做了接口抽象（`Service → DAO 接口 → DAO 实现`），替换数据库的步骤差不多。DDD 在数据库解耦上的优势不算很大。

DDD 的真正价值不是"方便换数据库"（实际项目很少换数据库），而是：

1. **业务逻辑集中** — 规则在 Entity 里，不散落在 Service 各处
2. **可测试性** — 领域逻辑可以纯单元测试
3. **团队协作** — 不同人改不同层，互不影响
4. **长期维护** — 业务变化时改动范围可控

---

## 九、适用场景

### 推荐使用 DDD

- 业务规则多且交叉（借阅系统、电商、金融）
- 团队多人协作，需要清晰边界
- 需要长期维护和迭代的系统
- 对代码质量有较高要求

### 推荐使用 MVC

- 简单 CRUD 项目（博客、Todo）
- 业务规则少，主要是增删改查
- 原型开发 / 快速验证
- 小团队或个人项目

### DDD 的代价

1. **代码量增加** — 多了一层 + 两套模型（Domain Entity + JPA Entity）+ Mapper
2. **学习曲线陡** — 需要理解聚合、仓储、值对象等概念
3. **过度设计风险** — 简单项目用 DDD 会导致不必要的复杂度
4. **团队共识** — 如果团队不理解 DDD，很容易退化成"换了名字的 MVC"

---

## 十、以"图书"模块为例的完整映射

### 文件对应关系

```
library-api/src/main/java/com/library/

interfaces/               ← 接口层
├── rest/BookController.java          HTTP 入口
├── dto/book/
│   ├── BookRequest.java              请求 DTO
│   ├── BookResponse.java             响应 DTO
│   ├── BookCopyResponse.java
│   └── CopyRequest.java

application/              ← 应用层
├── book/
│   ├── BookApplicationService.java   流程编排
│   └── command/
│       ├── CreateBookCommand.java    操作指令
│       └── UpdateBookCommand.java

domain/                   ← 领域层（核心）
├── book/
│   ├── Book.java                     领域实体（含业务规则）
│   ├── BookCopy.java                 领域实体
│   ├── BookRepository.java           仓储接口
│   └── CopyStatus.java              枚举

infrastructure/           ← 基础设施层
├── persistence/jpa/book/
│   ├── BookJpaEntity.java            数据库映射（@Entity）
│   ├── BookJpaRepository.java        Spring Data JPA
│   ├── BookRepositoryImpl.java       实现领域接口
│   ├── BookMapper.java               Domain ↔ JPA 转换
│   └── ...（Author, Category 等）
```

### 完整数据流

```
HTTP 请求: PUT /api/v1/books/1 { title: "新书名" }
  ↓
Interfaces 层: BookController
  │  @Valid 校验参数格式
  │  @RequireLibrarian 检查权限
  │  BookRequest → UpdateBookCommand
  ↓
Application 层: BookApplicationService.updateBook(command)
  │  bookRepository.findById(1)       ← 调用 Domain 接口
  │  book.updateInfo(...)              ← 调用 Domain 方法
  │  bookRepository.save(book)         ← 调用 Domain 接口
  │  @Transactional 保证原子性
  ↓
Domain 层: Book.updateInfo(...)
  │  Objects.requireNonNull(title)     ← 业务校验
  │  this.title = title                ← 状态变更
  │  this.updatedAt = now
  ↓
Infrastructure 层: BookRepositoryImpl.save(book)
  │  BookMapper: Book → BookJpaEntity  ← 模型转换
  │  BookJpaRepository.save(entity)    ← JPA 持久化
  ↓
MySQL 数据库: UPDATE book SET title = '新书名' WHERE id = 1
```

---

**总结：DDD 不是银弹，它是用更多的结构性成本换取业务逻辑的清晰度和可维护性。理解它的核心在于理解"MVC 的 Service = DDD 的 Application + Domain"这一拆分。**
