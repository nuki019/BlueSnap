package com.example.mycreate.ai

import com.example.mycreate.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockAiService : AiService {

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage {
        delay(800)
        val lastUser = messages.lastOrNull { it.role == Role.USER }?.content ?: ""
        val detected = detectAppType(lastUser)
        val reply = if (detected != null) {
            "好的！我来帮你做一个「${detected.first}」。\n\n" +
            "${detected.second}\n\n" +
            "我已经为你规划好了方案，点击下方按钮查看详细设计，确认后即可一键生成。"
        } else {
            "收到你的需求！让我来分析一下：\n\n" +
            "根据你的描述，我理解你需要一个自定义工具。我会基于蓝心大模型的能力，" +
            "为你设计功能方案并生成可直接运行的应用。\n\n" +
            "你也可以试试这些常见需求：番茄钟、待办清单、备忘录、记账本、习惯打卡"
        }
        return ChatMessage(role = Role.ASSISTANT, content = reply)
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val lastUser = messages.lastOrNull { it.role == Role.USER }?.content ?: ""
        val detected = detectAppType(lastUser)
        val fullText = if (detected != null) {
            "好的！我来帮你做一个「${detected.first}」。\n\n" +
            "${detected.second}\n\n" +
            "我已经为你规划好了方案，点击下方按钮查看详细设计，确认后即可一键生成。"
        } else {
            "收到你的需求！让我来分析一下：\n\n" +
            "根据你的描述，我理解你需要一个自定义工具。我会基于蓝心大模型的能力，" +
            "为你设计功能方案并生成可直接运行的应用。\n\n" +
            "你也可以试试这些常见需求：番茄钟、待办清单、备忘录、记账本、习惯打卡"
        }
        // 模拟逐字输出
        val buffer = StringBuilder()
        for (char in fullText) {
            buffer.append(char)
            emit(buffer.toString())
            delay(30)
        }
    }

    override suspend fun generatePlan(chatHistory: List<ChatMessage>): AppPlan {
        delay(500)
        val lastUser = chatHistory.lastOrNull { it.role == Role.USER }?.content ?: ""
        val detected = detectAppType(lastUser)
        return detected?.let { plans[it.first] } ?: plans["通用工具"]!!
    }

    override suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String {
        delay(1200)
        return htmlTemplates[plan.name] ?: htmlTemplates["通用工具"]!!
    }

    // ── 意图识别 ──────────────────────────────────────────────────────

    private fun detectAppType(input: String): Pair<String, String>? {
        val lower = input.lowercase()
        return when {
            lower.contains("番茄") || lower.contains("pomodoro") || lower.contains("计时") || lower.contains("倒计时") ->
                "番茄钟" to "这是一个25分钟番茄工作法计时器，支持专注倒计时、白噪音、每日专注统计等功能。"
            lower.contains("待办") || lower.contains("todo") || lower.contains("清单") || lower.contains("任务") ->
                "待办清单" to "这是一个任务管理工具，支持添加、完成、删除待办事项，数据本地持久化保存。"
            lower.contains("备忘") || lower.contains("记事") || lower.contains("笔记") || lower.contains("memo") ->
                "备忘录" to "这是一个轻量备忘录应用，支持快速记录、搜索、本地存储。"
            lower.contains("记账") || lower.contains("账本") || lower.contains("消费") || lower.contains("花销") ->
                "记账本" to "这是一个个人记账工具，支持收入/支出记录、分类统计、月度汇总。"
            lower.contains("打卡") || lower.contains("习惯") || lower.contains("habit") || lower.contains("坚持") ->
                "习惯打卡" to "这是一个习惯养成工具，支持多习惯管理、连续打卡天数追踪、日历视图。"
            else -> null
        }
    }

    // ── 方案库 ────────────────────────────────────────────────────────

    private val plans = mapOf(
        "番茄钟" to AppPlan(
            name = "番茄钟",
            description = "25分钟专注工作法计时器",
            features = listOf(
                Feature("25分钟倒计时", "核心计时功能，到时自动提醒"),
                Feature("白噪音播放", "提供雨声/海浪/篝火三种环境音"),
                Feature("专注统计", "记录每日专注次数和总时长"),
                Feature("自定义时长", "支持调整单次专注时长")
            )
        ),
        "待办清单" to AppPlan(
            name = "待办清单",
            description = "简洁高效的任务管理工具",
            features = listOf(
                Feature("添加待办", "输入任务内容快速添加"),
                Feature("完成标记", "点击勾选完成状态"),
                Feature("删除任务", "左滑删除不需要的任务"),
                Feature("本地存储", "关闭后数据不丢失")
            )
        ),
        "备忘录" to AppPlan(
            name = "备忘录",
            description = "轻量快速的记录工具",
            features = listOf(
                Feature("快速记录", "支持多条备忘录创建"),
                Feature("内容搜索", "关键词快速检索"),
                Feature("时间排序", "按创建时间自动排列"),
                Feature("本地存储", "所有数据安全保存在本地")
            )
        ),
        "记账本" to AppPlan(
            name = "记账本",
            description = "个人收支记录与统计工具",
            features = listOf(
                Feature("收支记录", "快速录入金额和分类"),
                Feature("分类统计", "按类别查看消费占比"),
                Feature("月度汇总", "查看本月收支总览"),
                Feature("历史记录", "查看所有记账记录")
            )
        ),
        "习惯打卡" to AppPlan(
            name = "习惯打卡",
            description = "好习惯养成追踪工具",
            features = listOf(
                Feature("习惯管理", "创建多个自定义习惯"),
                Feature("每日打卡", "一键完成当日打卡"),
                Feature("连续天数", "追踪连续打卡天数"),
                Feature("日历视图", "日历上直观查看打卡记录")
            )
        ),
        "通用工具" to AppPlan(
            name = "通用工具",
            description = "自定义轻量工具",
            features = listOf(
                Feature("核心功能", "基于你的需求自动生成"),
                Feature("简洁界面", "清爽直观的移动端界面"),
                Feature("数据存储", "本地持久化保存"),
                Feature("一键生成", "即开即用无需安装")
            )
        )
    )

    // ── HTML模板库 ─────────────────────────────────────────────────────

    private val htmlTemplates = mapOf(
        "番茄钟" to pomodoroHtml,
        "待办清单" to todoHtml,
        "备忘录" to memoHtml,
        "记账本" to expenseHtml,
        "习惯打卡" to habitHtml,
        "通用工具" to genericHtml
    )
}

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：番茄钟
// ═══════════════════════════════════════════════════════════════════════
private val pomodoroHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>番茄钟</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;display:flex;flex-direction:column;align-items:center;color:#fff;padding:20px}
h1{font-size:22px;margin-bottom:8px;letter-spacing:2px}
.subtitle{font-size:13px;opacity:.7;margin-bottom:30px}
.timer-ring{width:260px;height:260px;border-radius:50%;background:rgba(255,255,255,.1);backdrop-filter:blur(10px);display:flex;align-items:center;justify-content:center;margin-bottom:30px;box-shadow:0 8px 32px rgba(0,0,0,.2)}
.time{font-size:56px;font-weight:200;letter-spacing:4px}
.controls{display:flex;gap:16px;margin-bottom:30px}
.btn{padding:14px 32px;border:none;border-radius:50px;font-size:16px;cursor:pointer;transition:all .3s}
.btn-primary{background:#fff;color:#764ba2;font-weight:600}
.btn-secondary{background:rgba(255,255,255,.2);color:#fff}
.btn:active{transform:scale(.95)}
.stats{background:rgba(255,255,255,.1);border-radius:16px;padding:20px;width:100%;max-width:320px;backdrop-filter:blur(10px)}
.stat-row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid rgba(255,255,255,.1)}
.stat-row:last-child{border:none}
.stat-label{opacity:.8;font-size:14px}
.stat-value{font-weight:600;font-size:14px}
.sound-bar{display:flex;gap:12px;margin-bottom:20px}
.sound-btn{padding:8px 16px;border:1px solid rgba(255,255,255,.3);border-radius:20px;background:transparent;color:#fff;font-size:13px;cursor:pointer}
.sound-btn.active{background:rgba(255,255,255,.25);border-color:#fff}
</style>
</head>
<body>
<h1>🍅 番茄钟</h1>
<p class="subtitle">专注25分钟，高效一整天</p>
<div class="timer-ring">
<div class="time" id="timer">25:00</div>
</div>
<div class="sound-bar">
<button class="sound-btn" onclick="setSound('none')">静音</button>
<button class="sound-btn" onclick="setSound('rain')">🌧 雨声</button>
<button class="sound-btn" onclick="setSound('waves')">🌊 海浪</button>
<button class="sound-btn" onclick="setSound('fire')">🔥 篝火</button>
</div>
<div class="controls">
<button class="btn btn-primary" id="startBtn" onclick="toggleTimer()">开始专注</button>
<button class="btn btn-secondary" onclick="resetTimer()">重置</button>
</div>
<div class="stats">
<div class="stat-row"><span class="stat-label">今日专注次数</span><span class="stat-value" id="sessions">0 次</span></div>
<div class="stat-row"><span class="stat-label">今日专注时长</span><span class="stat-value" id="totalTime">0 分钟</span></div>
<div class="stat-row"><span class="stat-label">当前模式</span><span class="stat-value" id="mode">专注模式</span></div>
</div>
<script>
let time=25*60,running=false,interval=null,sessions=0,totalMinutes=0,sound='none';
const timerEl=document.getElementById('timer'),startBtn=document.getElementById('startBtn');
function updateDisplay(){const m=Math.floor(time/60),s=time%60;timerEl.textContent=(m<10?'0':'')+m+':'+(s<10?'0':'')+s}
function toggleTimer(){if(running){clearInterval(interval);running=false;startBtn.textContent='继续专注'}else{running=true;startBtn.textContent='暂停';interval=setInterval(()=>{if(time>0){time--;updateDisplay()}else{clearInterval(interval);running=false;sessions++;totalMinutes+=25;document.getElementById('sessions').textContent=sessions+' 次';document.getElementById('totalTime').textContent=totalMinutes+' 分钟';startBtn.textContent='开始专注';time=25*60;updateDisplay();if(navigator.vibrate)navigator.vibrate(500)}},1000)}}
function resetTimer(){clearInterval(interval);running=false;time=25*60;updateDisplay();startBtn.textContent='开始专注'}
function setSound(s){sound=s;document.querySelectorAll('.sound-btn').forEach(b=>b.classList.remove('active'));event.target.classList.add('active')}
updateDisplay();
</script>
</body>
</html>
""".trimIndent()

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：待办清单
// ═══════════════════════════════════════════════════════════════════════
private val todoHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>待办清单</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:#f5f5f5;min-height:100vh;padding:20px;padding-bottom:100px}
.header{display:flex;align-items:center;justify-content:space-between;margin-bottom:24px}
h1{font-size:24px;color:#1a1a2e}
.count{background:#1a73e8;color:#fff;padding:4px 12px;border-radius:12px;font-size:13px}
.input-bar{position:fixed;bottom:0;left:0;right:0;padding:16px 20px;background:#fff;box-shadow:0 -2px 12px rgba(0,0,0,.08);display:flex;gap:12px}
.input-bar input{flex:1;border:2px solid #e8e8e8;border-radius:12px;padding:12px 16px;font-size:15px;outline:none;transition:border .3s}
.input-bar input:focus{border-color:#1a73e8}
.input-bar button{background:#1a73e8;color:#fff;border:none;border-radius:12px;padding:12px 20px;font-size:15px;font-weight:600;cursor:pointer}
.todo-item{background:#fff;border-radius:12px;padding:16px;margin-bottom:10px;display:flex;align-items:center;gap:12px;box-shadow:0 1px 4px rgba(0,0,0,.06);transition:all .3s;animation:slideIn .3s ease}
.todo-item.done{opacity:.5}
.todo-item.done .todo-text{text-decoration:line-through;color:#999}
.checkbox{width:24px;height:24px;border-radius:50%;border:2px solid #ddd;cursor:pointer;display:flex;align-items:center;justify-content:center;flex-shrink:0;transition:all .3s}
.checkbox.checked{background:#1a73e8;border-color:#1a73e8}
.checkbox.checked::after{content:'✓';color:#fff;font-size:14px}
.todo-text{flex:1;font-size:15px;color:#333}
.delete-btn{color:#ccc;font-size:18px;cursor:pointer;padding:4px 8px}
.delete-btn:hover{color:#e74c3c}
.empty{text-align:center;padding:60px 20px;color:#999}
.empty-icon{font-size:48px;margin-bottom:12px}
@keyframes slideIn{from{opacity:0;transform:translateY(-10px)}to{opacity:1;transform:translateY(0)}}
</style>
</head>
<body>
<div class="header">
<h1>📝 待办清单</h1>
<span class="count" id="count">0 项</span>
</div>
<div id="list"></div>
<div style="text-align:center;padding:40px;color:#999" id="empty">
<div class="empty-icon">📋</div>
<p>还没有待办事项</p>
<p style="font-size:13px;margin-top:8px">在下方输入框添加你的第一个任务</p>
</div>
<div class="input-bar">
<input id="input" placeholder="添加新任务..." onkeypress="if(event.key==='Enter')addTodo()">
<button onclick="addTodo()">添加</button>
</div>
<script>
let todos=JSON.parse(localStorage.getItem('todos')||'[]');
function save(){localStorage.setItem('todos',JSON.stringify(todos));render()}
function addTodo(){const v=document.getElementById('input').value.trim();if(!v)return;todos.unshift({text:v,done:false,id:Date.now()});document.getElementById('input').value='';save()}
function toggle(id){const t=todos.find(t=>t.id===id);if(t)t.done=!t.done;save()}
function del(id){todos=todos.filter(t=>t.id!==id);save()}
function render(){var list=document.getElementById('list'),empty=document.getElementById('empty'),count=document.getElementById('count');list.innerHTML='';var active=todos.filter(function(x){return !x.done});count.textContent=active.length+' 项';empty.style.display=todos.length?'none':'block';todos.forEach(function(t){var d=document.createElement('div');d.className='todo-item'+(t.done?' done':'');d.innerHTML='<div class="checkbox '+(t.done?'checked':'')+'" onclick="toggle('+t.id+')"></div><span class="todo-text">'+t.text+'</span><span class="delete-btn" onclick="del('+t.id+')">×</span>';list.appendChild(d)})}
render();
</script>
</body>
</html>
""".trimIndent()

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：备忘录
// ═══════════════════════════════════════════════════════════════════════
private val memoHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>备忘录</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:#fafafa;min-height:100vh;padding:20px;padding-bottom:80px}
h1{font-size:24px;color:#333;margin-bottom:16px}
.search{width:100%;padding:12px 16px;border:2px solid #e8e8e8;border-radius:12px;font-size:14px;margin-bottom:16px;outline:none;background:#fff}
.search:focus{border-color:#4CAF50}
.add-btn{position:fixed;bottom:24px;right:24px;width:56px;height:56px;border-radius:50%;background:#4CAF50;color:#fff;border:none;font-size:28px;cursor:pointer;box-shadow:0 4px 12px rgba(76,175,80,.4)}
.memo-card{background:#fff;border-radius:12px;padding:16px;margin-bottom:10px;box-shadow:0 1px 4px rgba(0,0,0,.06);position:relative;animation:fadeIn .3s}
.memo-title{font-size:16px;font-weight:600;color:#333;margin-bottom:6px}
.memo-body{font-size:14px;color:#666;line-height:1.5;max-height:60px;overflow:hidden}
.memo-time{font-size:12px;color:#999;margin-top:8px}
.memo-del{position:absolute;top:12px;right:12px;color:#ccc;cursor:pointer;font-size:18px}
.modal{display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:10;align-items:center;justify-content:center}
.modal.show{display:flex}
.modal-content{background:#fff;border-radius:16px;padding:24px;width:90%;max-width:400px}
.modal-content input{width:100%;padding:12px;border:2px solid #e8e8e8;border-radius:8px;font-size:16px;margin-bottom:12px;outline:none}
.modal-content textarea{width:100%;padding:12px;border:2px solid #e8e8e8;border-radius:8px;font-size:14px;height:150px;resize:none;outline:none;margin-bottom:16px}
.modal-btns{display:flex;gap:12px;justify-content:flex-end}
.modal-btns button{padding:10px 20px;border:none;border-radius:8px;font-size:14px;cursor:pointer}
.btn-save{background:#4CAF50;color:#fff}
.btn-cancel{background:#eee;color:#666}
@keyframes fadeIn{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:translateY(0)}}
</style>
</head>
<body>
<h1>📒 备忘录</h1>
<input class="search" placeholder="搜索备忘录..." oninput="render(this.value)">
<div id="list"></div>
<button class="add-btn" onclick="openModal()">+</button>
<div class="modal" id="modal">
<div class="modal-content">
<input id="titleInput" placeholder="标题">
<textarea id="bodyInput" placeholder="写下你的想法..."></textarea>
<div class="modal-btns">
<button class="btn-cancel" onclick="closeModal()">取消</button>
<button class="btn-save" onclick="saveMemo()">保存</button>
</div>
</div>
</div>
<script>
let memos=JSON.parse(localStorage.getItem('memos')||'[]');
function openModal(){document.getElementById('modal').classList.add('show');document.getElementById('titleInput').value='';document.getElementById('bodyInput').value=''}
function closeModal(){document.getElementById('modal').classList.remove('show')}
function saveMemo(){const t=document.getElementById('titleInput').value.trim()||'未命名',b=document.getElementById('bodyInput').value.trim();if(!b)return;memos.unshift({title:t,body:b,time:new Date().toLocaleString('zh-CN'),id:Date.now()});localStorage.setItem('memos',JSON.stringify(memos));closeModal();render()}
function delMemo(id){memos=memos.filter(m=>m.id!==id);localStorage.setItem('memos',JSON.stringify(memos));render()}
function render(q){q=q||'';var list=document.getElementById('list');list.innerHTML='';var filtered=q?memos.filter(function(x){return x.title.includes(q)||x.body.includes(q)}):memos;filtered.forEach(function(m){list.innerHTML+='<div class="memo-card"><span class="memo-del" onclick="delMemo('+m.id+')">×</span><div class="memo-title">'+m.title+'</div><div class="memo-body">'+m.body+'</div><div class="memo-time">'+m.time+'</div></div>'})}
render();
</script>
</body>
</html>
""".trimIndent()

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：记账本
// ═══════════════════════════════════════════════════════════════════════
private val expenseHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>记账本</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:#f0f2f5;min-height:100vh;padding-bottom:80px}
.hero{background:linear-gradient(135deg,#00b4db,#0083b0);padding:30px 20px 40px;color:#fff;text-align:center;border-radius:0 0 24px 24px}
.hero h1{font-size:22px;margin-bottom:16px}
.balance{font-size:36px;font-weight:200;margin-bottom:4px}
.balance-label{font-size:13px;opacity:.7}
.row{display:flex;justify-content:center;gap:40px;margin-top:16px}
.row-item{text-align:center}
.row-amount{font-size:20px;font-weight:600}
.row-label{font-size:12px;opacity:.7}
.tabs{display:flex;padding:16px;gap:12px}
.tab{flex:1;padding:10px;border:none;border-radius:10px;font-size:14px;cursor:pointer;background:#fff;color:#666}
.tab.active{background:#0083b0;color:#fff}
.item{background:#fff;margin:0 16px 8px;padding:14px 16px;border-radius:12px;display:flex;align-items:center;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.item-icon{width:40px;height:40px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:20px;margin-right:12px}
.item-info{flex:1}
.item-cat{font-size:15px;font-weight:500;color:#333}
.item-note{font-size:12px;color:#999}
.item-amount{font-size:16px;font-weight:600}
.item-amount.expense{color:#e74c3c}
.item-amount.income{color:#27ae60}
.item-time{font-size:12px;color:#bbb;margin-left:8px}
.add-bar{position:fixed;bottom:0;left:0;right:0;padding:16px;background:#fff;box-shadow:0 -2px 12px rgba(0,0,0,.08);display:flex;gap:10px}
.add-bar select,.add-bar input{border:2px solid #e8e8e8;border-radius:10px;padding:10px;font-size:14px;outline:none}
.add-bar select{width:70px}
.add-bar input{flex:1}
.add-bar button{background:#0083b0;color:#fff;border:none;border-radius:10px;padding:10px 18px;font-size:14px;font-weight:600;cursor:pointer}
</style>
</head>
<body>
<div class="hero">
<h1>💰 记账本</h1>
<div class="balance" id="balance">¥0.00</div>
<div class="balance-label">本月结余</div>
<div class="row">
<div class="row-item"><div class="row-amount" id="income">¥0</div><div class="row-label">收入</div></div>
<div class="row-item"><div class="row-amount" id="expense">¥0</div><div class="row-label">支出</div></div>
</div>
</div>
<div class="tabs">
<button class="tab active" onclick="filter='all';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">全部</button>
<button class="tab" onclick="filter='expense';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">支出</button>
<button class="tab" onclick="filter='income';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">收入</button>
</div>
<div id="list"></div>
<div class="add-bar">
<select id="type"><option value="expense">支出</option><option value="income">收入</option></select>
<select id="cat"><option>餐饮</option><option>交通</option><option>购物</option><option>娱乐</option><option>工资</option><option>其他</option></select>
<input id="amount" type="number" placeholder="金额" step="0.01">
<button onclick="add()">记录</button>
</div>
<script>
let records=JSON.parse(localStorage.getItem('records')||'[]'),filter='all';
const icons={餐饮:'🍔',交通:'🚌',购物:'🛒',娱乐:'🎮',工资:'💼',其他:'📌'};
function add(){const type=document.getElementById('type').value,cat=document.getElementById('cat').value,amount=parseFloat(document.getElementById('amount').value);if(!amount||amount<=0)return;records.unshift({type,cat,amount,time:new Date().toLocaleDateString('zh-CN'),id:Date.now()});localStorage.setItem('records',JSON.stringify(records));document.getElementById('amount').value='';render()}
function del(id){records=records.filter(r=>r.id!==id);localStorage.setItem('records',JSON.stringify(records));render()}
function render(){var list=document.getElementById('list');list.innerHTML='';var inc=0,exp=0;var filtered=filter==='all'?records:records.filter(function(r){return r.type===filter});filtered.forEach(function(r){if(r.type==='income')inc+=r.amount;else exp+=r.amount;list.innerHTML+='<div class="item"><div class="item-icon">'+(icons[r.cat]||'📌')+'</div><div class="item-info"><div class="item-cat">'+r.cat+'</div></div><span class="item-amount '+r.type+'">'+(r.type==='expense'?'-':'+')+'¥'+r.amount.toFixed(2)+'</span><span class="item-time">'+r.time+'</span><span style="cursor:pointer;color:#ccc;margin-left:8px" onclick="del('+r.id+')">×</span></div>'});document.getElementById('balance').textContent='¥'+(inc-exp).toFixed(2);document.getElementById('income').textContent='¥'+inc.toFixed(0);document.getElementById('expense').textContent='¥'+exp.toFixed(0)}
render();
</script>
</body>
</html>
""".trimIndent()

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：习惯打卡
// ═══════════════════════════════════════════════════════════════════════
private val habitHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>习惯打卡</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:#f5f0eb;min-height:100vh;padding:20px;padding-bottom:80px}
h1{font-size:24px;color:#333;margin-bottom:4px}
.sub{font-size:13px;color:#999;margin-bottom:20px}
.habit-card{background:#fff;border-radius:16px;padding:20px;margin-bottom:12px;box-shadow:0 2px 8px rgba(0,0,0,.04)}
.habit-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.habit-name{font-size:17px;font-weight:600;color:#333}
.habit-streak{font-size:13px;color:#ff6b6b;background:#fff0f0;padding:4px 10px;border-radius:8px}
.habit-bar{height:8px;background:#f0f0f0;border-radius:4px;overflow:hidden;margin-bottom:12px}
.habit-bar-fill{height:100%;border-radius:4px;transition:width .3s}
.check-row{display:flex;gap:6px}
.check-day{width:36px;height:36px;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:11px;color:#999;background:#f8f8f8}
.check-day.done{background:#4CAF50;color:#fff}
.check-day.today{border:2px solid #4CAF50}
.check-btn{width:100%;padding:14px;border:none;border-radius:12px;font-size:15px;font-weight:600;cursor:pointer;margin-top:12px;transition:all .3s}
.check-btn.active{background:#4CAF50;color:#fff}
.check-btn.done{background:#e8f5e9;color:#4CAF50}
.add-area{position:fixed;bottom:0;left:0;right:0;padding:16px;background:#fff;box-shadow:0 -2px 12px rgba(0,0,0,.08);display:flex;gap:12px}
.add-area input{flex:1;border:2px solid #e8e8e8;border-radius:12px;padding:12px 16px;font-size:14px;outline:none}
.add-area button{background:#ff6b6b;color:#fff;border:none;border-radius:12px;padding:12px 20px;font-size:14px;font-weight:600;cursor:pointer}
@keyframes pop{0%{transform:scale(1)}50%{transform:scale(1.2)}100%{transform:scale(1)}}
.pop{animation:pop .3s ease}
</style>
</head>
<body>
<h1>🔥 习惯打卡</h1>
<p class="sub" id="dateSub"></p>
<div id="list"></div>
<div class="add-area">
<input id="newHabit" placeholder="添加新习惯..." onkeypress="if(event.key==='Enter')addHabit()">
<button onclick="addHabit()">添加</button>
</div>
<script>
let habits=JSON.parse(localStorage.getItem('habits')||'[]');
const colors=['#4CAF50','#2196F3','#ff9800','#9c27b0','#e91e63','#00bcd4'];
const today=new Date().toISOString().slice(0,10);
document.getElementById('dateSub').textContent=new Date().toLocaleDateString('zh-CN',{year:'numeric',month:'long',day:'numeric',weekday:'long'});
function getStreak(h){let s=0,d=new Date();for(let i=0;i<365;i++){const k=d.toISOString().slice(0,10);if(h.days&&h.days[k]){s++}else if(i>0)break;d.setDate(d.getDate()-1)}return s}
function addHabit(){const v=document.getElementById('newHabit').value.trim();if(!v)return;habits.push({name:v,days:{},color:colors[habits.length%colors.length],id:Date.now()});document.getElementById('newHabit').value='';save()}
function toggleDay(id){const h=habits.find(h=>h.id===id);if(!h)return;if(!h.days)h.days={};if(h.days[today])delete h.days[today];else h.days[today]=true;save()}
function delHabit(id){habits=habits.filter(h=>h.id!==id);save()}
function save(){localStorage.setItem('habits',JSON.stringify(habits));render()}
function render(){const list=document.getElementById('list');list.innerHTML='';if(!habits.length){list.innerHTML='<div style="text-align:center;padding:60px;color:#999"><div style="font-size:48px;margin-bottom:12px">🌱</div><p>还没有习惯，添加一个开始吧</p></div>';return}
habits.forEach(function(h){var streak=getStreak(h),doneToday=h.days&&h.days[today],weekDays=['一','二','三','四','五','六','日'],d=new Date(),dayOfWeek=d.getDay(),weekHtml=weekDays.map(function(w,i){var dd=new Date(d);dd.setDate(dd.getDate()-(dayOfWeek===0?6:dayOfWeek-1)+i);var k=dd.toISOString().slice(0,10),isToday=k===today,isDone=h.days&&h.days[k];return'<div class="check-day'+(isDone?' done':'')+(isToday?' today':'')+'">'+w+'</div>'}).join('');list.innerHTML+='<div class="habit-card"><div class="habit-header"><span class="habit-name">'+h.name+'</span><span class="habit-streak">🔥 连续'+streak+'天</span></div><div class="check-row">'+weekHtml+'</div><button class="check-btn '+(doneToday?'done':'active')+'" onclick="toggleDay('+h.id+')">'+(doneToday?'✅ 今日已打卡':'打卡')+'</button><div style="text-align:right;margin-top:8px"><span style="font-size:12px;color:#ccc;cursor:pointer" onclick="delHabit('+h.id+')">删除</span></div></div>'})}
render();
</script>
</body>
</html>
""".trimIndent()

// ═══════════════════════════════════════════════════════════════════════
// HTML 模板：通用工具
// ═══════════════════════════════════════════════════════════════════════
private val genericHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>轻量工具</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,sans-serif;background:linear-gradient(135deg,#a8edea 0%,#fed6e3 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px}
.card{background:#fff;border-radius:20px;padding:40px;text-align:center;box-shadow:0 10px 40px rgba(0,0,0,.1);max-width:360px;width:100%}
.icon{font-size:56px;margin-bottom:16px}
h1{font-size:22px;color:#333;margin-bottom:8px}
p{color:#666;font-size:14px;line-height:1.6;margin-bottom:24px}
.btn{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;border:none;border-radius:12px;padding:14px 32px;font-size:16px;font-weight:600;cursor:pointer;width:100%;margin-bottom:12px}
.counter{font-size:48px;font-weight:200;color:#764ba2;margin:20px 0}
.note{background:#f8f9fa;border-radius:12px;padding:16px;text-align:left;margin-top:16px}
.note textarea{width:100%;border:none;outline:none;resize:none;font-size:14px;height:80px;background:transparent}
</style>
</head>
<body>
<div class="card">
<div class="icon">✨</div>
<h1>蓝心快搭</h1>
<p>这是一个由AI生成的轻量工具。你可以通过对话告诉蓝心快搭你的具体需求，生成更贴合的应用。</p>
<div class="counter" id="count">0</div>
<button class="btn" onclick="document.getElementById('count').textContent=parseInt(document.getElementById('count').textContent)+1">点击计数</button>
<button class="btn" style="background:#f0f0f0;color:#666" onclick="document.getElementById('count').textContent=0">重置</button>
<div class="note">
<textarea placeholder="在这里记录你的想法..."></textarea>
</div>
</div>
</body>
</html>
""".trimIndent()
