# Sub-project C 实现计划：借阅、规则与罚款 (3/3) - 前端视图

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现前端的借阅规则配置、管理员借还书操作、读者个人借阅/罚款查看。

---

## 任务 C8：前端 API 绑定

**Files:**
- Create: `library-web/src/api/rules.ts`
- Create: `library-web/src/api/borrows.ts`
- Create: `library-web/src/api/fines.ts`

- [ ] **Step 1: 编写 API 绑定**

```typescript
// rules.ts
import request from './request'
export const getRules = () => request.get('/rules')
export const updateRule = (id: number, data: any) => request.put(`/rules/${id}`, data)

// borrows.ts
import request from './request'
export const getBorrows = (params: any) => request.get('/borrows', { params })
export const borrowBook = (data: any) => request.post('/borrows', data)
export const returnBook = (id: number) => request.put(`/borrows/${id}/return`)
export const getMyBorrows = (params: any) => request.get('/my/borrows', { params })
export const renewBook = (id: number) => request.put(`/my/borrows/${id}/renew`)

// fines.ts
import request from './request'
export const getFines = (params: any) => request.get('/fines', { params })
export const payFine = (id: number, data: any) => request.put(`/fines/${id}/pay`, data)
export const getMyFines = (params: any) => request.get('/my/fines', { params })
```

- [ ] **Step 2: Commit**
```bash
git add library-web/src/api/
git commit -m "feat(web): api bindings for rules, borrows, fines"
```

## 任务 C9：管理端视图 (Admin)

**Files:**
- Create: `library-web/src/views/settings/RuleConfig.vue`
- Create: `library-web/src/views/borrows/BorrowManage.vue`
- Create: `library-web/src/views/fines/FineManage.vue`
- Modify: `library-web/src/router/index.ts`

- [ ] **Step 1: 编写 RuleConfig.vue**
展示不同读者类型的规则，支持编辑（最大借阅天数、最大借阅数、最大续借数、每日罚金）。

- [ ] **Step 2: 编写 BorrowManage.vue**
顶部表单：输入 Reader No 和 Barcode 进行借书操作。
下方表格：展示所有借阅记录，支持按状态、读者、条码搜索。操作列提供 "Return" 按钮。

- [ ] **Step 3: 编写 FineManage.vue**
展示所有罚款记录。操作列提供 "Pay" 按钮（弹出确认框，输入金额）。

- [ ] **Step 4: Commit**
```bash
git add library-web/src/views/settings/ library-web/src/views/borrows/ library-web/src/views/fines/
git commit -m "feat(web): admin views for rules, borrows, fines"
```

## 任务 C10：读者端视图 (Reader)

**Files:**
- Create: `library-web/src/views/my/MyBorrows.vue`
- Create: `library-web/src/views/my/MyFines.vue`
- Modify: `library-web/src/router/index.ts`

- [ ] **Step 1: 编写 MyBorrows.vue**
展示当前登录读者的借阅记录。
对于状态为 `BORROWING` 的记录，提供 "Renew" 按钮。

- [ ] **Step 2: 编写 MyFines.vue**
展示当前登录读者的罚款记录。

- [ ] **Step 3: Commit**
```bash
git add library-web/src/views/my/ library-web/src/router/index.ts
git commit -m "feat(web): reader views for own borrows and fines"
```

---

## 验证与交接

1. **后端验证**：`mvn verify` 确保测试通过。
2. **前端验证**：`npm run build` 确保 TypeScript 编译通过。
3. **冒烟测试**：
   - 配置借阅规则。
   - 管理员为读者借出一本书。
   - 读者登录，查看 `My Borrows`，点击续借。
   - 管理员操作归还该书。

**Plan complete and saved to `docs/superpowers/plans/2026-06-19-sub-project-c-borrowing-and-rules-*.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration
**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
