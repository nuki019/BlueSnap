# 蓝心快搭核心代码包说明

本代码包只包含复赛提交所需的核心差异化模块，不包含完整工程、不包含本机 `local.properties`、不包含真实用户输入的备用模型 Key、不包含构建产物。

## 包含模块

- AI 服务接口与实现：蓝心大模型 / OpenAI 兼容模型调用、Mock 兜底、生成方案、生成 HTML Bundle。
- 模型配置：内置国产模型预设、本机备用模型凭据保存逻辑。
- 主业务状态：一句话需求、方案确认、生成、历史、分享的 ViewModel 状态流。
- 关键 UI：主页入口、对话页、方案确认页、WebView 预览页、历史页、设置页、分享页。
- WebView 与文件分发：沙箱预览、HTML 导出、FileProvider 系统分享。

## 不包含内容

- 不包含 `local.properties`。
- 不包含 DeepSeek 或用户输入的备用模型 Key。
- 不包含 APK、build 目录、截图大图、视频素材。
- 不包含完整 Gradle 缓存或 IDE 配置。

## 运行说明

完整工程在开发机中可通过 Android Studio 或 Gradle 构建。复赛提交时，APK 由完整工程构建；本 zip 仅用于展示“核心功能调用大模型的代码包”。

如需从完整工程重新构建，请在项目根目录准备本机 `local.properties`，配置 Android SDK 和参赛用蓝心 AppKey，然后运行：

```powershell
.\gradlew.bat :app:assembleContestDemo
```

## 安全说明

源码包中不提交长期有效私钥。复赛 APK 可按赛事现场需要注入短期参赛 AppKey；赛后建议轮换或吊销。
