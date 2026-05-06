# BlueSnap - 后续优化指南

本文档记录已知问题、优化方向和技术债，供后续开发参考。

---

## 一、已知问题

### 1.1 流式输出偶发异常

**现象**：偶尔出现 "flow exception transparency is violated" 错误，或流式内容一次性输出。

**根因**：`flow {}` 的 `emit()` 不能在 `withContext(Dispatchers.IO)` 的阻塞线程中安全调用。已改用 `channelFlow` + `trySend`，但仍有边缘情况。

**建议优化**：
- 考虑使用 OkHttp 的 `EventSource` (SSE 库：`okhttp-sse`) 替代手动解析
- 或实现真正的流式 SSE 客户端，使用 `BufferedSource` 逐行非阻塞读取
- 在 ViewModel 层添加重试逻辑（当前失败后直接显示错误）

### 1.2 HTML 生成质量

**现象**：AI 生成的 HTML 应用交互性不稳定，部分按钮可能无响应。

**根因**：大模型生成的 JavaScript 代码质量不可控，可能缺少事件绑定或存在运行时错误。

**建议优化**：
- 改进 Prompt：在 `HTML_SYSTEM_PROMPT` 中提供更多交互模式的示例代码
- 添加 HTML 后处理：用正则检查 `onclick` 绑定是否存在于对应函数
- WebView 注入 polyfill：在加载 HTML 后自动补全缺失的事件监听
- 支持流式 HTML 生成：边生成边预览，用户可提前发现问题

### 1.3 输入法适配

**现象**：部分设备上输入框与键盘的间距不理想。

**根因**：`enableEdgeToEdge()` + `imePadding()` 在不同 Android 版本/厂商 ROM 上行为不一致。

**建议优化**：
- 使用 `WindowInsetsAnimationCompat` 实现平滑的键盘跟随动画
- 在 `MainActivity` 中根据设备信息动态调整 `windowSoftInputMode`
- 测试更多厂商 ROM（MIUI、ColorOS、OriginOS 等）

---

## 二、功能优化方向

### 2.1 对话体验

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| 多轮上下文压缩 | 当对话过长时，自动摘要历史消息以减少 token 消耗 | 高 |
| 打断/取消生成 | 流式输出中支持用户点击"停止"中断生成 | 高 |
| 消息持久化 | 使用 Room/SharedPreferences 保存对话历史 | 中 |
| 语音输入 | 集成语音识别，支持语音描述需求 | 低 |

### 2.2 方案生成

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| 方案对比 | 一次生成多个方案供用户选择 | 中 |
| 功能依赖图 | 显示功能之间的依赖关系 | 低 |
| 布局预览缩略图 | 在 PlanScreen 中展示布局效果预览 | 中 |

### 2.3 HTML 生成

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| 流式 HTML 预览 | 边生成边渲染到 WebView，实时查看效果 | 高 |
| HTML 代码编辑 | 允许用户查看和微调生成的 HTML 代码 | 中 |
| 导出功能 | 将 HTML 文件保存到本地或分享 | 中 |
| 模板缓存 | 缓存已生成的 HTML，避免重复 API 调用 | 中 |
| 多页面支持 | 生成多页面应用（SPA 路由） | 低 |

### 2.4 WebView 增强

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| 错误捕获面板 | 在 WebView 上叠加显示 JS 错误信息 | 高 |
| 性能监控 | 注入 performance API 监控页面加载时间 | 低 |
| 截图分享 | 对 WebView 内容截图并生成分享图 | 中 |

---

## 三、架构优化

### 3.1 依赖注入

**现状**：`MainViewModel` 直接实例化 `RealAiService()`，紧耦合。

**建议**：
```kotlin
// 引入 Hilt 或手动 DI
@Module
object AppModule {
    @Provides
    fun provideAiService(): AiService = RealAiService()
    
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
}
```

### 3.2 错误处理

**现状**：错误直接显示为 AI 消息文本，无重试机制。

**建议**：
```kotlin
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Streaming(val content: String) : UiState()
    data class Success(val message: ChatMessage) : UiState()
    data class Error(val message: String, val retryable: Boolean) : UiState()
}
```

### 3.3 网络层重构

**现状**：`RealAiService` 中 SSE 解析逻辑与业务逻辑混合。

**建议拆分**：
```
RealAiService
  ├── SseClient          # SSE 连接管理
  ├── ChatRepository     # 对话业务逻辑
  ├── PlanRepository     # 方案生成逻辑
  └── HtmlRepository     # HTML 生成逻辑
```

### 3.4 测试覆盖

**现状**：无测试代码。

**建议优先添加**：
- `MainViewModel` 的单元测试（状态流转、错误处理）
- `RealAiService` 的单元测试（JSON 解析、SSE 解析）
- PlanScreen / ChatScreen 的 UI 测试

---

## 四、性能优化

### 4.1 减少 API 延迟

| 手段 | 说明 |
|------|------|
| 切换更快模型 | 使用 `Doubao-Seed-2.0-mini` 替代 pro |
| Prompt 精简 | 缩短 system prompt 长度，减少 token 消耗 |
| 结果缓存 | 对相同需求缓存生成结果 |
| 并行请求 | 方案生成和对话可部分并行 |

### 4.2 内存优化

| 手段 | 说明 |
|------|------|
| 消息分页 | LazyColumn 使用分页加载，避免长对话 OOM |
| HTML 压缩 | 生成的 HTML 使用 minify 处理 |
| WebView 内存 | 离开预览页时释放 WebView 资源 |

### 4.3 电量优化

| 手段 | 说明 |
|------|------|
| 网络请求合并 | 避免频繁小请求 |
| 后台限制 | 应用退到后台时停止 SSE 连接 |

---

## 五、UI/UX 优化

### 5.1 动画增强

- 页面转场：Shared Element Transition（对话消息 → 方案卡片）
- 流式输出：逐字打印动画，而非整块更新
- 加载状态：Skeleton Screen 替代简单 Loading 指示器

### 5.2 暗色模式

当前仅支持亮色模式，建议添加：
- `darkColorScheme()` 配置
- 跟随系统设置或手动切换
- WebView HTML 注入暗色 CSS 变量

### 5.3 无障碍

- 添加 `contentDescription` 到所有交互元素
- 支持 TalkBack 朗读
- 键盘导航支持

---

## 六、赛事冲刺建议

### 决赛前优先完成

1. **流式 HTML 预览**（视觉冲击力最大）
2. **JS 错误捕获面板**（提升生成质量感知）
3. **消息持久化**（重启不丢失对话）
4. **导出/分享功能**（演示时可展示分享效果）
5. **暗色模式**（提升完成度感）

### 演示优化

- 准备 2-3 个高质量演示用例（番茄钟、记账本）
- 提前缓存生成结果，避免演示时等待 API
- 准备离线 Mock 模式作为网络异常的降级方案
