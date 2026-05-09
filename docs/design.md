# 蓝心快搭 - 应用技术设计文档

## 一、整体架构

### 1.1 架构概览

采用 MVVM + 单向数据流架构，遵循单一职责原则，各层职责清晰分离：

```
┌─────────────────────────────────────────────────────────┐
│ UI Layer (Jetpack Compose)                              │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────┐     │
│  │HomeScreen│ │ChatScreen│ │PlanScreen│ │PreviewScreen│  │
│  └─────────┘ └─────────┘ └─────────┘ └──────────┘     │
├─────────────────────────────────────────────────────────┤
│ ViewModel Layer                                         │
│  ┌──────────────────────────────────────────────┐      │
│  │ MainViewModel (StateFlow + UiState)           │      │
│  └──────────────────────────────────────────────┘      │
├─────────────────────────────────────────────────────────┤
│ Domain / Service Layer                                  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │ AiService     │ │ AppStorage   │ │ HtmlGenerator│   │
│  │ (Interface)   │ │ (Room DB)    │ │ (Engine)     │   │
│  └──────────────┘ └──────────────┘ └──────────────┘   │
├─────────────────────────────────────────────────────────┤
│ Data Layer                                              │
│  ┌──────────────┐ ┌──────────────┐                     │
│  │ Retrofit/     │ │ Room         │                     │
│  │ OkHttp        │ │ Database     │                     │
│  └──────────────┘ └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
```

### 1.2 核心设计原则

| 原则 | 应用方式 |
|------|---------|
| SRP（单一职责） | ViewModel只管状态，Service只管AI交互，Screen只管UI渲染 |
| OCP（开闭原则） | AiService为接口，可替换Mock/Real实现，无需修改上层代码 |
| DIP（依赖倒置） | ViewModel依赖AiService接口而非具体实现 |
| KISS | 初赛版本保持单Activity + Compose Navigation，不引入复杂框架 |
| YAGNI | 不预先实现APK打包、多页面等远期功能 |

### 1.3 包结构

```
com.example.bluesnap/
├── MainActivity.kt              # 入口Activity
├── BlueSnapApp.kt             # Application类（预留）
├── data/
│   └── Models.kt                # 数据模型：Message, Plan, GeneratedApp, AppState
├── ai/
│   ├── AiService.kt             # AI服务接口
│   └── MockAiService.kt         # Mock实现（Demo用）
├── viewmodel/
│   └── MainViewModel.kt         # 主ViewModel，管理全局状态
└── ui/
    ├── theme/
    │   ├── Color.kt              # 蓝心快搭品牌色彩
    │   ├── Theme.kt              # Material3主题配置
    │   └── Type.kt               # 字体配置
    ├── navigation/
    │   └── AppNavigation.kt      # 导航图定义
    ├── components/
    │   ├── MessageBubble.kt      # 聊天气泡组件
    │   ├── PlanCard.kt           # 方案确认卡片
    │   └── TemplateChip.kt       # 场景模板标签
    └── screens/
        ├── HomeScreen.kt         # 首页：欢迎 + 场景模板 + 快速输入
        ├── ChatScreen.kt         # 对话页：多轮对话交互
        ├── PlanScreen.kt         # 方案确认页：功能清单 + 布局选择
        └── PreviewScreen.kt      # 预览页：WebView运行生成应用
```

---

## 二、数据模型设计

### 2.1 核心数据模型

```kotlin
// 用户/AI消息
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,                    // USER / ASSISTANT
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
enum class Role { USER, ASSISTANT }

// AI生成的应用方案
data class AppPlan(
    val name: String,                  // 应用名称
    val description: String,           // 一句话描述
    val features: List<Feature>,       // 功能清单
    val layoutIndex: Int = 0,          // 当前选中的布局方案
    val layouts: List<String> = listOf("方案A", "方案B", "方案C")
)
data class Feature(
    val name: String,
    val description: String,
    val enabled: Boolean = true        // 用户可开关
)

// 生成的HTML应用
data class GeneratedApp(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val htmlContent: String,           // 完整的单HTML代码
    val createdAt: Long = System.currentTimeMillis()
)

// 全局UI状态
data class AppState(
    val messages: List<ChatMessage> = emptyList(),
    val currentPlan: AppPlan? = null,
    val isGenerating: Boolean = false,
    val generatedApp: GeneratedApp? = null,
    val savedApps: List<GeneratedApp> = emptyList(),
    val currentScreen: Screen = Screen.HOME
)
enum class Screen { HOME, CHAT, PLAN, PREVIEW }
```

