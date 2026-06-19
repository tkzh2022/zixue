# Sub-project A 冒烟测试清单

## 前置准备
1. 确保 MySQL 8 运行中，创建库 `library`，用户名密码 `library/library`
2. 或者修改 `library-api/src/main/resources/application.yml` 里的数据库连接
3. 确保 JDK 21 已安装并在 PATH 中

## 后端启动
```bash
cd library-api
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- 观察日志：Flyway 迁移成功，DataInitializer 插入了 librarian 和 reader1 账号

## 前端启动
```bash
cd library-web
npm install
npm run dev
```

## 测试步骤
1. [ ] 浏览器打开 `http://localhost:5173`，自动跳转到 `/login`
2. [ ] 界面显示中文，点击右上角（如果有）切换语言，文案应变化
3. [ ] 点击“注册”，填写信息（用户名 `testuser`，密码 `password123`），提交后提示成功并跳回登录
4. [ ] 使用 `testuser` / `password123` 登录，应跳转到 `/catalog`（Reader 角色），看到占位页面
5. [ ] 刷新页面，保持登录状态不掉
6. [ ] 点击“退出登录”，跳回 `/login`
7. [ ] 使用 `librarian` / `librarian123` 登录，应跳转到 `/dashboard`（Librarian 角色）
8. [ ] 手动在地址栏输入 `http://localhost:5173/catalog`，应被拦截或显示 403（如果做了严格区分）
9. [ ] F12 Network 查看请求头，确保带有 `Authorization: Bearer ...` 和 `X-Trace-Id`
10. [ ] 后端测试：`mvn clean verify`，确保所有测试通过，ArchUnit 无报错
