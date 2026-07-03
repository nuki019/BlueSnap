package com.example.bluesnap.ai

import com.example.bluesnap.data.AppPlan
import com.example.bluesnap.data.ChatMessage
import com.example.bluesnap.data.DEFAULT_SYSTEM_PROMPT
import com.example.bluesnap.data.Feature
import com.example.bluesnap.data.GenerationBundle
import com.example.bluesnap.data.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockAiService : AiService {

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage {
        delay(500)
        return ChatMessage(role = Role.ASSISTANT, content = buildReply(messages))
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val fullText = buildReply(messages)
        val buffer = StringBuilder()
        for (char in fullText) {
            buffer.append(char)
            emit(buffer.toString())
            delay(18)
        }
    }

    override suspend fun generatePlan(
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): AppPlan {
        delay(450)
        val lastUser = chatHistory.lastOrNull { it.role == Role.USER }?.content.orEmpty()
        val detected = detectAppType(lastUser)
        return plans[detected?.first] ?: plans.getValue("通用工具")
    }

    override suspend fun generateHtml(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): String {
        delay(900)
        return htmlTemplates[plan.name] ?: htmlTemplates.getValue("通用工具")
    }

    override suspend fun generateBundle(
        plan: AppPlan,
        chatHistory: List<ChatMessage>,
        systemPrompt: String
    ): GenerationBundle {
        delay(900)
        val html = htmlTemplates[plan.name] ?: htmlTemplates.getValue("通用工具")
        return GenerationBundle(
            html = html,
            summary = plan.description,
            imagePrompt = mediaPrompts[plan.name]?.first,
            audioPrompt = mediaPrompts[plan.name]?.second
        )
    }

    private fun buildReply(messages: List<ChatMessage>): String {
        val lastUser = messages.lastOrNull { it.role == Role.USER }?.content.orEmpty()
        val detected = detectAppType(lastUser)
        return if (detected != null) {
            "好的，我来帮你做一个「${detected.first}」。\n\n" +
                "${detected.second}\n\n" +
                "我已经为你规划好了功能清单和界面风格，确认后即可生成一个可在手机里运行、可保存、可分享的 HTML 轻工具。"
        } else {
            "收到你的需求。我会先整理功能清单和界面风格，再生成一个移动端优先、可本地保存的单文件 HTML 轻工具。\n\n" +
                "你也可以试试这些校园效率场景：活动筹备、课程任务板、小组分工、求职投递表、生活预算、快捷思维导图。"
        }
    }

    private fun detectAppType(input: String): Pair<String, String>? {
        val lower = input.lowercase()
        return when {
            listOf("思维导图", "脑图", "结构图", "快捷思维导图", "mindmap", "mind map").any { lower.contains(it) } ->
                "快捷思维导图" to "这是一个为课堂笔记、活动复盘和求职准备设计的大纲脑图工具，支持节点增删、层级调整、复制大纲和本地保存。"
            listOf("活动", "社团", "报名", "筹备", "迎新").any { lower.contains(it) } ->
                "活动筹备" to "这是一个面向校园活动团队的筹备助手，支持报名名单、任务分工、预算记录和进度打卡。"
            listOf("课程", "作业", "ddl", "考试", "待办", "todo", "任务").any { lower.contains(it) } ->
                "课程任务板" to "这是一个课程任务板，支持作业、考试、DDL 和完成状态统一追踪。"
            listOf("小组", "协作", "成员", "负责人").any { lower.contains(it) } ->
                "小组分工" to "这是一个小组协作分工工具，支持成员任务、负责人、进度状态和本地记录。"
            listOf("求职", "实习", "简历", "投递", "面试", "岗位").any { lower.contains(it) } ->
                "求职投递表" to "这是一个求职实习投递表，支持公司岗位、投递状态、面试安排和反馈记录。"
            listOf("预算", "记账", "账本", "消费", "花销", "支出").any { lower.contains(it) } ->
                "生活预算" to "这是一个校园生活预算工具，支持支出记录、分类统计和本月剩余额度提醒。"
            listOf("打卡", "习惯", "habit", "坚持").any { lower.contains(it) } ->
                "习惯打卡" to "这是一个习惯养成工具，支持多习惯管理、连续打卡天数追踪和日历视图。"
            else -> null
        }
    }

    private val plans = mapOf(
        "活动筹备" to AppPlan(
            name = "活动筹备",
            description = "校园活动报名、分工、预算和进度管理工具",
            features = listOf(
                Feature("报名名单", "记录参与者姓名和联系方式"),
                Feature("任务分工", "按负责人追踪筹备事项"),
                Feature("预算记录", "记录支出项目并自动汇总金额"),
                Feature("进度打卡", "标记筹备任务完成状态")
            )
        ),
        "课程任务板" to AppPlan(
            name = "课程任务板",
            description = "作业、考试和 DDL 的统一追踪工具",
            features = listOf(
                Feature("课程任务", "记录作业、阅读和复习事项"),
                Feature("DDL 提醒", "标记截止日期和紧急程度"),
                Feature("完成状态", "切换待办、进行中和已完成"),
                Feature("本地留存", "关闭后任务仍保存在手机里")
            )
        ),
        "小组分工" to AppPlan(
            name = "小组分工",
            description = "小组作业成员分工和进度同步工具",
            features = listOf(
                Feature("成员列表", "记录小组成员和联系方式"),
                Feature("任务分配", "给每个任务指定负责人"),
                Feature("进度跟踪", "查看每项任务完成状态"),
                Feature("汇报清单", "整理可分享的阶段性记录")
            )
        ),
        "求职投递表" to AppPlan(
            name = "求职投递表",
            description = "求职实习投递与面试跟进工具",
            features = listOf(
                Feature("岗位记录", "记录公司、岗位和投递渠道"),
                Feature("状态跟进", "追踪已投递、笔试、面试和 offer"),
                Feature("面试安排", "记录时间地点和准备事项"),
                Feature("反馈复盘", "保存面试反馈和下一步动作")
            )
        ),
        "生活预算" to AppPlan(
            name = "生活预算",
            description = "校园日常支出和预算控制工具",
            features = listOf(
                Feature("支出记录", "快速记录金额、分类和备注"),
                Feature("分类统计", "查看餐饮、交通、学习等开销"),
                Feature("余额提醒", "根据月预算显示剩余额度"),
                Feature("历史记录", "本地保存每一笔消费")
            )
        ),
        "快捷思维导图" to AppPlan(
            name = "快捷思维导图",
            description = "面向课堂笔记和项目复盘的手机端层级脑图工具",
            features = listOf(
                Feature("节点编辑", "快速添加主题、子节点和补充说明"),
                Feature("层级管理", "支持缩进、提升和展开收起"),
                Feature("结构导出", "一键复制为大纲文本"),
                Feature("本地保存", "自动保存最近一次脑图内容")
            )
        ),
        "习惯打卡" to AppPlan(
            name = "习惯打卡",
            description = "好习惯养成追踪工具",
            features = listOf(
                Feature("习惯管理", "创建多个自定义习惯"),
                Feature("每日打卡", "一键完成当日打卡"),
                Feature("连续天数", "追踪连续打卡天数"),
                Feature("本地保存", "记录保存在手机本地")
            )
        ),
        "通用工具" to AppPlan(
            name = "通用工具",
            description = "校园场景自定义轻量工具",
            features = listOf(
                Feature("核心功能", "基于你的需求自动生成"),
                Feature("简洁界面", "清爽直观的移动端界面"),
                Feature("数据存储", "本地持久化保存"),
                Feature("一键生成", "即开即用无需安装")
            )
        )
    )

    private val htmlTemplates = mapOf(
        "活动筹备" to eventHtml,
        "课程任务板" to campusListHtml(
            title = "课程任务板",
            subtitle = "把作业、考试、阅读和 DDL 放在同一屏追踪。",
            placeholder = "添加课程任务，如：周五前交实验报告",
            seeds = listOf("高数作业 DDL 周五", "英语展示 PPT", "实验报告提交")
        ),
        "小组分工" to campusListHtml(
            title = "小组分工",
            subtitle = "把成员、负责人和进度记录在一个可分享的小工具里。",
            placeholder = "添加分工，如：小王负责资料收集",
            seeds = listOf("资料收集 - 小王", "PPT 设计 - 小李", "课堂汇报 - 小陈")
        ),
        "求职投递表" to campusListHtml(
            title = "求职投递表",
            subtitle = "记录公司、岗位、投递状态和面试安排。",
            placeholder = "添加投递，如：蓝心科技 产品实习 已投递",
            seeds = listOf("产品实习 - 已投递", "算法实习 - 准备笔试", "运营实习 - 等待面试")
        ),
        "生活预算" to campusListHtml(
            title = "生活预算",
            subtitle = "记录校园日常开销，随手看本月预算压力。",
            placeholder = "添加支出，如：午餐 18 元",
            seeds = listOf("午餐 18 元", "打印资料 12 元", "地铁 6 元")
        ),
        "快捷思维导图" to mindMapHtml,
        "习惯打卡" to campusListHtml(
            title = "习惯打卡",
            subtitle = "把每天想坚持的事变成清晰的打卡记录。",
            placeholder = "添加习惯，如：背单词 20 分钟",
            seeds = listOf("背单词 20 分钟", "跑步 2 公里", "睡前复盘")
        ),
        "通用工具" to campusListHtml(
            title = "校园轻工具",
            subtitle = "把临时需求整理成可保存、可分享的手机小工具。",
            placeholder = "添加一条记录",
            seeds = listOf("第一条任务", "第二条记录", "待完善事项")
        )
    )

    private val mediaPrompts = mapOf(
        "活动筹备" to ("一张清爽的校园活动筹备看板插图，手机界面上有报名、分工、预算和进度模块，蓝白色科技风" to "朗读活动筹备进度摘要，语气清晰、节奏轻快"),
        "快捷思维导图" to ("一张手机端思维导图工具插图，中心主题向外延展为课程笔记、项目复盘、求职准备，简洁蓝白配色" to "朗读当前脑图的大纲层级，适合复习和汇报前快速确认")
    )
}

