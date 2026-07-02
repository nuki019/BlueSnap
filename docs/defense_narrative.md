# 蓝心快搭答辩叙事与竞品对比

## 1. 答辩主叙事

蓝心快搭不是“又一个代码生成器”，而是面向有创造力的人，把“我想要一个工具”的表达，直接变成手机里能运行、能保存、能分享的轻应用。

核心口号：

> 把创造的自由还给用户。

目标人群可以广泛表达为：
- 学生和社团组织者：临时活动、学习卡片、统计表、打卡工具。
- 小微商家：预约、记账、库存、客户记录。
- 内容创作者：选题库、素材管理、互动小游戏。
- 教师和助教：课堂签到、练习卡、分组任务。
- 生活效率爱好者：自定义清单、家庭预算、习惯记录。

答辩时不要把用户讲得过散。结构应为：
1. 先讲“大叙事”：每个有创造力的人都可能需要自己的小工具。
2. 再落到“黄金样例”：社团负责人/班委的活动筹备工具。
3. 最后回到“可扩展”：同一闭环可复用到学习、生活、商家、创作等场景。

## 2. 一句话定义

用户用自然语言说出想法，蓝心快搭先生成可确认的方案，再生成单 HTML 轻应用，并在手机 WebView 沙箱中即时运行，支持离线导出和分享。

## 3. 与竞品的事实对比

核查日期：2026-07-02。以下只使用官方页面或官方代码仓库作依据。

| 产品 | 官方定位/能力 | 优势 | 蓝心快搭应避开的正面竞争 | 蓝心快搭差异化 |
|---|---|---|---|---|
| GitHub Spark | 官方介绍为用自然语言创建、分享 micro apps，可在桌面和移动设备使用；GitHub Docs 说明可用自然语言构建和部署 AI web app。来源：[GitHub Next](https://githubnext.com/projects/github-spark/)、[GitHub Docs](https://docs.github.com/copilot/tutorials/building-ai-app-prototypes) | GitHub 生态强、数据存储/AI/GitHub auth/部署完整 | 不比完整 fullstack 和 GitHub 协作生态 | 更轻、更手机端、更适合本地即时小工具和比赛设备演示 |
| Replit Agent | 官方称可用自然语言构建 app/site，部署并分享；Agent 可继续反馈修改，并集成数据库、认证和第三方服务。来源：[Replit AI](https://replit.com/ai)、[Replit Agent](https://replit.com/products/agent) | 云 IDE、部署、测试、服务集成完整 | 不比云端工程能力和生产部署 | 用户无需理解 IDE，直接在 Android 应用内完成生成和运行 |
| Bolt.new | 官方/开源 README 称可在浏览器中 prompt、run、edit、deploy full-stack apps，具备 WebContainers、npm、Node server、部署等能力。来源：[Bolt GitHub](https://github.com/stackblitz/bolt.new)、[Bolt](https://bolt.new/) | 浏览器全栈开发环境强，适合 MVP 和 Web 项目 | 不比全栈项目复杂度 | 蓝心快搭聚焦单 HTML 轻应用，目标是“即用的小工具”而不是“完整工程” |
| Lovable | 官方文档称其是用自然语言构建、迭代、部署 Web 应用的 full-stack AI development platform。来源：[Lovable Docs](https://docs.lovable.dev/introduction/welcome)、[Lovable FAQ](https://docs.lovable.dev/introduction/faq) | 面向非技术用户，Web app 生成和部署成熟 | 不比 Web 产品上线能力 | 蓝心快搭更强调手机端运行、离线导出和 vivo/Android 设备现场体验 |
| Vercel v0 | 官方文档称 v0 可用 prompt 创建真实代码、full-stack apps 和 agents，并可部署或发 PR。来源：[Vercel v0 Docs](https://vercel.com/docs/v0)、[Vercel Academy](https://vercel.com/academy/ai-sdk/ui-with-v0) | UI 质量高，前端组件生态强，部署链路成熟 | 不比 React UI 生成质量和 Vercel 云部署 | 蓝心快搭不是 UI 组件生成器，而是面向用户的轻应用闭环 |

## 4. 答辩中的优势表达

建议表达：
- “我们承认云端 full-stack AI builder 已经很强，所以我们不重复做一个云 IDE。”
- “蓝心快搭选择更小但更确定的切口：手机端、单 HTML、WebView 沙箱、离线导出。”
- “评委看到的不是概念图，而是从输入到运行的完整链路。”
- “它解决的是长尾轻工具：功能小、周期短、个人化强、不值得下载大 App，也不值得开电脑开发。”

不要表达：
- “我们已经实现端侧大模型。”
- “我们能生成 APK。”
- “我们比 Replit/Bolt 更强。”
- “我们是完整应用市场。”

## 5. 可能被问到的问题

**问：和 GitHub Spark、Replit、Bolt 这类工具有什么区别？**  
答：它们更偏云端 Web/full-stack 工程生成，适合部署成网站或完整项目。蓝心快搭聚焦手机端轻应用：自然语言生成单 HTML，在 Android WebView 沙箱里立即运行，并能离线导出和分享。我们的目标不是替代云 IDE，而是让普通用户在手机上快速得到一个“刚好够用”的小工具。

**问：为什么不是直接生成 APK？**  
答：复赛阶段选择单 HTML 是为了稳定、快速和安全。轻工具不需要完整 APK 安装链路，WebView 可即时预览，localStorage 可保存数据，HTML 文件也更容易分享和离线留存。APK 生成可以作为后续方向，但不作为当前已实现能力。

**问：模型生成代码不稳定怎么办？**  
答：产品上有三层处理：第一，先生成方案让用户确认，减少模型自由发挥；第二，HTML 提取失败时不加载裸文本；第三，真实模型失败会回退 Mock 模板，保证演示和基础体验可用。后续会增加 JS 运行错误可视化和自动修复。

**问：生成的 HTML 是否安全？**  
答：当前 WebView 禁止文件访问和 content 访问，阻断外部跳转和外部资源，关闭混合内容，提交包默认不注入调试桥。它适合运行轻量本地工具，不开放系统敏感能力。

**问：为什么用户愿意用？**  
答：因为用户的很多需求是即时、个人化、低频但刚需的。比如活动筹备、课堂统计、家庭预算、小店库存。现成 App 往往太重或不贴合，低代码工具又需要学习。蓝心快搭把这个中间地带变成“说一句话即可试用”。

## 6. PPT 叙事顺序

1. 封面：把创造的自由还给用户。
2. 用户洞察：创造力很多，落地工具很少。
3. 具体样例：社团活动筹备为什么麻烦。
4. 产品真实截图：一句话输入。
5. 方案确认截图：AI 不是直接替用户决定，用户可控。
6. 预览运行截图：WebView 里真实可操作。
7. 离线导出/分享截图：从“生成”走向“可留存、可流转”。
8. 竞品对比：不做云 IDE，做手机端轻应用闭环。
9. 安全与鲁棒性：demo-safe、Mock 兜底、WebView 沙箱。
10. 总结：让每个有创造力的人，在手机上拥有自己的小工具。
