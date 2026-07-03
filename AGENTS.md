# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

蓝心快搭 (BlueSnap) 是一个 Android 应用，用户通过自然语言描述需求，AI 自动生成完整的单页 HTML 应用并在 WebView 沙箱中实时预览。2026 中国大学生计算机设计大赛 AIGC 创新赛道参赛作品。

技术栈：Kotlin + Jetpack Compose + Material3 + OkHttp + vivo 蓝心大模型 API (OpenAI 兼容协议)。

## Build & Run

```bash
# 编译检查（最快反馈）
./gradlew :app:compileDebugKotlin

# 完整 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

**前置条件**：在项目根目录创建 `local.properties`（已在 .gitignore 中），包含：

```properties
sdk.dir=/path/to/your/Android/Sdk
ai.api.key=YOUR_API_KEY_HERE
ai.base.url=https://api-ai.vivo.com.cn/v1
ai.model=Doubao-Seed-2.0-pro
```

API 配置通过 `BuildConfig` 注入（见 `app/build.gradle.kts` 中的 `buildConfigField`），运行时使用 `BuildConfig.AI_API_KEY` / `BuildConfig.AI_BASE_URL` / `BuildConfig.AI_MODEL`。

## Testing

```bash
# 单元测试
./gradlew testDebugUnitTest

# Instrumented 测试（需连接设备）
./gradlew connectedDebugAndroidTest
```

测试文件仅有默认模板：
- `app/src/test/.../ExampleUnitTest.kt`
- `app/src/androidTest/.../ExampleInstrumentedTest.kt`

## Architecture

**单 Activity + Compose Navigation** 架构，MVVM 单向数据流。

### 数据流

```
用户输入 -> MainViewModel.sendMessage()
         -> AiService.chatStream() (SSE 流式)
         -> StateFlow<AppState> 更新
         -> Compose UI 自动重组
