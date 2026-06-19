# Sub-project D 实现计划：Dashboard、i18n 与部署 (1/2) - 业务与国际化

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现管理员和读者的仪表盘统计数据 API 与视图，并完善国际化 (i18n) 支持。
**Architecture:** DDD 4层架构。
**Tech Stack:** Java 17/21, Spring Boot 3.2, Vue 3, vue-i18n.

---

## 任务 D1：Dashboard 应用层与 API

**Files:**
- Create: `library-api/src/main/java/com/library/application/dashboard/DashboardApplicationService.java`
- Create: `library-api/src/main/java/com/library/interfaces/dto/dashboard/AdminDashboardResponse.java`
- Create: `library-api/src/main/java/com/library/interfaces/dto/dashboard/ReaderDashboardResponse.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/DashboardController.java`

- [ ] **Step 1: 编写 DashboardApplicationService**

```java
package com.library.application.dashboard;
// ... imports
@Service
public class DashboardApplicationService {
    // Inject repositories (Book, Reader, BorrowRecord, FineRecord)
    
    public AdminDashboardResponse getAdminStats() {
        // count total books, total readers
        // count active borrows (status = BORROWING or OVERDUE)
        // sum unpaid fines
        // return AdminDashboardResponse
    }
    
    public ReaderDashboardResponse getReaderStats(Long userAccountId) {
        // find reader by userAccountId
        // count active borrows for this reader
        // sum unpaid fines for this reader
        // return ReaderDashboardResponse
    }
}
```

- [ ] **Step 2: 编写 DashboardController**

```java
package com.library.interfaces.rest;
// ... imports
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    
    @GetMapping("/admin")
    @RequireLibrarian
    public Result<AdminDashboardResponse> getAdminDashboard() {
        // call service
    }
    
    @GetMapping("/reader")
    @RequireReader
    public Result<ReaderDashboardResponse> getReaderDashboard(@RequestAttribute("userId") Long userId) {
        // call service
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/application/dashboard/ library-api/src/main/java/com/library/interfaces/
git commit -m "feat(api): dashboard application service and controller"
```

## 任务 D2：前端 Dashboard 视图与 API 绑定

**Files:**
- Create: `library-web/src/api/dashboard.ts`
- Modify: `library-web/src/views/dashboard/Dashboard.vue`

- [ ] **Step 1: 编写 API 绑定**

```typescript
// dashboard.ts
import request from './request'
export const getAdminDashboard = () => request.get('/dashboard/admin')
export const getReaderDashboard = () => request.get('/dashboard/reader')
```

- [ ] **Step 2: 完善 Dashboard.vue**
根据当前用户的 role (`userStore.role`)，调用不同的 API。
使用 `el-row`, `el-col`, `el-card`, `el-statistic` 展示统计数据。

- [ ] **Step 3: Commit**
```bash
git add library-web/src/api/dashboard.ts library-web/src/views/dashboard/
git commit -m "feat(web): dashboard views and api bindings"
```

## 任务 D3：完善国际化 (i18n)

**Files:**
- Modify: `library-web/src/i18n/en.json`
- Modify: `library-web/src/i18n/zh-CN.json`
- Modify: `library-api/src/main/java/com/library/domain/shared/exception/ResultCode.java` (确保错误信息可被前端翻译，或后端直接支持 i18n)

- [ ] **Step 1: 完善前端语言包**
确保菜单、按钮、表单标签、常见提示信息在 `en.json` 和 `zh-CN.json` 中都有对应的翻译。

- [ ] **Step 2: 前端拦截器错误信息翻译**
在 `library-web/src/api/request.ts` 中，根据后端的 `code` 映射到前端的 i18n 错误信息配置。

- [ ] **Step 3: Commit**
```bash
git add library-web/src/i18n/ library-web/src/api/request.ts
git commit -m "feat(web): complete i18n support"
```
