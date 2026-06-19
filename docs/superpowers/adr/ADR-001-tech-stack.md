# ADR-001: 技术栈版本（JDK 21 + Spring Boot 3.2）

- 日期：2026-06-18
- 状态：已采纳
- 作用范围：整个项目（spec、plan、所有 Sub-project）

## 背景
本机检测到 JDK 1.8 与 JDK 21 共存。spec 选择 Java 17 + Spring Boot 3.2，与 JDK 21 完全兼容（21 向下兼容 17 字节码）。

## 决策
- 编译目标：Java 17（保证 17/21 都能跑）
- 运行环境：JDK 21（实际安装的版本）
- Spring Boot：3.2.5
- 命名空间：jakarta.\*（servlet/persistence/validation）
- jjwt：0.12.5
- bucket4j：8.10.1
- ArchUnit：1.3.0

## Maven 调用
所有 mvn 调用必须显式指定 JDK 21：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
mvn -v
```

或在每条命令前加 `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home mvn ...`。

## 后续
- 用户可在 ~/.zshrc 永久导出 JAVA_HOME
- 项目附带 `scripts/dev-env.sh` 用于设置环境变量
