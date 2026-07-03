# 蓝心快搭鲁棒性审查与改进清单

评估日期：2026-07-02  
目标：保证复赛演示可控、提交包安全、功能边界清楚。

## 1. 当前鲁棒性结论

| 场景 | 当前状态 | 风险等级 | 已有处理 | 建议修改方法 |
|---|---|---:|---|---|
| 本机没有 API key | 默认 `ai.demo.mode=true`，走 Mock | 低 | `AiServiceFactory` 自动选择 Mock | 保持提交 APK demo-safe；现场联网前才在本机关闭 demo |
| API key 是占位符 | 不视为可用 key | 低 | `AiConfig.hasUsableKey` 过滤占位符 | 已加 `AiConfigTest` 防回归 |
| vivo API 失败或返回空流 | 自动回退 Mock | 中 | `FallbackAiService` 捕获失败；空流抛错 | 演示前优先用 Mock 黄金链路录屏 |
| 模型未返回完整 HTML | 阻止裸文本进入 WebView | 中 | `extractHtml()` 抛 `AiException` | UI 可进一步增加“重新生成/切换模板”提示 |
| 生成 HTML 引用外链 | WebView 拦截非 localhost/about/data/blob | 中 | `shouldInterceptRequest()` 阻断外部资源 | 如要支持图片，优先支持 data URI 或本地导入 |
| 生成 HTML 尝试跳转 | 阻止外部 URL 加载 | 低 | `shouldOverrideUrlLoading()` 拦截 | 可在 UI 追加“外部链接已被拦截”提示 |
| 生成 HTML 使用 localStorage | 可用 | 低 | 每个应用使用独立 `.localhost` 子域 + DOM Storage | 同一应用可持久化，不同应用减少数据串扰 |
| 分享 HTML | 分享成果页可分享 cache 文件 | 中 | FileProvider + cache-path | 接收方能否打开取决于系统/应用；保留导出作为稳妥路径 |
| 离线导出 HTML | 分享成果页可用系统文件创建器保存 | 低 | SAF `CreateDocument("text/html")` | 演示时导出到 Downloads，证明离线可留存 |
| 提交包泄露密钥 | 当前扫描无平台密钥前缀 | 高 | key 文件删除、忽略规则、demo-safe BuildConfig | 已暴露旧 key 必须在平台侧轮换 |
| WebView 调试日志泄露内容 | demo-safe 下不注入调试桥 | 低 | `DEBUG && !AI_DEMO_MODE` 才启用 | release/提交包保持 demo mode |
| 用户关闭所有功能 | 不会生成空功能描述 | 低 | `confirmPlan()` 前自动保留第一个核心功能 | 后续可在 UI 上直接禁用最后一个开关 |
| 长对话导致 token 过高 | 未处理 | 中 | 无 | 增加“生成前摘要历史”或限制只取最近 N 条消息 |
| 用户反馈修改后未直接刷新预览 | 当前反馈走对话/方案流程 | 中 | 可继续修改，但不够直观 | 增加“基于当前应用重新生成”状态，保留旧 app 直到新 app 成功 |
| 无网络现场演示 | 可演示 | 低 | Mock 模板 + 默认 demo mode | PPT/视频明确“真实模型可配，演示有离线兜底” |
| 设备没有文件管理器/分享目标 | 导出或分享可能失败 | 中 | Toast 提示失败 | 演示设备提前验证；视频以已验证设备录制 |
| 输入法遮挡 | 聊天页已 `imePadding()` | 中 | 基础适配 | 录屏前验证目标设备；必要时使用横向较少输入 |

## 2. 自举测试用例

### 用例 A：活动筹备黄金链路
输入：
> 给迎新活动做一个报名、分工和预算管理工具，要能记录参与同学、负责人、支出金额和筹备进度。

期望：
- 自动进入活动筹备方案。
- 方案页包含报名名单、任务分工、预算记录、进度打卡。
- 预览页能添加报名、任务、预算，统计数字会变化。
- 可以进入分享成果页导出 HTML，也可以分享 HTML。

### 用例 B：模型失败兜底
配置：
```properties
ai.demo.mode=false
ai.api.key=YOUR_API_KEY_HERE
```

期望：
- 工厂判断 key 不可用，直接使用 Mock。
- 应用不显示网络错误，不阻塞演示。

### 用例 C：外部资源拦截
让模型或手工 HTML 包含：
```html
<img src="https://example.com/a.png">
<a href="https://example.com">跳转</a>
```

期望：
- 页面主体仍可运行。
- 外部图片/链接不会加载到外部站点。

### 用例 D：非 HTML 响应
让 AI 返回普通文本。

期望：
- `RealAiService.extractHtml()` 抛错。
- `FallbackAiService` 使用 Mock HTML。
- 裸文本不会进入 WebView。

### 用例 E：离线导出
在预览页点击“分享成果”，再点击“导出 HTML”，保存为 `.html`。

期望：
- 系统文件创建器出现。
- 文件保存后可用浏览器打开。
- APK 不需要存储权限。

## 3. 下一轮代码优化优先级

1. 反馈修改时保留当前预览，并显示“正在生成新版”。
2. 给 WebView 拦截增加可见提示，展示安全沙箱价值。
3. 加 `MainViewModel` 单元测试，覆盖 Mock 兜底、方案生成、HTML 生成失败。
4. 增加可选的“查看 HTML 代码”只读页，便于答辩展示生成结果。