### 2.2 数据流设计

```
用户输入 → ViewModel.sendMessage()
         → AiService解析需求 → 返回ChatMessage
         → ViewModel更新AppState.messages
         → 用户确认方案 → ViewModel.generateApp()
         → AiService生成HTML → 返回GeneratedApp
         → ViewModel更新AppState.generatedApp
         → 跳转PreviewScreen → WebView加载htmlContent
```

---

## 三、AI服务接口设计

### 3.1 接口定义

```kotlin
interface AiService {
    // 解析用户需求，返回AI回复消息
    suspend fun chat(messages: List<ChatMessage>): ChatMessage

    // 根据对话历史生成应用方案
    suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan

    // 根据确认的方案生成HTML代码
    suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String
}
```

### 3.2 Mock实现策略

初赛Demo使用Mock实现，在本地预置高质量HTML模板：

| 用户输入关键词 | 匹配的应用 | 预置HTML特性 |
|---------------|-----------|-------------|
| 番茄钟/计时器/pomodoro | 番茄钟 | 25分钟倒计时、白噪音、专注统计 |
| 待办/清单/todo | 待办清单 | 增删改查、完成勾选、本地存储 |
| 备忘录/记事/笔记 | 备忘录 | Markdown编辑、本地存储、搜索 |
| 记账/账本/消费 | 记账本 | 收支录入、分类统计、图表展示 |
| 打卡/习惯/habit | 习惯打卡 | 多习惯管理、连续打卡天数、日历视图 |
| 其他 | 通用工具 | 基础交互框架，提示用户修改 |

### 3.3 真实API接入路径

后续接入蓝心大模型API的接口设计：

```kotlin
class RealAiService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.vivo.com.cn/blueLM/v1"
) : AiService {
    // POST /chat/completions
    // 请求体: { model: "blueLM-chat", messages: [...] }
    // 响应体: { choices: [{ message: { content: "..." } }] }
    // 使用流式输出(Streaming)提升用户体验
}
```

---

## 四、ViewModel设计

### 4.1 状态管理

```kotlin
class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val aiService: AiService = MockAiService()

    // 发送用户消息并获取AI回复
    fun sendMessage(content: String)

    // 确认方案并生成应用
    fun confirmPlan()

    // 切换功能项开关
    fun toggleFeature(index: Int)

    // 迭代修改（基于反馈重新生成）
    fun iterateWithFeedback(feedback: String)

    // 保存/删除应用
    fun saveApp(app: GeneratedApp)
    fun deleteApp(appId: String)

    // 导航
    fun navigateTo(screen: Screen)
}
```

### 4.2 关键流程时序

```
sendMessage("做一个番茄钟"):
  1. 添加UserMessage到messages
  2. 设置isGenerating=true
  3. 调用aiService.chat()获取AI回复
  4. 添加AiMessage到messages
  5. 检测是否含方案关键词 → 自动调用generatePlan()
  6. 设置currentPlan, isGenerating=false
  7. 自动导航到PLAN页

confirmPlan():
  1. 设置isGenerating=true
  2. 调用aiService.generateHtml(plan)
  3. 创建GeneratedApp对象
  4. 设置generatedApp, isGenerating=false
  5. 导航到PREVIEW页
```

---

## 五、UI/UX设计

### 5.1 设计系统

**色彩方案（蓝心品牌色）**：

| 用途 | 色值 | 说明 |
|------|------|------|
| Primary | #1A73E8 | vivo蓝，主色调 |
| Primary Variant | #0D47A1 | 深蓝，渐变用 |
| Secondary | #00BCD4 | 青色，辅助色 |
| Surface | #F8F9FA | 浅灰白，卡片背景 |
| Background | #FFFFFF | 白色 |
| UserBubble | #E3F2FD | 浅蓝，用户消息气泡 |
| AiBubble | #F5F5F5 | 浅灰，AI消息气泡 |

**组件规范**：
- 圆角：卡片 16dp，按钮 12dp，气泡 20dp
- 间距：屏幕边距 16dp，组件间距 12dp，气泡内边距 12dp
- 字体：标题 20sp Bold，正文 15sp Regular，辅助 12sp