```

### 核心分层

| 层 | 关键文件 | 职责 |
|---|---|---|
| UI | `ui/screens/*.kt`, `ui/components/*.kt` | 纯 Compose UI，通过回调与 ViewModel 交互 |
| ViewModel | `viewmodel/MainViewModel.kt` | 唯一 ViewModel，管理 `AppState` 状态流 |
| AI 服务 | `ai/AiService.kt` (接口) | 定义 `chat`/`chatStream`/`generatePlan`/`generateHtml` |
| AI 实现 | `ai/RealAiService.kt`, `ai/MockAiService.kt` | 真实 API / 离线 Mock |
| 数据模型 | `data/Models.kt` | `ChatMessage`, `AppPlan`, `GeneratedApp`, `AppState` |

### 导航（非 Jetpack Navigation）

不使用 Navigation Compose 库。导航通过 `AppState.currentScreen` 枚举 + `AnimatedContent` 实现（见 `ui/navigation/AppNavigation.kt`）。切换屏幕调用 `viewModel.navigateTo(Screen.XXX)`。

### AI 服务双实现

- `RealAiService`：OkHttp 直连 vivo 蓝心 API，SSE 流式解析手动实现（逐行读取 `data:` 前缀），非流式调用用于 plan 和 HTML 生成。有两个 OkHttpClient 实例：普通（120s read timeout）和长耗时（300s，用于 HTML 生成）。
- `MockAiService`：内置 5 种预置 HTML 模板（番茄钟/待办清单/备忘录/记账本/习惯打卡），用于离线演示和调试。

`MainViewModel` 中硬编码使用 `RealAiService()`。切换到 Mock 可直接替换为 `MockAiService()`。

### 流式输出机制

`chatStream()` 返回 `Flow<String>`（内部用 `channelFlow` + `trySend`）。ViewModel collect 时更新 `AppState.streamingContent`，ChatScreen 中 `StreamingBubble` 组件实时渲染流式内容。流式完成后内容转为正式 `ChatMessage`。

**已知问题**：`flow {}` 的 `emit()` 在 `withContext(Dispatchers.IO)` 阻塞线程中不安全，已改用 `channelFlow` + `trySend`，但仍有边缘情况可能导致 "flow exception transparency is violated"。

### 方案自动生成触发

`MainViewModel.shouldAutoPlan()` 通过关键词匹配（"方案"/"规划好"/"已经理解"/"为你设计"/"可以生成"）判断 AI 回复是否暗示方案就绪，自动调用 `generatePlan()` 并跳转 PlanScreen。

### WebView 沙箱

`PreviewScreen` 中的 `AppWebView` 以 `https://localhost` 作为 base URL 加载 HTML（确保 localStorage 可用）。注入 `DEBUG_JS_BRIDGE` 脚本捕获 JS 错误到 Logcat。安全设置：禁用文件访问、禁止弹窗、拦截所有 URL 跳转。

### Prompt 模板

三个 system prompt 定义在 `RealAiService.companion` 中：
- `CHAT_SYSTEM_PROMPT`：对话阶段，引导用户明确需求
- `PLAN_SYSTEM_PROMPT`：强制返回 JSON 格式方案
- `HTML_SYSTEM_PROMPT`：生成完整单 HTML 文件，含详细的 JS 交互要求

## Key Patterns & Pitfalls

1. **HTML 模板中的字符串冲突**：`MockAiService.kt` 中内联了大量 HTML 模板作为 Kotlin raw string。如果需要在模板中使用 `${...}`（如 JS 模板字符串），必须用字符串拼接替代，否则会被 Kotlin 字符串模板引擎解析。

2. **API Key 安全**：`ai.api.key` 存储在 `local.properties`（git ignored），通过 Gradle `buildConfigField` 注入到 `BuildConfig`。切勿将 key 硬编码到源码。

3. **JSON 解析容错**：`RealAiService.parsePlan()` 和 `extractHtml()` 使用正则 + 手动 JSONObject 解析，有多层 fallback（代码块提取 -> 裸 JSON 提取 -> 兜底默认值）。修改时注意保持容错链完整。

4. **状态管理**：整个应用状态在单一 `AppState` data class 中，通过 `MutableStateFlow.update {}` 进行不可变更新。添加新状态字段时需在 `AppState` 中定义，并在 `MainViewModel` 中提供对应的更新方法。

5. **Edge-to-edge**：`MainActivity` 启用了 `enableEdgeToEdge()`，ChatScreen 和 PreviewScreen 的输入栏使用 `imePadding()` + `navigationBarsPadding()` 适配。

## File Layout

```
app/src/main/java/com/example/bluesnap/
  MainActivity.kt              -- 唯一 Activity，setContent 入口
  data/Models.kt               -- 全部数据模型（ChatMessage, AppPlan, GeneratedApp, AppState, Screen 枚举）
  ai/AiService.kt              -- AI 服务接口（4 个方法）
  ai/RealAiService.kt          -- 真实 API 实现 + prompt 模板 + AiException
  ai/MockAiService.kt          -- Mock 实现 + 5 套 HTML 模板（~400 行）
  viewmodel/MainViewModel.kt   -- 状态管理 + 业务逻辑
  ui/navigation/AppNavigation.kt  -- AnimatedContent 导航
  ui/screens/HomeScreen.kt     -- 首页：品牌区 + 模板 chips + 输入框
  ui/screens/ChatScreen.kt     -- 对话页：消息列表 + 流式气泡 + 输入栏
  ui/screens/PlanScreen.kt     -- 方案确认：功能开关 + 布局选择
  ui/screens/PreviewScreen.kt  -- WebView 预览 + JS 调试桥 + 反馈输入
  ui/screens/ShareScreen.kt    -- 结果分发页，导出/分享 HTML
  ui/components/MessageBubble.kt   -- 已完成的消息气泡
  ui/components/StreamingBubble.kt -- 流式输出气泡（含动画省略号）
  ui/components/PlanCard.kt        -- 方案详情卡片
  ui/components/TemplateChip.kt    -- 场景模板定义 + defaultTemplates 列表
  ui/theme/Color.kt            -- 品牌色彩定义（BluePrimary 系列）
  ui/theme/Theme.kt            -- Material3 主题（亮色/暗色，动态色默认关闭）
  ui/theme/Type.kt             -- 字体配置

docs/design.md                 -- 完整技术设计文档
docs/optimization.md           -- 已知问题与优化方向
策划案/                        -- 比赛策划案（LaTeX 源文件 + Markdown）
```

## Code Comments Language

代码注释和日志使用中文。Prompt 模板使用中文。
