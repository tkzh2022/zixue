## 任务 A20：前端工程脚手架

**Files:**
- Create: `library-web/package.json`
- Create: `library-web/vite.config.ts`
- Create: `library-web/tsconfig.json` `tsconfig.node.json`
- Create: `library-web/index.html`
- Create: `library-web/src/main.ts` `src/App.vue`
- Create: `library-web/.eslintrc.cjs` `.prettierrc.json` `.gitignore`

依赖：vue 3.4、vue-router 4、pinia、element-plus 2.7、@element-plus/icons-vue、axios、vue-i18n 9、dayjs、nanoid。
devDeps：typescript 5.3、@vitejs/plugin-vue、unplugin-vue-components、unplugin-auto-import、vitest、@vue/test-utils、jsdom、eslint、prettier、eslint-plugin-vue。

vite.config.ts：proxy `/api` → `http://localhost:8080`，autoImport(ElementPlusResolver)。

提交：`git commit -m "chore(web): scaffold vue3 + vite + element plus"`

## 任务 A21：i18n 框架

**Files:**
- Create: `library-web/src/i18n/index.ts`
- Create: `library-web/src/i18n/locales/zh/common.json menu.json error.json`
- Create: `library-web/src/i18n/locales/en/common.json menu.json error.json`

error.json 完整覆盖 spec 7.5 错误码（zh+en），common.json 含按钮、表单常用文案。
默认语言 zh，localStorage key=`library:lang`。

提交：`git commit -m "feat(web): i18n setup with zh/en locales"`

## 任务 A22：axios 封装 + 自动刷新

**Files:**
- Create: `library-web/src/api/request.ts`
- Create: `library-web/src/api/auth.ts`
- Create: `library-web/src/utils/auth.ts` (token 存取 in-memory + 持久化)
- Create: `library-web/src/utils/trace.ts` (nanoid 生成 traceId)
- Test: `library-web/tests/unit/api/request.spec.ts`

request.ts 关键逻辑：
- baseURL `/api/v1`
- 请求拦截：注入 Authorization、X-Trace-Id、Accept-Language
- 响应拦截：根据 code 分支（见 spec 8.3）
- 8402 触发 refresh：用单 promise 锁防并发；失败清 token + 跳登录
- 8401/8403 直接清 token + 跳登录

测试：mock axios.create + adapter，断言 401 触发 refresh 并重发原请求。

提交：`git commit -m "feat(web): axios with auto refresh and i18n error toast"`

## 任务 A23：Pinia user store

**Files:**
- Create: `library-web/src/stores/user.ts`
- Create: `library-web/src/stores/app.ts`
- Test: `library-web/tests/unit/stores/user.spec.ts`

user store 状态：accessToken、refreshToken、profile、role；
actions：login、register、refresh、logout、fetchMe、reset。
持久化：accessToken 不持久化（仅内存），refreshToken 写 localStorage。

提交：`git commit -m "feat(web): pinia user store with token lifecycle"`

## 任务 A24：路由与守卫

**Files:**
- Create: `library-web/src/router/index.ts`
- Create: `library-web/src/router/guards.ts`
- Create: `library-web/src/layouts/AdminLayout.vue ReaderLayout.vue BlankLayout.vue`
- Create: `library-web/src/views/error/NotFound.vue Forbidden.vue`

路由表：
- `/login`、`/register` → BlankLayout
- `/` → 根据 role 跳 `/dashboard` 或 `/catalog`
- `/dashboard` → AdminLayout，meta.roles=['LIBRARIAN']（占位 view）
- `/catalog` → ReaderLayout，meta.roles=['READER']（占位 view）
- `/403` `/404` 错误页

guards：beforeEach 检查 requiresAuth（无 token → /login）→ 角色匹配（不匹配 → /403）。

提交：`git commit -m "feat(web): router with role-based guards and layouts"`

## 任务 A25：登录页 + 注册页

**Files:**
- Create: `library-web/src/views/auth/Login.vue`
- Create: `library-web/src/views/auth/Register.vue`
- Test: `library-web/tests/unit/views/Login.spec.ts`

