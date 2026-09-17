# 摄影文案 AI Agent

> 一个基于 Spring AI 的摄影作品文案创作助手，支持流式对话、工具调用与多轮记忆。

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-6DB33F?logo=spring&logoColor=white)
![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0.2-FF6A00)
![DashScope](https://img.shields.io/badge/Model-qwen--plus-8B5CF6)

---

## 简介

该项目是一个面向摄影师的文案创作 Agent。它会像一位资深摄影文案专家那样，围绕**人像、风光、纪实**三类题材逐步追问拍摄细节，再结合发布平台（小红书 / 朋友圈 / 公众号 / 图虫）与语气偏好，输出专属文案。

底层由阿里云百炼（DashScope）的 `qwen-plus` 驱动，通过 Spring AI 的 `ChatClient` 构建，具备**流式输出**、**工具调用**和**文件持久化的多轮记忆**三项核心能力。

## 功能特性

- **流式对话** — 基于 SSE 逐字返回，前端可实时渲染打字机效果
- **工具调用（Function Calling）** — 内置 7 个工具，模型可按需自主调用（见下表）
- **多轮对话记忆** — 基于 Kryo 序列化落地为本地文件，重启后上下文不丢失
- **可插拔 Advisor** — 日志埋点与「重读增强」两个 Advisor 可按需启用
- **在线接口文档** — 集成 Knife4j / springdoc-openapi
- **向量存储预留** — 已接入 PgVector，默认关闭，按需开启（见配置说明）

### 内置工具

| 工具 | 方法 | 说明 |
|---|---|---|
| `FileOperationTool` | `readFile` / `writeFile` | 读取、写入本地文件 |
| `WebSearchTool` | `searchWeb` | 调用 SearchAPI 检索百度结果（取前 5 条） |
| `WebScrapingTool` | `scrapeWebPage` | 抓取指定 URL 的网页正文 |
| `ResourceDownloadTool` | `downloadResource` | 下载网络资源到本地 |
| `TerminalOperationTool` | `executeTerminalCommand` | 执行终端命令（Windows `cmd.exe`） |
| `PDFGenerationTool` | `generatePDF` | 用 iText 生成 PDF，内置中文字体 |
| `TerminateTool` | `doTerminate` | 主动结束当前对话 |

## 技术栈

| 分类 | 选型 |
|---|---|
| 语言 / 构建 | Java 21、Maven |
| 框架 | Spring Boot 3.4.4、Spring Web（WebFlux `Flux` 流式） |
| AI 框架 | Spring AI 1.0.0、Spring AI Alibaba 1.0.0.2 |
| 大模型 | 阿里云百炼 DashScope `qwen-plus` / `qwen3.7-text-embedding` |
| 向量存储 | PgVector（PostgreSQL，默认关闭） |
| 对话记忆 | Kryo 5.6.2（文件序列化） |
| 工具库 | Hutool、Jsoup、iText 9.1.0 |
| 接口文档 | Knife4j 4.4.0 |
| 其他 | Lombok、MCP Client Starter、Ollama Starter |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- 阿里云百炼 API Key（[获取地址](https://bailian.console.aliyun.com/)）
- SearchAPI Key（[searchapi.io](https://www.searchapi.io/)，使用联网搜索工具时需要）

### 1. 克隆仓库

```bash
git clone https://github.com/Navigator2333/suxin-photo-ai-agent.git
cd suxin-photo-ai-agent
```

### 2. 创建本地配置文件（必做）

项目默认激活 `local` profile（见 `application.yaml` 的 `spring.profiles.active`），而 `application-local.yaml` **已被 `.gitignore` 排除**，因此克隆后需要手动创建：

`src/main/resources/application-local.yaml`

```yaml
spring:
  ai:
    dashscope:
      # 必填：阿里云百炼 API Key，缺失会导致启动失败
      api-key: sk-你的百炼APIKey
      chat:
        options:
          model: qwen-plus
      embedding:
        options:
          model: qwen3.7-text-embedding

# 必填：WebSearchTool 依赖该配置，缺失会导致启动失败
search-api:
  api-key: 你的SearchAPIKey

logging:
  level:
    org.springframework.ai: DEBUG   # 可选：查看 Spring AI 调用细节
```

> ⚠️ **`search-api.api-key` 没有默认值**，`ToolRegistration` 通过 `@Value("${search-api.api-key}")` 注入。若该配置缺失，应用启动时会因占位符无法解析而报错。

### 3. 启动

```bash
# 方式一：Maven
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/suxin-photo-ai-agent-0.0.1-SNAPSHOT.jar
```

启动后服务监听 `8123` 端口，根路径为 `/api`。

## 接口说明

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/ai/photo_app/chat/server_sent_event` | GET | SSE 流式对话，参数 `message`、`chatId` |
| `/api/health` | GET | 健康检查，返回 `ok` |
| `/api/doc.html` | GET | Knife4j 接口文档 |
| `/api/swagger-ui.html` | GET | Swagger UI |

### 调用示例

```bash
# 健康检查
curl http://localhost:8123/api/health

# 流式对话（-N 关闭缓冲，才能看到逐字输出）
curl -N "http://localhost:8123/api/ai/photo_app/chat/server_sent_event?message=你好，我拍了一张海边日落的照片，帮我写一段小红书文案&chatId=demo-001"
```

> `chatId` 是会话唯一标识，相同 `chatId` 的多次请求会共享上下文记忆；不传则每次都是新会话。

## 项目结构

```
src/main/java/com/suxin/
├── SuxinAiAgentApplication.java   # 启动类
├── app/
│   └── PhotoApp.java              # 核心：ChatClient 装配、系统提示词、工具挂载
├── controller/
│   ├── AiController.java          # SSE 流式对话接口
│   └── HealthController.java      # 健康检查
├── advisor/
│   ├── MyLoggerAdvisor.java       # 日志 Advisor
│   └── ReReadingAdvisor.java      # 重读增强 Advisor
├── chatmemory/
│   └── FileBasedChatMemory.java   # 基于 Kryo 的文件化对话记忆
├── tools/                         # 7 个可调用工具
│   ├── ToolRegistration.java      # 工具集中注册
│   ├── FileOperationTool.java
│   ├── WebSearchTool.java
│   ├── WebScrapingTool.java
│   ├── ResourceDownloadTool.java
│   ├── TerminalOperationTool.java
│   ├── PDFGenerationTool.java
│   └── TerminateTool.java
└── constant/
    └── FileConstant.java          # 文件保存根目录（${user.dir}/tmp）
```

运行时产物统一落在项目根目录的 `tmp/` 下（`tmp/chat-memory/`、`tmp/pdf/` 等），该目录已被 `.gitignore` 排除。

## 配置说明

### 关于向量存储（PgVector）

启动类中显式排除了数据源自动配置：

```java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
```

因此**默认不会连接数据库，PgVector 相关能力处于关闭状态**。如需启用，需：

1. 移除启动类中的 `DataSourceAutoConfiguration.class` 排除项
2. 在本地配置中补全 `spring.datasource` 连接信息
3. 准备一个启用了 `vector` 扩展的 PostgreSQL 实例

`application-dev.yaml` 中已给出 PgVector 的参数模板（HNSW 索引、余弦距离），可作为参考。

### 配置文件分工

| 文件 | 是否入库 | 用途 |
|---|---|---|
| `application.yaml` | ✅ | 主配置：端口、context-path、Knife4j、激活 profile |
| `application-dev.yaml` | ✅ | 模板：仅保留结构，敏感值一律留空 |
| `application-local.yaml` | ❌ 已忽略 | 本地真实密钥，**请勿提交** |

## 安全须知

- **本项目的工具集赋予了模型执行终端命令、读写本地文件、下载远程资源的能力**，`TerminalOperationTool` 更是直接调用 `cmd.exe` 执行任意命令。请仅在受信任的环境中使用，切勿将服务直接暴露到公网，也不要让不可信输入触达该接口。
- 所有密钥请一律写入 `application-local.yaml`，该文件已在 `.gitignore` 中排除。提交前建议确认：
  ```bash
  git diff --cached | grep -iE "password|api-key|sk-"
  ```

## 测试

```bash
mvn test
```

`PhotoAppTest` 会发起一次真实的流式对话请求，**需要有效的 DashScope API Key 并且会消耗额度**，在无密钥的 CI 环境中请跳过：

```bash
mvn test -Dtest='!PhotoAppTest'
```

## Roadmap

- [ ] 引入 RAG：基于 PgVector 沉淀优秀摄影文案语料
- [ ] 补充 `application-local.yaml.example` 模板，降低上手成本
- [ ] 增加不依赖外部 API 的单元测试

## License

本项目尚未添加开源许可证。如需开源分发，建议补充 `LICENSE` 文件（如 [MIT](https://choosealicense.com/licenses/mit/)）。
