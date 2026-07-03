# 蓝心快搭 mid 复赛交付文件清单

目录：`C:\Users\wfy\Desktop\蓝心快搭\mid`

## 可运行产品包

- `BlueSnap_mid_contestDemo_signed.apk`
  - 主提交 APK。
  - `contestDemo` 构建，已本地签名，可安装。
  - 包内允许包含蓝心参赛 AppKey，用于评委现场联网体验。

- `BlueSnap_mid_debug_signed.apk`
  - 备用 Debug APK。
  - 仅用于本机或现场快速排障，不建议作为主提交包。

## 核心代码包

- `BlueSnap_mid_core_code.zip`
  - 包含 AI 调用、模型兜底、方案生成、HTML 生成、WebView 沙箱、导出分享、关键 UI 和 ViewModel。
  - 不包含 `local.properties`。
  - 不包含 DeepSeek Key 或用户输入的备用模型 Key。

## 材料文档

- `BlueSnap_mid_ppt_outline.md`
  - 复赛作品策划 PPT 详细大纲。
  - 按模板 13 页结构组织，强调真实截图、用户洞察、差异化和完成度。

- `BlueSnap_mid_poster_prompt.md`
  - 竖版 70cm x 150cm 宣传海报生成提示词。
  - 已结合当前首页截图风格，包含中文提示词、英文增强提示词和负面提示词。

- `BlueSnap_mid_core_code_readme.md`
  - 核心源码包说明。

## 待补充材料

- 作品宣传海报成图：竖版 70cm x 150cm，文件不小于 2MB。
- 作品演示视频：建议 9:16 竖版，约 3 分钟，MP4 格式。
- PPT 成稿：基于 `BlueSnap_mid_ppt_outline.md` 和模板制作，禁止流程图，使用真实截图。