private fun campusListHtml(
    title: String,
    subtitle: String,
    placeholder: String,
    seeds: List<String>
): String {
    val seedItems = seeds.joinToString(",") { "\"${it.escapeJsString()}\"" }
    return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>$title</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f5f8fd;color:#17233c;min-height:100vh;padding:16px 16px 24px}
.hero{background:linear-gradient(135deg,#1a73e8,#13a8c8);color:#fff;border-radius:22px;padding:20px;margin-bottom:14px;box-shadow:0 10px 28px rgba(26,115,232,.22)}
.hero h1{font-size:24px;margin-bottom:6px}.hero p{font-size:13px;opacity:.9;line-height:1.5}
.panel{background:#fff;border:1px solid #e3eaf6;border-radius:18px;padding:14px;box-shadow:0 8px 24px rgba(16,35,75,.06)}
.row{display:flex;gap:8px;margin-bottom:12px}
input{min-width:0;flex:1;border:1px solid #d9e3f0;border-radius:12px;padding:0 12px;height:46px;font-size:14px;outline:none;background:#fbfdff}
input:focus{border-color:#1a73e8}
button{border:0;border-radius:12px;background:#1a73e8;color:#fff;font-weight:800;min-height:46px;padding:0 14px}
.item{display:flex;align-items:center;gap:10px;background:#f7faff;border:1px solid #e8eef8;border-radius:14px;padding:12px;margin-top:9px}
.item.done{opacity:.58}.item.done b{text-decoration:line-through}
.item-main{flex:1;min-width:0}.item b{font-size:15px}.item span{font-size:12px;color:#52627a}
.item button{background:#ffe8e8;color:#d63031;width:34px;height:34px;min-height:34px;padding:0;font-size:18px}
.empty{text-align:center;color:#667085;padding:28px 0;font-size:14px}
</style>
</head>
<body>
<div class="hero"><h1>$title</h1><p>$subtitle</p></div>
<div class="panel">
  <div class="row">
    <input id="text" placeholder="$placeholder">
    <button id="add">添加</button>
  </div>
  <div id="list"></div>
</div>
<script>
const key='bluesnap_${title.escapeJsString()}';
let items=JSON.parse(localStorage.getItem(key)||'null')||[$seedItems];
items=items.map(x=>typeof x==='string'?{text:x,done:false}:x);
function save(){localStorage.setItem(key,JSON.stringify(items))}
function render(){
  const list=document.getElementById('list');
  list.innerHTML='';
  if(!items.length){list.innerHTML='<div class="empty">暂无记录，添加一条开始使用</div>';return}
  items.forEach((item,index)=>{
    const div=document.createElement('div');
    div.className='item '+(item.done?'done':'');
    const main=document.createElement('div');
    main.className='item-main';
    main.addEventListener('click',()=>toggleItem(index));
    const title=document.createElement('b');
    title.textContent=item.text;
    const status=document.createElement('span');
    status.textContent=item.done?'已完成':'进行中';
    main.appendChild(title);main.appendChild(document.createElement('br'));main.appendChild(status);
    const del=document.createElement('button');
    del.textContent='×';
    del.addEventListener('click',()=>deleteItem(index));
    div.appendChild(main);div.appendChild(del);list.appendChild(div);
  })
}
function addItem(){const input=document.getElementById('text');const text=input.value.trim();if(!text)return;items.unshift({text,done:false});input.value='';save();render()}
function toggleItem(index){items[index].done=!items[index].done;save();render()}
function deleteItem(index){items.splice(index,1);save();render()}
document.getElementById('add').addEventListener('click',addItem);
document.getElementById('text').addEventListener('keydown',e=>{if(e.key==='Enter')addItem()});
save();render();
</script>
</body>
</html>
""".trimIndent()
}

private fun String.escapeJsString(): String =
    replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

private val mindMapHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>快捷思维导图</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f4f7fb;color:#17233c;min-height:100vh;padding:12px 12px 78px}
.top{position:sticky;top:0;z-index:3;background:rgba(244,247,251,.94);backdrop-filter:blur(10px);padding:6px 0 10px}
.head{display:flex;align-items:center;justify-content:space-between;background:#fff;border:1px solid #e2eaf6;border-radius:16px;padding:10px 12px;box-shadow:0 8px 24px rgba(16,35,75,.06)}
.head h1{font-size:17px}.head p{font-size:12px;color:#52627a;margin-top:2px}
.stats{display:flex;gap:6px}.stat{min-width:48px;text-align:center;background:#eaf3ff;color:#1a73e8;border-radius:12px;padding:6px}.stat b{display:block;font-size:15px}.stat span{font-size:10px}
.toolbar{display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-top:10px}
button{border:0;border-radius:12px;background:#1a73e8;color:#fff;font-weight:800;min-height:42px}
.toolbar button.secondary{background:#eef4ff;color:#1a73e8}.toolbar button.ghost{background:#fff;color:#344054;border:1px solid #dbe5f3}
.panel{background:#fff;border:1px solid #e3eaf6;border-radius:18px;padding:10px;box-shadow:0 8px 24px rgba(16,35,75,.06)}
.node{position:relative;background:#f8fbff;border:1px solid #dce7f5;border-radius:14px;margin:7px 0;padding:9px 9px 9px 10px}
.node:before{content:"";position:absolute;left:-7px;top:22px;width:7px;height:1px;background:#b8c7dc}
.node.level0{background:#eaf4ff;border-color:#b9d8ff}.node.level0:before{display:none}
.row{display:flex;align-items:center;gap:7px}.title{flex:1;min-width:0;font-size:15px;font-weight:800;border:0;background:transparent;color:#17233c;outline:none}
.note{width:100%;border:0;background:transparent;color:#344054;outline:none;font-size:13px;line-height:1.45;margin-top:4px;resize:none;min-height:24px}
.badge{font-size:11px;color:#1a73e8;background:#e8f1ff;border-radius:999px;padding:4px 8px;white-space:nowrap}
.iconbtn{width:32px;height:32px;min-height:32px;border:0;border-radius:10px;background:#eef4ff;color:#1a73e8;font-weight:900}
.children{margin-left:10px;border-left:1px solid #d8e3f3;padding-left:8px}.collapsed>.children{display:none}.collapsed>.row .toggle:after{content:"+"}.toggle:after{content:"-"}
.quick{position:fixed;right:14px;bottom:16px;z-index:4;display:flex;align-items:center;gap:8px;background:#fff;border:1px solid #dbe5f3;border-radius:999px;padding:8px;box-shadow:0 12px 30px rgba(16,35,75,.18)}
.quick input{width:min(58vw,230px);border:0;outline:none;font-size:14px;color:#17233c;padding-left:8px}
.quick button{border-radius:999px;width:44px;min-height:44px;padding:0;font-size:20px}
.toast{position:fixed;left:50%;bottom:78px;transform:translateX(-50%);background:#17233c;color:#fff;padding:10px 14px;border-radius:999px;font-size:13px;opacity:0;pointer-events:none;transition:.2s;z-index:5}.toast.show{opacity:1}
</style>
</head>
<body>
<section class="top">
  <div class="head">
    <div><h1>快捷思维导图</h1><p>课堂笔记 / 项目复盘 / 求职准备</p></div>
    <div class="stats">
      <div class="stat"><b id="total">0</b><span>节点</span></div>
      <div class="stat"><b id="depth">0</b><span>层级</span></div>
    </div>
  </div>
  <div class="toolbar">
    <button id="addRoot">添加分支</button>
    <button class="secondary" id="copy">复制大纲</button>
    <button class="ghost" id="reset">重置</button>
  </div>
</section>
<main class="panel" id="map"></main>
<div class="quick">
  <input id="quick" placeholder="新分支，如：面试准备">
  <button id="quickAdd">+</button>
</div>
<div class="toast" id="toast">已复制</div>
<script>
const storeKey='bluesnap_mind_map_v2';
function seed(){return {id:1,title:'校园效率工具设计',note:'从用户痛点出发，拆成可演示的功能模块',children:[
  {id:2,title:'用户洞察',note:'大学生和准职场青年，经常需要临时小工具',children:[
    {id:5,title:'活动筹备',note:'报名、分工、预算、进度',children:[]},
    {id:6,title:'课程协作',note:'DDL、成员任务、提交状态',children:[]}
  ]},
  {id:3,title:'核心能力',note:'一句话生成可运行 HTML',children:[
    {id:7,title:'方案可控',note:'先确认功能，再生成',children:[]},
    {id:8,title:'结果分享',note:'导出和系统分享 HTML',children:[]}
  ]},
  {id:4,title:'答辩亮点',note:'轻量、速度快、隐私边界清楚',children:[]}
]}}
let root=load();
function load(){try{return JSON.parse(localStorage.getItem(storeKey))||seed()}catch(e){return seed()}}
function save(){localStorage.setItem(storeKey,JSON.stringify(root));render()}
function nextId(){let max=0;walk(root,n=>{if(n.id>max)max=n.id});return max+1}
function walk(n,fn,level=0,parent=null){fn(n,level,parent);(n.children||[]).forEach(c=>walk(c,fn,level+1,n))}
function find(id){let hit=null;walk(root,n=>{if(n.id===id)hit=n});return hit}
function findParent(id){let hit=null;walk(root,(n,l,p)=>{if(n.id===id)hit=p});return hit}
function addChild(id){const n=find(id)||root;n.children=n.children||[];n.children.push({id:nextId(),title:'新节点',note:'',children:[]});save()}
function quickAdd(){const input=document.getElementById('quick');const text=input.value.trim();if(!text)return;root.children.push({id:nextId(),title:text,note:'',children:[]});input.value='';save();toast('已添加分支')}
function removeNode(id){if(id===root.id)return toast('中心主题不能删除');const p=findParent(id);if(!p)return;p.children=p.children.filter(x=>x.id!==id);save()}
function indent(id){const p=findParent(id);if(!p)return;const list=p.children;const idx=list.findIndex(x=>x.id===id);if(idx<=0)return toast('需要前一个同级节点');const node=list.splice(idx,1)[0];list[idx-1].children.push(node);save()}
function outdent(id){const p=findParent(id),gp=p?findParent(p.id):null;if(!p||!gp)return toast('已经在顶层');const node=find(id);p.children=p.children.filter(x=>x.id!==id);const parentIndex=gp.children.findIndex(x=>x.id===p.id);gp.children.splice(parentIndex+1,0,node);save()}
function toggle(id){const n=find(id);if(!n)return;n.collapsed=!n.collapsed;save()}
function render(){const map=document.getElementById('map');map.innerHTML=nodeHtml(root,0);bindInputs();let count=0,depth=0;walk(root,(n,l)=>{count++;depth=Math.max(depth,l+1)});document.getElementById('total').textContent=count;document.getElementById('depth').textContent=depth}
function nodeHtml(n,level){const child=(n.children||[]).map(c=>nodeHtml(c,level+1)).join('');return '<section class="node level'+level+(n.collapsed?' collapsed':'')+'" data-id="'+n.id+'"><div class="row"><button class="iconbtn toggle" data-action="toggle" data-id="'+n.id+'"></button><input class="title" value="'+esc(n.title)+'" data-field="title" data-id="'+n.id+'"><span class="badge">L'+(level+1)+'</span></div><textarea class="note" data-field="note" data-id="'+n.id+'" placeholder="补充说明">'+esc(n.note||'')+'</textarea><div class="row" style="margin-top:7px"><button class="iconbtn" data-action="add" data-id="'+n.id+'">+</button><button class="iconbtn" data-action="indent" data-id="'+n.id+'">→</button><button class="iconbtn" data-action="outdent" data-id="'+n.id+'">←</button><button class="iconbtn" data-action="remove" data-id="'+n.id+'">×</button></div><div class="children">'+child+'</div></section>'}
function bindInputs(){document.querySelectorAll('[data-field]').forEach(el=>{el.oninput=()=>{const n=find(parseInt(el.dataset.id));if(n){n[el.dataset.field]=el.value;localStorage.setItem(storeKey,JSON.stringify(root))}}});document.querySelectorAll('[data-action]').forEach(btn=>{btn.onclick=()=>{const id=parseInt(btn.dataset.id);const a=btn.dataset.action;if(a==='toggle')toggle(id);if(a==='add')addChild(id);if(a==='indent')indent(id);if(a==='outdent')outdent(id);if(a==='remove')removeNode(id)}})}
function outline(n,level=0){let text='  '.repeat(level)+'- '+n.title+(n.note?'：'+n.note:'')+'\n';(n.children||[]).forEach(c=>{text+=outline(c,level+1)});return text}
function copyOutline(){const text=outline(root);if(navigator.clipboard){navigator.clipboard.writeText(text).then(()=>toast('大纲已复制')).catch(()=>toast(text))}else{toast(text)}}
function resetMap(){root=seed();save();toast('已恢复示例')}
function esc(s){return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/"/g,'&quot;')}
function toast(t){const el=document.getElementById('toast');el.textContent=t;el.classList.add('show');setTimeout(()=>el.classList.remove('show'),1500)}
document.getElementById('addRoot').addEventListener('click',()=>addChild(root.id));
document.getElementById('copy').addEventListener('click',copyOutline);
document.getElementById('reset').addEventListener('click',resetMap);
document.getElementById('quickAdd').addEventListener('click',quickAdd);
document.getElementById('quick').addEventListener('keydown',e=>{if(e.key==='Enter')quickAdd()});
render();
</script>
</body>
</html>
""".trimIndent()

private val eventHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>活动筹备助手</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f4f7fb;color:#17233c;min-height:100vh;padding:18px 18px 26px}
.hero{background:linear-gradient(135deg,#1a73e8,#13c2c2);color:#fff;border-radius:22px;padding:22px;margin-bottom:16px;box-shadow:0 10px 28px rgba(26,115,232,.24)}
.hero h1{font-size:24px;margin-bottom:6px}.hero p{font-size:13px;opacity:.9;line-height:1.5}
.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-top:18px}.stat{background:rgba(255,255,255,.18);border-radius:14px;padding:12px;text-align:center}.stat b{display:block;font-size:20px}.stat span{font-size:11px;opacity:.86}
.tabs{display:flex;gap:8px;margin:14px 0}.tab{flex:1;border:0;border-radius:12px;padding:10px 4px;background:#fff;color:#667085;font-weight:800}.tab.active{background:#1a73e8;color:#fff}
.panel{display:none;background:#fff;border-radius:18px;padding:16px;box-shadow:0 3px 14px rgba(16,24,40,.06)}.panel.active{display:block}
.row{display:flex;gap:8px;margin-bottom:10px}input{min-width:0;flex:1;border:1px solid #d9e2ef;border-radius:12px;padding:12px;font-size:14px;outline:none;background:#fbfdff}input:focus{border-color:#1a73e8}
button{min-height:44px}.primary{border:0;border-radius:12px;background:#1a73e8;color:#fff;padding:0 14px;font-weight:800}
.item{display:flex;align-items:center;gap:10px;background:#f7faff;border-radius:14px;padding:12px;margin-top:9px}.item-main{flex:1;min-width:0}.item-main b{font-size:15px}.item-main p{font-size:12px;color:#52627a;margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.chip{font-size:12px;border-radius:999px;background:#e8f1ff;color:#1a73e8;padding:5px 9px}.done{opacity:.55}.done b{text-decoration:line-through}.danger{border:0;background:transparent;color:#98a2b3;font-size:20px;padding:0 4px}.empty{color:#667085;text-align:center;padding:28px 10px;font-size:14px}.money{font-weight:800;color:#0f9f6e}
</style>
</head>
<body>
<section class="hero">
<h1>活动筹备助手</h1>
<p>给校园活动团队的一屏式工具：报名、分工、预算、进度都放在手机里。</p>
<div class="stats">
<div class="stat"><b id="peopleCount">0</b><span>报名人数</span></div>
<div class="stat"><b id="taskCount">0</b><span>待办任务</span></div>
<div class="stat"><b id="budgetTotal">¥0</b><span>预算支出</span></div>
</div>
</section>
<nav class="tabs">
<button class="tab active" data-tab="people">报名</button>
<button class="tab" data-tab="tasks">分工</button>
<button class="tab" data-tab="budget">预算</button>
</nav>
<section id="people" class="panel active"><div class="row"><input id="personName" placeholder="姓名"><input id="personPhone" placeholder="电话/微信"></div><button class="primary" style="width:100%" id="addPerson">添加报名</button><div id="peopleList"></div></section>
<section id="tasks" class="panel"><div class="row"><input id="taskName" placeholder="任务，如：借教室"><input id="ownerName" placeholder="负责人"></div><button class="primary" style="width:100%" id="addTask">添加分工</button><div id="taskList"></div></section>
<section id="budget" class="panel"><div class="row"><input id="budgetName" placeholder="支出项目"><input id="budgetAmount" type="number" placeholder="金额"></div><button class="primary" style="width:100%" id="addBudget">记录预算</button><div id="budgetList"></div></section>
<script>
let state=JSON.parse(localStorage.getItem('eventHelper')||'{"people":[],"tasks":[],"budgets":[]}');
function save(){localStorage.setItem('eventHelper',JSON.stringify(state));render()}
function showTab(id,btn){document.querySelectorAll('.panel').forEach(p=>p.classList.remove('active'));document.getElementById(id).classList.add('active');document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));btn.classList.add('active')}
function addPerson(){const n=document.getElementById('personName').value.trim(),p=document.getElementById('personPhone').value.trim();if(!n)return;state.people.unshift({id:Date.now(),name:n,phone:p});document.getElementById('personName').value='';document.getElementById('personPhone').value='';save()}
function addTask(){const n=document.getElementById('taskName').value.trim(),o=document.getElementById('ownerName').value.trim();if(!n)return;state.tasks.unshift({id:Date.now(),name:n,owner:o||'待分配',done:false});document.getElementById('taskName').value='';document.getElementById('ownerName').value='';save()}
function addBudget(){const n=document.getElementById('budgetName').value.trim(),a=parseFloat(document.getElementById('budgetAmount').value);if(!n||!a)return;state.budgets.unshift({id:Date.now(),name:n,amount:a});document.getElementById('budgetName').value='';document.getElementById('budgetAmount').value='';save()}
function del(type,id){state[type]=state[type].filter(x=>x.id!==id);save()}
function toggleTask(id){const t=state.tasks.find(x=>x.id===id);if(t)t.done=!t.done;save()}
function render(){const total=state.budgets.reduce((s,x)=>s+x.amount,0),open=state.tasks.filter(t=>!t.done).length;document.getElementById('peopleCount').textContent=state.people.length;document.getElementById('taskCount').textContent=open;document.getElementById('budgetTotal').textContent='¥'+total.toFixed(0);renderList('peopleList',state.people,'people');renderList('taskList',state.tasks,'tasks');renderList('budgetList',state.budgets,'budgets')}
function renderList(id,items,type){const el=document.getElementById(id);if(!items.length){el.innerHTML='<div class="empty">暂无记录</div>';return}el.innerHTML='';items.forEach(x=>{const div=document.createElement('div');div.className='item';const main=document.createElement('div');main.className='item-main';const b=document.createElement('b');const p=document.createElement('p');const chip=document.createElement('span');chip.className='chip';if(type==='people'){b.textContent=x.name;p.textContent=x.phone;chip.textContent='已报名'}if(type==='tasks'){if(x.done)div.classList.add('done');b.textContent=x.name;p.textContent='负责人：'+x.owner;chip.textContent=x.done?'完成':'待办';main.addEventListener('click',()=>toggleTask(x.id))}if(type==='budgets'){b.textContent=x.name;p.textContent='活动预算记录';chip.className='money';chip.textContent='¥'+x.amount.toFixed(0)}main.appendChild(b);main.appendChild(p);const delBtn=document.createElement('button');delBtn.className='danger';delBtn.textContent='×';delBtn.addEventListener('click',()=>del(type,x.id));div.appendChild(main);div.appendChild(chip);div.appendChild(delBtn);el.appendChild(div)})}
document.querySelectorAll('.tab').forEach(btn=>btn.addEventListener('click',()=>showTab(btn.dataset.tab,btn)));
document.getElementById('addPerson').addEventListener('click',addPerson);
document.getElementById('addTask').addEventListener('click',addTask);
document.getElementById('addBudget').addEventListener('click',addBudget);
render();
</script>
</body>
</html>
""".trimIndent()
