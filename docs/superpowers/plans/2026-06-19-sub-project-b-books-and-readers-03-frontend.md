# Sub-project B 实现计划：图书与读者 (3/3) - 前端视图

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现前端的图书管理、读者管理以及读者端的公开目录搜索页面。

---

## 任务 B8：前端 API 绑定

**Files:**
- Create: `library-web/src/api/books.ts`
- Create: `library-web/src/api/readers.ts`
- Create: `library-web/src/api/catalog.ts`

- [ ] **Step 1: 编写 API 绑定**

```typescript
// books.ts
import request from './request'
export const getBooks = (params: any) => request.get('/books', { params })
export const createBook = (data: any) => request.post('/books', data)
export const updateBook = (id: number, data: any) => request.put(`/books/${id}`, data)
export const deleteBook = (id: number) => request.delete(`/books/${id}`)
export const addCopy = (bookId: number, data: any) => request.post(`/books/${bookId}/copies`, data)
export const deleteCopy = (copyId: number) => request.delete(`/copies/${copyId}`)

// readers.ts
import request from './request'
export const getReaders = (params: any) => request.get('/readers', { params })
export const createReader = (data: any) => request.post('/readers', data)
export const updateReaderStatus = (id: number, status: string) => request.put(`/readers/${id}/status`, { status })

// catalog.ts
import request from './request'
export const searchCatalog = (params: any) => request.get('/catalog/books', { params })
export const getCatalogBook = (id: number) => request.get(`/catalog/books/${id}`)
```

- [ ] **Step 2: Commit**
```bash
git add library-web/src/api/
git commit -m "feat(web): api bindings for books, readers, and catalog"
```

## 任务 B9：图书管理视图 (Admin)

**Files:**
- Create: `library-web/src/views/books/BookList.vue`
- Create: `library-web/src/views/books/BookForm.vue`
- Create: `library-web/src/views/books/CopyManage.vue`
- Modify: `library-web/src/router/index.ts` (注册路由)

- [ ] **Step 1: 编写 BookList.vue**
使用 `el-table` 展示图书列表，包含 ISBN, Title, Author, Total Copies, Available Copies。
顶部有搜索框（Keyword）。
操作列有：Edit, Manage Copies, Delete。

- [ ] **Step 2: 编写 BookForm.vue (Dialog 或独立页面)**
使用 `el-form`，字段：ISBN, Title, Publisher, Publish Year, Author(s), Category。

- [ ] **Step 3: 编写 CopyManage.vue (Dialog)**
展示某本书的所有复本（Barcode, Status）。
提供 "Add Copy" 按钮（输入 barcode）。
提供 "Delete/Report Lost" 按钮。

- [ ] **Step 4: Commit**
```bash
git add library-web/src/views/books/ library-web/src/router/index.ts
git commit -m "feat(web): admin book management views"
```

## 任务 B10：读者管理视图 (Admin)

**Files:**
- Create: `library-web/src/views/readers/ReaderList.vue`
- Create: `library-web/src/views/readers/ReaderForm.vue`
- Modify: `library-web/src/router/index.ts`

- [ ] **Step 1: 编写 ReaderList.vue**
使用 `el-table` 展示读者（Reader No, Name, Phone, Email, Status, Register Date）。
操作列：Edit, Enable/Disable。

- [ ] **Step 2: 编写 ReaderForm.vue (Dialog)**
用于创建/编辑读者信息。

- [ ] **Step 3: Commit**
```bash
git add library-web/src/views/readers/ library-web/src/router/index.ts
git commit -m "feat(web): admin reader management views"
```

## 任务 B11：公开目录视图 (Reader)

**Files:**
- Create: `library-web/src/views/catalog/CatalogSearch.vue`
- Create: `library-web/src/views/catalog/CatalogBookDetail.vue`
- Modify: `library-web/src/router/index.ts`

- [ ] **Step 1: 编写 CatalogSearch.vue**
提供一个大搜索框。
搜索结果用卡片列表（`el-row`, `el-col`, `el-card`）展示，显示书名、作者、可用复本数。
点击卡片进入详情。

- [ ] **Step 2: 编写 CatalogBookDetail.vue**
展示图书详细信息和摘要。
列出当前馆藏复本状态（可用/借出）。

- [ ] **Step 3: Commit**
```bash
git add library-web/src/views/catalog/ library-web/src/router/index.ts
git commit -m "feat(web): reader catalog search views"
```

---

## 验证与交接

1. **后端验证**：`mvn verify` 确保领域和接口层测试通过，ArchUnit 无报错。
2. **前端验证**：`npm run build` 确保 TypeScript 编译通过。
3. **冒烟测试**：
   - 管理员登录后，进入图书管理，能添加一本书，并为其添加 2 个复本。
   - 管理员进入读者管理，能添加一个读者，并禁用/启用该读者。
   - 读者登录后，进入 Catalog，能搜索到刚才添加的书，并看到 available copies 为 2。

**Plan complete and saved to `docs/superpowers/plans/2026-06-19-sub-project-b-books-and-readers-*.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration
**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
