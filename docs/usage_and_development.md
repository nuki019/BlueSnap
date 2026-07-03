# 蓝心快搭开发与使用说明

## 1. 运行模式

默认提交模式是安全演示模式：

```properties
ai.demo.mode=true
ai.provider=vivo
ai.fallback.provider=mock
ai.api.key=YOUR_API_KEY_HERE
ai.base.url=https://api-ai.vivo.com.cn/v1
ai.model=Doubao-Seed-2.0-pro
```

含义：
- `ai.demo.mode=true`：永远使用内置 Mock 模板，不消耗 API，不需要网络。
- `ai.demo.mode=false` 且 key 可用：使用真实模型。
- 真实模型失败、空流、HTML 不完整时：自动回退 Mock。
- DeepSeek 仅作本地备用 provider，不作为作品核心叙事。

## 2. 本机配置

复制示例文件：

```powershell
Copy-Item local.properties.example local.properties
```

修改 `sdk.dir` 为本机 Android SDK 路径。真实 key 只放在 `local.properties`，不要写入 README、PPT、截图、代码或提交包。

### vivo 云端模型

```properties
ai.demo.mode=false
ai.provider=vivo
ai.fallback.provider=mock
ai.api.key=YOUR_VIVO_KEY
ai.base.url=https://api-ai.vivo.com.cn/v1
ai.model=Doubao-Seed-2.0-pro
```

### DeepSeek 备用

```properties
ai.demo.mode=false
ai.provider=deepseek
ai.fallback.provider=mock
ai.api.key=YOUR_DEEPSEEK_KEY
ai.base.url=https://api.deepseek.com
ai.model=deepseek-chat
```

## 3. 常用命令

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

APK 输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 4. 演示操作

推荐黄金链路：

1. 打开首页，确认四个校园场景卡：活动筹备、课程协作、求职实习、生活管理。
2. 点击“活动筹备”。
3. 等待流式回复完成。
4. 在方案页确认功能：报名名单、任务分工、预算记录、进度打卡。
5. 点击“确认生成”。
6. 在预览页添加报名、任务和预算。
7. 点击“分享成果”，进入结果分发页。
8. 点击“导出 HTML”，保存 HTML 到本机。
9. 点击“系统分享 HTML”，展示 HTML 可流转。

## 5. 安全检查

提交前必须执行：

```powershell
git check-ignore -v -- 'example-api-key.txt' 'exampleapikey.txt' '.env' '.env.local' 'secrets/demo.txt'
git ls-files | Select-String -Pattern 'api-key|apikey|local.properties|\.env|secrets|BuildConfig.java|app-debug.apk|\.jks|\.keystore'
# 将 PATTERN 替换为本项目用到的平台密钥前缀或已吊销 key 的片段。
rg -n "PATTERN" --glob '!app/build/**' --glob '!build/**' --glob '!.git/**'
rg -a -n "PATTERN" app\build\outputs\apk\debug\app-debug.apk
```

期望：
- `git check-ignore` 命中 `.gitignore` 规则。
- `git ls-files` 无输出。
- 两个 `rg` 命令无输出。

## 6. 提交材料建议

只打包差异化核心模块：
- AI 调用与回退：`ai/`
- 状态流转：`viewmodel/MainViewModel.kt`
- WebView 沙箱与导出分享：`ui/screens/PreviewScreen.kt`、`ui/screens/ShareScreen.kt`
- 方案确认和模板入口：`ui/components/TemplateChip.kt`、`ui/screens/HomeScreen.kt`
- 配置说明：`local.properties.example`、README、关键 docs

不要提交：
- `local.properties`
- `app/build/`
- `.gradle/`
- 明文 key 文件
- 临时录屏素材源文件
