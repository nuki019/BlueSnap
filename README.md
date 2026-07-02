# 蓝心快搭 (BlueSnap)

> 将创作的自由还给每个人 —— 用自然语言描述需求，AI 一键生成可运行的轻量应用

2026 中国大学生计算机设计大赛 AIGC 创新赛道参赛作品

## 项目简介

蓝心快搭 (BlueSnap) 是一个 Android 应用，面向有创造力但不一定会开发的人。用户通过自然语言描述想要的工具，AI 自动生成完整的单页 HTML 应用并在 WebView 沙箱中实时预览。无需编程知识，无需安装额外应用，即说即用。

**核心能力：**
- 自然语言理解：支持中文描述需求，AI 自动解析意图
- 智能方案生成：根据需求生成功能清单，用户可自定义开关
- 一键代码生成：生成完整的单 HTML 文件应用（CSS/JS 内联、无外部依赖）
- 实时预览运行：WebView 沙箱内直接运行，支持 localStorage 持久化
- 流式对话输出：SSE 流式返回，实时显示 AI 思考过程
- 离线导出与分享：生成的 HTML 可保存到本地，也可通过系统分享给他人
- Demo-safe 兜底：提交包默认使用 Mock 模板，不内置真实 API Key

## 技术栈

| 层级 | 技术 |
|------|------|
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM + 单向数据流 (StateFlow) |
| 网络 | OkHttp 4.12 + SSE 流式通信 |
| AI | vivo 蓝心大模型 API (OpenAI 兼容协议) |
| 预览 | WebView + JavaScript 沙箱 |
| 语言 | Kotlin, minSdk 33, targetSdk 36 |

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/nmsl-ai/BlueSnap.git
cd BlueSnap
```

### 2. 配置 API Key

在项目根目录创建 `local.properties`（已在 .gitignore 中）：

```properties
sdk.dir=/path/to/your/Android/Sdk
ai.demo.mode=true
ai.provider=vivo
ai.fallback.provider=mock
ai.api.key=YOUR_API_KEY_HERE
ai.base.url=https://api-ai.vivo.com.cn/v1
ai.model=Doubao-Seed-2.0-pro
```

> API Key 申请：https://aigc.vivo.com.cn

默认 `ai.demo.mode=true`，不会调用真实模型，适合复赛提交、录屏和离线演示。需要联调真实模型时，将 `ai.demo.mode=false` 并填入短期可轮换 Key。DeepSeek 可作为本地备用 provider，详见 `docs/usage_and_development.md`。

### 3. 编译运行

```bash
./gradlew installDebug
```

或使用 Android Studio 直接打开项目并运行。

## 项目结构

```
app/src/main/java/com/example/bluesnap/
├── MainActivity.kt                 # 入口
├── data/
│   └── Models.kt                   # 数据模型 (ChatMessage, AppPlan, AppState)
├── ai/
│   ├── AiService.kt                # AI 服务接口
│   ├── MockAiService.kt            # Mock 实现 (离线演示)
│   └── RealAiService.kt            # 真实 API 实现 (SSE 流式)
├── viewmodel/
│   └── MainViewModel.kt            # 状态管理
└── ui/
    ├── theme/                      # Material3 主题
    ├── navigation/
    │   └── AppNavigation.kt        # 页面导航
    ├── components/
    │   ├── MessageBubble.kt        # 消息气泡
    │   ├── StreamingBubble.kt      # 流式输出气泡
    │   ├── PlanCard.kt             # 方案卡片
    │   └── TemplateChip.kt         # 场景模板
    └── screens/
        ├── HomeScreen.kt           # 首页
        ├── ChatScreen.kt           # 对话页
        ├── PlanScreen.kt           # 方案确认页
        └── PreviewScreen.kt        # WebView 预览页
```

## 演示流程

1. 打开应用 → 首页展示场景模板（活动筹备、番茄钟、待办清单等）
2. 点击模板或手动输入 → "给迎新活动做一个报名、分工和预算管理工具"
3. AI 流式回复 → 实时显示思考过程
4. 查看方案 → 功能清单可自定义开关
5. 确认生成 → AI 生成完整 HTML 应用
6. 预览运行 → WebView 内直接操作，支持数据持久化
7. 离线导出/分享 HTML → 输入反馈继续优化

## 可用模型

| 模型 | 特点 |
|------|------|
| Doubao-Seed-2.0-pro | 代码生成质量最佳，速度较慢 |
| Doubao-Seed-2.0-mini | 速度快，质量略低 |
| Volc-DeepSeek-V3.2 | 综合能力强 |

在 `local.properties` 中修改 `ai.model` 即可切换。

## 复赛材料与文档

| 文档 | 用途 |
|---|---|
| `docs/completion_review.md` | 完成度评估、PPT结构、视频脚本、海报文案 |
| `docs/robustness_review.md` | 鲁棒性案例、风险等级、对应修改方法 |
| `docs/usage_and_development.md` | 开发/使用/安全检查说明 |
| `docs/defense_narrative.md` | 答辩叙事、竞品对比、问答准备 |

提交前必须确认：
- 不提交 `local.properties`、明文 Key、`app/build/`、`.gradle/`。
- APK 和源码扫描不得包含真实 Key。
- PPT 使用真实截图，禁用流程图。
- 视频为 MP4，≤3分钟，≥1080P。
- 海报为竖版 70cm × 150cm，≥2MB。

## License

本项目为 2026 中国大学生计算机设计大赛参赛作品，仅供学习交流使用。