Login.vue：el-form + 用户名/密码 + 记住我（暂仅前端记忆）；submit → user.login → 成功 router.push 根。
Register.vue：用户名（4-20）、密码（>=8 含字母数字）、姓名、邮箱、手机；submit → user.register → 跳登录页 + Toast。

测试：组件层 mock store，断言 submit 触发 action 与跳转。

提交：`git commit -m "feat(web): login and register pages"`

## 任务 A26：占位 dashboard、catalog、布局壳

**Files:**
- Create: `library-web/src/views/dashboard/Dashboard.vue`
- Create: `library-web/src/views/catalog/CatalogHome.vue`
- Modify: AdminLayout.vue / ReaderLayout.vue 加顶栏（用户名、登出按钮、语言切换）

让登录后能看到一个空白但可见的页面，证明守卫 + 角色 + 登出闭环跑通。

提交：`git commit -m "feat(web): placeholder layouts and home pages"`

## 任务 A27：LangSwitch + ErrorBoundary 组件

**Files:**
- Create: `library-web/src/components/global/LangSwitch.vue`
- Create: `library-web/src/components/global/ErrorBoundary.vue`
- Modify: `App.vue` 包裹 ErrorBoundary

提交：`git commit -m "feat(web): language switcher and error boundary"`

## 任务 A28：端到端冒烟脚本（人工验证清单）

**Files:**
- Create: `docs/runbooks/sub-project-a-smoke-test.md`

清单：
1. `cd library-api && mvn -DskipTests spring-boot:run`（dev profile）
2. `cd library-web && pnpm i && pnpm dev`
3. 浏览器打开 http://localhost:5173，看到登录页（中文）
4. 切换语言 → 文案变英文
5. 注册 reader1 → 跳登录 → 用 reader1 登录 → 跳到 /catalog 占位页
6. 登出 → 回登录页；用 librarian/librarian123 登录 → 跳 /dashboard
7. 访问 /403 模拟越权 → 显示禁止页
8. F12 看 Network：每个请求有 X-Trace-Id 响应头
9. 把 access TTL 改为 30 秒，等过期后操作 → 看到自动 refresh 一次然后继续可用
10. 删 refresh 后再操作 → 跳登录页
11. `mvn -pl library-api verify`：所有测试通过；JaCoCo 覆盖率 domain ≥ 80%、application ≥ 70%

提交：`git commit -m "docs: smoke test runbook for sub-project A"`

---

## Self-Review（执行检查）

- [x] 每个任务包含 Files / 关键代码片段 / 提交命令
- [x] 错误码全部在 spec 7.5 定义并在前端 i18n 覆盖
- [x] JWT 双 token 流程（签发、校验、刷新轮换、重放检测、登出）有对应任务（A7、A10、A14）
- [x] 限流（A17）有测试（限流计数器测试）
- [x] ArchUnit 守护分层（A2 + A18）
- [x] TraceId 贯穿后端 MDC + 前端 nanoid（A15 + A22）
- [x] 前端登录闭环 + 自动刷新有组件 + store + 拦截器三层覆盖
- [x] 冒烟测试清单（A28）覆盖所有关键路径

## 执行交接

Plan 已完成并保存到：
- `docs/superpowers/plans/2026-06-18-sub-project-a-foundation-and-auth-00-overview.md`
- `docs/superpowers/plans/2026-06-18-sub-project-a-foundation-and-auth-01-backend-foundation.md`
- `docs/superpowers/plans/2026-06-18-sub-project-a-foundation-and-auth-02-backend-auth.md`
- `docs/superpowers/plans/2026-06-18-sub-project-a-foundation-and-auth-03-backend-security.md`
- `docs/superpowers/plans/2026-06-18-sub-project-a-foundation-and-auth-04-frontend.md`

执行选项：
1. **Subagent-Driven（推荐）** - 每个任务起一个新的子代理实现 + 复核
2. **Inline 执行** - 在当前会话里逐任务实现，过程中可在每 3-4 个任务后做一次检查点

注意：根据你的项目规则，子代理必须使用与上层一致的模型，不能用 Composer 2.5 Fast。