### 5.2 四页面设计

**HomeScreen**：
- 顶部：蓝心快搭 logo + 标语
- 中部：6个场景模板Chip（横排两行，可横向滚动）
- 底部：大输入框 + 发送按钮 + 语音按钮(图标)
- 点击模板：自动填充输入框并跳转ChatScreen

**ChatScreen**：
- 顶部：返回按钮 + "蓝心快搭" 标题
- 主体：LazyColumn消息列表，用户蓝色气泡(右) / AI灰色气泡(左)
- AI含方案时：气泡内嵌"查看方案"按钮
- 底部：输入框 + 发送按钮
- 生成中：底部显示loading动画 + "正在构思中..."

**PlanScreen**：
- 顶部：返回 + "方案确认"
- 功能清单卡片：Checkbox列表，每个功能可开关
- 布局选择：3个缩略图方案，单选Radio
- 交互流程：简洁文字描述
- 底部按钮："确认生成"（主） + "调整需求"（次）

**PreviewScreen**：
- 顶部工具栏：返回 + 应用名 + 分享/导出按钮
- 主体：全屏WebView（占满剩余空间）
- 底部：反馈输入框 + "修改"按钮
- WebView设置：启用JavaScript、DOM Storage

### 5.3 导航架构

```
NavHost(startDestination = "home") {
    composable("home")      → HomeScreen
    composable("chat")      → ChatScreen
    composable("plan")      → PlanScreen
    composable("preview")   → PreviewScreen
}
```

---

## 六、WebView沙箱设计

### 6.1 安全配置

```kotlin
webView.settings.apply {
    javaScriptEnabled = true           // 允许JS执行
    domStorageEnabled = true           // 允许localStorage
    allowFileAccess = false            // 禁止访问文件系统
    allowContentAccess = false         // 禁止访问ContentProvider
    javaScriptCanOpenWindowsAutomatically = false  // 禁止弹窗
    setSupportZoom(false)              // 禁止缩放
}
webView.webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(...) = true  // 拦截所有跳转
}
```

### 6.2 HTML模板结构

生成的HTML应用遵循统一模板结构：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, 
          maximum-scale=1.0, user-scalable=no">
    <title>{应用名}</title>
    <style>
        /* 移动端重置样式 */
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family: -apple-system, sans-serif; 
               max-width: 100vw; overflow-x: hidden; }
        /* 应用特定样式 */
    </style>
</head>
<body>
    <!-- 应用HTML结构 -->
    <script>
        // 应用JS逻辑
        // 使用localStorage持久化数据
    </script>
</body>
</html>
```

---

## 七、技术依赖

### 7.1 当前依赖（已有）

| 依赖 | 版本 | 用途 |
|------|------|------|
| Jetpack Compose BOM | 2026.02.01 | UI框架 |
| Material3 | (BOM管理) | 设计系统 |
| Activity Compose | 1.8.0 | Compose Activity集成 |
| Lifecycle Runtime KTX | 2.6.1 | 生命周期管理 |
| Core KTX | 1.10.1 | Kotlin扩展 |

### 7.2 需新增依赖

| 依赖 | 用途 |
|------|------|
| navigation-compose | 页面导航 |
| webkit (androidx.webkit) | WebView增强 |
| kotlinx-serialization | JSON序列化 |
| room (远期) | 本地数据库 |
| okhttp3 + retrofit (远期) | 真实API调用 |

---

## 八、Demo演示计划

### 8.1 演示流程

1. **启动**：展示蓝心快搭首页，标语"将创作的自由还给每个人"
2. **输入需求**：点击"番茄钟"模板，或手动输入"做一个番茄钟应用"
3. **AI对话**：展示AI回复，含功能方案描述
4. **方案确认**：跳转方案页，展示功能清单和布局选择
5. **生成应用**：点击"确认生成"，展示生成过程动画
6. **预览运行**：WebView内运行生成的番茄钟，演示倒计时功能
7. **迭代修改**：输入反馈"把背景改成蓝色"，展示增量更新

### 8.2 适配目标

- 最低SDK：33（Android 13）
- 目标SDK：36
- 测试机型：vivo手机（推荐）
- 屏幕适配：6.5-6.8英寸主流机型
