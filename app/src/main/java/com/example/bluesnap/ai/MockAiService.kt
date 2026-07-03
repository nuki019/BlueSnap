package com.example.bluesnap.ai

import com.example.bluesnap.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockAiService : AiService {

    override suspend fun chat(messages: List<ChatMessage>): ChatMessage {
        delay(800)
        val lastUser = messages.lastOrNull { it.role == Role.USER }?.content ?: ""
        val detected = detectAppType(lastUser)
        val reply = if (detected != null) {
            "???????????${detected.first}??\n\n" +
            "${detected.second}\n\n" +
            "???????????????????????????????????"
        } else {
            "???????????????\n\n" +
            "??????????????????????????????????" +
            "????????????????????\n\n" +
            "??????????????????????????????????????????????"
        }
        return ChatMessage(role = Role.ASSISTANT, content = reply)
    }

    override fun chatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val lastUser = messages.lastOrNull { it.role == Role.USER }?.content ?: ""
        val detected = detectAppType(lastUser)
        val fullText = if (detected != null) {
            "???????????${detected.first}??\n\n" +
            "${detected.second}\n\n" +
            "???????????????????????????????????"
        } else {
            "???????????????\n\n" +
            "??????????????????????????????????" +
            "????????????????????\n\n" +
            "??????????????????????????????????????????????"
        }
        // ??????
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
        return detected?.let { plans[it.first] } ?: plans["????"]!!
    }

    override suspend fun generateHtml(plan: AppPlan, chatHistory: List<ChatMessage>): String {
        delay(1200)
        return htmlTemplates[plan.name] ?: htmlTemplates["????"]!!
    }

    override suspend fun generateBundle(plan: AppPlan, chatHistory: List<ChatMessage>): GenerationBundle {
        delay(1200)
        val html = htmlTemplates[plan.name] ?: htmlTemplates["????"]!!
        return GenerationBundle(
            html = html,
            summary = plan.description,
            imagePrompt = mediaPrompts[plan.name]?.first,
            audioPrompt = mediaPrompts[plan.name]?.second
        )
    }

    // ?? ???? ??????????????????????????????????????????????????????

    private fun detectAppType(input: String): Pair<String, String>? {
        val lower = input.lowercase()
        return when {
            lower.contains("????") || lower.contains("??") || lower.contains("???") ||
                lower.contains("mindmap") || lower.contains("mind map") ->
                "??????" to "?????????????????????????????????????????????????????"
            lower.contains("??") || lower.contains("??") || lower.contains("??") ||
                lower.contains("??") || lower.contains("??") ->
                "????" to "??????????????????????????????????????????"
            lower.contains("??") || lower.contains("??") || lower.contains("ddl") ||
                lower.contains("??") ->
                "?????" to "??????????????????DDL ??????????"
            lower.contains("??") || lower.contains("??") || lower.contains("??") || lower.contains("???") ->
                "????" to "??????????????????????????????????"
            lower.contains("??") || lower.contains("todo") || lower.contains("??") ->
                "?????" to "??????????????????DDL ??????????"
            lower.contains("??") || lower.contains("??") || lower.contains("??") ||
                lower.contains("??") || lower.contains("??") || lower.contains("??") ->
                "?????" to "??????????????????????????????????"
            lower.contains("??") || lower.contains("??") || lower.contains("??") || lower.contains("memo") ->
                "???" to "???????????????????????????"
            lower.contains("??") || lower.contains("??") || lower.contains("??") ||
                lower.contains("??") || lower.contains("??") || lower.contains("??") ->
                "????" to "??????????????????????????????????"
            lower.contains("??") || lower.contains("??") || lower.contains("habit") || lower.contains("??") ->
                "????" to "?????????????????????????????????"
            lower.contains("??") || lower.contains("pomodoro") || lower.contains("??") || lower.contains("???") ->
                "???" to "????25?????????????????????????????????"
            else -> null
        }
    }

    // ?? ??? ????????????????????????????????????????????????????????

    private val plans = mapOf(
        "????" to AppPlan(
            name = "????",
            description = "???????????????????",
            features = listOf(
                Feature("????", "??????????????"),
                Feature("????", "????????????"),
                Feature("????", "?????????????"),
                Feature("????", "????????????")
            )
        ),
        "?????" to AppPlan(
            name = "?????",
            description = "?????? DDL ???????",
            features = listOf(
                Feature("????", "????????????"),
                Feature("DDL ??", "???????????"),
                Feature("????", "??????????????"),
                Feature("????", "????????????")
            )
        ),
        "????" to AppPlan(
            name = "????",
            description = "???????????????",
            features = listOf(
                Feature("????", "???????????"),
                Feature("????", "??????????"),
                Feature("????", "??????????"),
                Feature("????", "???????????")
            )
        ),
        "?????" to AppPlan(
            name = "?????",
            description = "?????????????",
            features = listOf(
                Feature("????", "????????????"),
                Feature("????", "???????????? offer"),
                Feature("????", "???????????"),
                Feature("????", "????????????")
            )
        ),
        "????" to AppPlan(
            name = "????",
            description = "?????????????",
            features = listOf(
                Feature("????", "????????????"),
                Feature("????", "?????????????"),
                Feature("????", "???????????"),
                Feature("????", "?????????")
            )
        ),
        "??????" to AppPlan(
            name = "??????",
            description = "?????????????????????",
            features = listOf(
                Feature("????", "???????????????"),
                Feature("????", "????????????"),
                Feature("????", "????????????????"),
                Feature("????", "????????????")
            )
        ),
        "???" to AppPlan(
            name = "???",
            description = "25??????????",
            features = listOf(
                Feature("25?????", "?????????????"),
                Feature("?????", "????/??/???????"),
                Feature("????", "????????????"),
                Feature("?????", "??????????")
            )
        ),
        "????" to AppPlan(
            name = "????",
            description = "???????????",
            features = listOf(
                Feature("????", "??????????"),
                Feature("????", "????????"),
                Feature("????", "??????????"),
                Feature("????", "????????")
            )
        ),
        "???" to AppPlan(
            name = "???",
            description = "?????????",
            features = listOf(
                Feature("????", "?????????"),
                Feature("????", "???????"),
                Feature("????", "?????????"),
                Feature("????", "???????????")
            )
        ),
        "???" to AppPlan(
            name = "???",
            description = "???????????",
            features = listOf(
                Feature("????", "?????????"),
                Feature("????", "?????????"),
                Feature("????", "????????"),
                Feature("????", "????????")
            )
        ),
        "????" to AppPlan(
            name = "????",
            description = "?????????",
            features = listOf(
                Feature("????", "?????????"),
                Feature("????", "????????"),
                Feature("????", "????????"),
                Feature("????", "???????????")
            )
        ),
        "????" to AppPlan(
            name = "????",
            description = "???????????",
            features = listOf(
                Feature("????", "??????????"),
                Feature("????", "??????????"),
                Feature("????", "???????"),
                Feature("????", "????????")
            )
        )
    )

    // ?? HTML??? ?????????????????????????????????????????????????????

    private val htmlTemplates = mapOf(
        "????" to eventHtml,
        "?????" to courseBoardHtml,
        "????" to groupWorkHtml,
        "?????" to jobTrackerHtml,
        "????" to lifeBudgetHtml,
        "??????" to mindMapHtml,
        "???" to pomodoroHtml,
        "????" to todoHtml,
        "???" to memoHtml,
        "???" to expenseHtml,
        "????" to habitHtml,
        "????" to genericHtml
    )

    private val mediaPrompts = mapOf(
        "????" to ("??????????????????????????????????????????" to "????????????????????"),
        "??????" to ("????????????????????????????????????????????" to "????????????????????????")
    )
}

private val courseBoardHtml = campusListHtml(
    title = "?????",
    icon = "??",
    subtitle = "?????????? DDL ????????",
    placeholder = "?????????????????",
    seeds = listOf("???? DDL ??", "???? PPT", "??????")
)

private val groupWorkHtml = campusListHtml(
    title = "????",
    icon = "??",
    subtitle = "????????????????????????",
    placeholder = "???????????????",
    seeds = listOf("???? - ??", "PPT ?? - ??", "???? - ??")
)

private val jobTrackerHtml = campusListHtml(
    title = "?????",
    icon = "??",
    subtitle = "??????????????????",
    placeholder = "??????????? ???? ???",
    seeds = listOf("???? - ???", "???? - ????", "???? - ????")
)

private val lifeBudgetHtml = campusListHtml(
    title = "????",
    icon = "??",
    subtitle = "???????????????????",
    placeholder = "????????? 18 ?",
    seeds = listOf("?? 18 ?", "???? 12 ?", "?? 6 ?")
)

private fun campusListHtml(
    title: String,
    icon: String,
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
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f6f8fc;color:#17233c;min-height:100vh;padding:18px}
.hero{background:linear-gradient(135deg,#1a73e8,#13c2c2);color:#fff;border-radius:18px;padding:22px;margin-bottom:16px;box-shadow:0 10px 28px rgba(26,115,232,.2)}
.hero h1{font-size:24px;margin-bottom:6px}.hero p{font-size:13px;opacity:.88;line-height:1.5}
.panel{background:#fff;border-radius:14px;padding:14px;box-shadow:0 8px 24px rgba(15,35,75,.08);margin-bottom:12px}
.row{display:flex;gap:8px}.row input{flex:1;border:1px solid #d9e2f1;border-radius:12px;padding:12px;font-size:14px;outline:none}
.row button{border:0;border-radius:12px;background:#1a73e8;color:#fff;font-weight:700;padding:0 16px}
.item{display:flex;align-items:center;justify-content:space-between;gap:10px;background:#f7f9fd;border-radius:12px;padding:12px;margin-top:10px}
.item.done{opacity:.55}.item.done b{text-decoration:line-through}.item b{font-size:14px}.item span{font-size:12px;color:#6b7890}
.item button{border:0;background:#ffe8e8;color:#d63031;border-radius:10px;width:34px;height:34px;font-size:18px}
.empty{text-align:center;color:#8a96aa;padding:28px 0;font-size:14px}
</style>
</head>
<body>
<div class="hero"><h1>$icon $title</h1><p>$subtitle</p></div>
<div class="panel">
  <div class="row">
    <input id="text" placeholder="$placeholder" onkeypress="if(event.key==='Enter')addItem()">
    <button onclick="addItem()">??</button>
  </div>
  <div id="list"></div>
</div>
<script>
var key='bluesnap_$title';
var items=JSON.parse(localStorage.getItem(key)||'null')||[$seedItems];
function save(){localStorage.setItem(key,JSON.stringify(items))}
function render(){var list=document.getElementById('list');list.innerHTML='';if(!items.length){list.innerHTML='<div class="empty">?????????????</div>';return}items.forEach(function(item,index){var div=document.createElement('div');div.className='item '+(item.done?'done':'');div.innerHTML='<div onclick="toggleItem('+index+')"><b>'+item.text+'</b><br><span>'+(item.done?'???':'???')+'</span></div><button onclick="deleteItem('+index+')">?</button>';list.appendChild(div)})}
function addItem(){var input=document.getElementById('text');var text=input.value.trim();if(!text)return;items.unshift({text:text,done:false});input.value='';save();render()}
function toggleItem(index){items[index].done=!items[index].done;save();render()}
function deleteItem(index){items.splice(index,1);save();render()}
items=items.map(function(x){return typeof x==='string'?{text:x,done:false}:x});save();render();
</script>
</body>
</html>
""".trimIndent()
}

private fun String.escapeJsString(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}

// ???????????????????????????????????????????????????????????????????????
// HTML ?????????
// ???????????????????????????????????????????????????????????????????????
private val mindMapHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>??????</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f5f8fd;color:#17233c;min-height:100vh;padding:16px;padding-bottom:92px}
.hero{background:#fff;border:1px solid #e3eaf6;border-radius:18px;padding:18px;box-shadow:0 8px 24px rgba(16,35,75,.06);margin-bottom:12px}
.eyebrow{display:inline-flex;align-items:center;height:24px;border-radius:999px;background:#e8f1ff;color:#1a73e8;font-size:12px;font-weight:800;padding:0 10px;margin-bottom:10px}
h1{font-size:24px;line-height:1.2;margin-bottom:6px}.sub{font-size:13px;color:#667085;line-height:1.6}
.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:14px}.stat{background:#f7faff;border-radius:12px;padding:10px;text-align:center}.stat b{display:block;font-size:18px}.stat span{font-size:11px;color:#667085}
.toolbar{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin:12px 0}.toolbar button,.bar button{border:0;border-radius:12px;background:#1a73e8;color:#fff;font-weight:800;min-height:42px}
.toolbar button.secondary{background:#eef4ff;color:#1a73e8}.toolbar button.ghost{background:#fff;color:#475467;border:1px solid #dbe5f3}
.panel{background:#fff;border:1px solid #e3eaf6;border-radius:18px;padding:12px;box-shadow:0 8px 24px rgba(16,35,75,.06)}
.node{position:relative;background:#f8fbff;border:1px solid #e2eaf6;border-radius:14px;margin:8px 0;padding:10px 10px 10px 12px}
.node:before{content:"";position:absolute;left:-8px;top:22px;width:8px;height:1px;background:#c8d6eb}
.node.level0{background:linear-gradient(135deg,#1a73e8,#13a8c8);color:#fff;border:0}.node.level0:before{display:none}
.row{display:flex;align-items:center;gap:8px}.title{flex:1;min-width:0;font-size:15px;font-weight:800;border:0;background:transparent;color:inherit;outline:none}
.note{width:100%;border:0;background:transparent;color:inherit;opacity:.72;outline:none;font-size:12px;margin-top:5px;resize:none;min-height:32px}
.badge{font-size:11px;color:#1a73e8;background:#e8f1ff;border-radius:999px;padding:4px 8px;white-space:nowrap}.level0 .badge{background:rgba(255,255,255,.2);color:#fff}
.iconbtn{width:32px;height:32px;border:0;border-radius:10px;background:#eef4ff;color:#1a73e8;font-weight:900}.level0 .iconbtn{background:rgba(255,255,255,.2);color:#fff}
.children{margin-left:18px;border-left:1px solid #d8e3f3;padding-left:10px}.collapsed>.children{display:none}.collapsed>.row .toggle:after{content:"+"}.toggle:after{content:"-"}
.bar{position:fixed;left:0;right:0;bottom:0;background:#fff;border-top:1px solid #e3eaf6;padding:12px 16px;display:flex;gap:8px;box-shadow:0 -8px 24px rgba(16,35,75,.08)}
.bar input{flex:1;min-width:0;border:1px solid #d9e3f0;border-radius:12px;padding:0 12px;font-size:14px;outline:none}.bar input:focus{border-color:#1a73e8}
.toast{position:fixed;left:50%;bottom:78px;transform:translateX(-50%);background:#17233c;color:#fff;padding:10px 14px;border-radius:999px;font-size:13px;opacity:0;pointer-events:none;transition:.2s}.toast.show{opacity:1}
</style>
</head>
<body>
<section class="hero">
  <span class="eyebrow">MIND MAP</span>
  <h1>??????</h1>
  <p class="sub">???????????????????????????????????</p>
  <div class="stats">
    <div class="stat"><b id="total">0</b><span>??</span></div>
    <div class="stat"><b id="depth">0</b><span>??</span></div>
    <div class="stat"><b id="done">0</b><span>???</span></div>
  </div>
</section>
<div class="toolbar">
  <button onclick="addChild(root.id)">????</button>
  <button class="secondary" onclick="copyOutline()">????</button>
  <button class="ghost" onclick="resetMap()">????</button>
</div>
<main class="panel" id="map"></main>
<div class="bar">
  <input id="quick" placeholder="????????????" onkeypress="if(event.key==='Enter')quickAdd()">
  <button onclick="quickAdd()">??</button>
</div>
<div class="toast" id="toast">???</div>
<script>
var storeKey='bluesnap_mind_map_v1';
function seed(){return {id:1,title:'????????',note:'??????????????????',children:[
  {id:2,title:'????',note:'???????????????????',children:[
    {id:5,title:'????',note:'???????????',children:[]},
    {id:6,title:'????',note:'DDL??????????',children:[]}
  ]},
  {id:3,title:'????',note:'???????? HTML',children:[
    {id:7,title:'????',note:'?????????',children:[]},
    {id:8,title:'????',note:'??????? HTML',children:[]}
  ]},
  {id:4,title:'????',note:'?????????????',children:[]}
]}}
var root=load();var selected=root.id;
function load(){try{return JSON.parse(localStorage.getItem(storeKey))||seed()}catch(e){return seed()}}
function save(){localStorage.setItem(storeKey,JSON.stringify(root));render()}
function nextId(){var max=0;walk(root,function(n){if(n.id>max)max=n.id});return max+1}
function walk(n,fn,level,parent){fn(n,level||0,parent||null);(n.children||[]).forEach(function(c){walk(c,fn,(level||0)+1,n)})}
function find(id){var hit=null;walk(root,function(n){if(n.id===id)hit=n});return hit}
function findParent(id){var hit=null;walk(root,function(n,l,p){if(n.id===id)hit=p});return hit}
function addChild(id){var n=find(id)||root;n.children=n.children||[];n.children.push({id:nextId(),title:'???',note:'',children:[]});selected=n.children[n.children.length-1].id;save()}
function quickAdd(){var input=document.getElementById('quick');var text=input.value.trim();if(!text)return;root.children.push({id:nextId(),title:text,note:'',children:[]});input.value='';save();toast('?????')}
function removeNode(id){if(id===root.id)return toast('????????');var p=findParent(id);if(!p)return;p.children=p.children.filter(function(x){return x.id!==id});selected=root.id;save()}
function indent(id){var p=findParent(id);if(!p)return;var list=p.children;var idx=list.findIndex(function(x){return x.id===id});if(idx<=0)return toast('?????????');var node=list.splice(idx,1)[0];list[idx-1].children.push(node);save()}
function outdent(id){var p=findParent(id),gp=p?findParent(p.id):null;if(!p||!gp)return toast('?????');var node=find(id);if(!node)return;p.children=p.children.filter(function(x){return x.id!==id});var parentIndex=gp.children.findIndex(function(x){return x.id===p.id});gp.children.splice(parentIndex+1,0,node);save()}
function toggle(id){var n=find(id);if(!n)return;n.collapsed=!n.collapsed;save()}
function render(){var map=document.getElementById('map');map.innerHTML=nodeHtml(root,0);bindInputs();var count=0,depth=0;walk(root,function(n,l){count++;if(l+1>depth)depth=l+1});document.getElementById('total').textContent=count;document.getElementById('depth').textContent=depth;document.getElementById('done').textContent='HTML'}
function nodeHtml(n,level){var child=(n.children||[]).map(function(c){return nodeHtml(c,level+1)}).join('');return '<section class="node level'+level+(n.collapsed?' collapsed':'')+'" data-id="'+n.id+'"><div class="row"><button class="iconbtn toggle" onclick="toggle('+n.id+')"></button><input class="title" value="'+esc(n.title)+'" data-field="title" data-id="'+n.id+'"><span class="badge">L'+(level+1)+'</span></div><textarea class="note" data-field="note" data-id="'+n.id+'" placeholder="????">'+esc(n.note||'')+'</textarea><div class="row" style="margin-top:8px"><button class="iconbtn" onclick="addChild('+n.id+')">+</button><button class="iconbtn" onclick="indent('+n.id+')">?</button><button class="iconbtn" onclick="outdent('+n.id+')">?</button><button class="iconbtn" onclick="removeNode('+n.id+')">?</button></div><div class="children">'+child+'</div></section>'}
function bindInputs(){document.querySelectorAll('[data-field]').forEach(function(el){el.oninput=function(){var n=find(parseInt(el.dataset.id));if(n){n[el.dataset.field]=el.value;localStorage.setItem(storeKey,JSON.stringify(root))}}})}
function outline(n,level){var pad='  '.repeat(level);var text=pad+'- '+n.title+(n.note?'?'+n.note:'')+'\n';(n.children||[]).forEach(function(c){text+=outline(c,level+1)});return text}
function copyOutline(){var text=outline(root,0);if(navigator.clipboard){navigator.clipboard.writeText(text).then(function(){toast('?????')})}else{toast(text)}}
function resetMap(){root=seed();selected=root.id;save();toast('?????')}
function esc(s){return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/"/g,'&quot;')}
function toast(t){var el=document.getElementById('toast');el.textContent=t;el.classList.add('show');setTimeout(function(){el.classList.remove('show')},1500)}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ???????
// ???????????????????????????????????????????????????????????????????????
private val eventHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>??????</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,"PingFang SC",sans-serif;background:#f4f7fb;color:#17233c;min-height:100vh;padding:18px;padding-bottom:28px}
.hero{background:linear-gradient(135deg,#1a73e8,#13c2c2);color:#fff;border-radius:22px;padding:22px;margin-bottom:16px;box-shadow:0 10px 28px rgba(26,115,232,.24)}
.hero h1{font-size:24px;margin-bottom:6px}
.hero p{font-size:13px;opacity:.86;line-height:1.5}
.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-top:18px}
.stat{background:rgba(255,255,255,.18);border-radius:14px;padding:12px;text-align:center}
.stat b{display:block;font-size:20px;margin-bottom:3px}
.stat span{font-size:11px;opacity:.82}
.tabs{display:flex;gap:8px;margin:14px 0}
.tab{flex:1;border:0;border-radius:12px;padding:10px 4px;background:#fff;color:#667085;font-weight:700}
.tab.active{background:#1a73e8;color:#fff}
.panel{display:none;background:#fff;border-radius:18px;padding:16px;box-shadow:0 3px 14px rgba(16,24,40,.06)}
.panel.active{display:block}
.row{display:flex;gap:8px;margin-bottom:10px}
input{min-width:0;flex:1;border:1px solid #d9e2ef;border-radius:12px;padding:12px;font-size:14px;outline:none;background:#fbfdff}
input:focus{border-color:#1a73e8}
button{min-height:44px;cursor:pointer}
.primary{border:0;border-radius:12px;background:#1a73e8;color:#fff;padding:0 14px;font-weight:700}
.item{display:flex;align-items:center;gap:10px;background:#f7faff;border-radius:14px;padding:12px;margin-top:9px}
.item-main{flex:1;min-width:0}
.item-main b{font-size:15px}
.item-main p{font-size:12px;color:#667085;margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.chip{font-size:12px;border-radius:999px;background:#e8f1ff;color:#1a73e8;padding:5px 9px}
.done{opacity:.55}.done b{text-decoration:line-through}
.danger{border:0;background:transparent;color:#c8ced8;font-size:20px;padding:0 4px}
.empty{color:#98a2b3;text-align:center;padding:28px 10px;font-size:14px}
.money{font-weight:800;color:#0f9f6e}
</style>
</head>
<body>
<section class="hero">
<h1>??????</h1>
<p>????????????????????????????????</p>
<div class="stats">
<div class="stat"><b id="peopleCount">0</b><span>????</span></div>
<div class="stat"><b id="taskCount">0</b><span>????</span></div>
<div class="stat"><b id="budgetTotal">?0</b><span>????</span></div>
</div>
</section>
<nav class="tabs">
<button class="tab active" onclick="showTab('people',this)">??</button>
<button class="tab" onclick="showTab('tasks',this)">??</button>
<button class="tab" onclick="showTab('budget',this)">??</button>
</nav>
<section id="people" class="panel active">
<div class="row"><input id="personName" placeholder="??"><input id="personPhone" placeholder="??/??"></div>
<button class="primary" style="width:100%" onclick="addPerson()">????</button>
<div id="peopleList"></div>
</section>
<section id="tasks" class="panel">
<div class="row"><input id="taskName" placeholder="????????"><input id="ownerName" placeholder="???"></div>
<button class="primary" style="width:100%" onclick="addTask()">????</button>
<div id="taskList"></div>
</section>
<section id="budget" class="panel">
<div class="row"><input id="budgetName" placeholder="????"><input id="budgetAmount" type="number" placeholder="??"></div>
<button class="primary" style="width:100%" onclick="addBudget()">????</button>
<div id="budgetList"></div>
</section>
<script>
var state=JSON.parse(localStorage.getItem('eventHelper')||'{"people":[],"tasks":[],"budgets":[]}');
function save(){localStorage.setItem('eventHelper',JSON.stringify(state));render()}
function showTab(id,btn){document.querySelectorAll('.panel').forEach(function(p){p.classList.remove('active')});document.getElementById(id).classList.add('active');document.querySelectorAll('.tab').forEach(function(t){t.classList.remove('active')});btn.classList.add('active')}
function addPerson(){var n=document.getElementById('personName').value.trim(),p=document.getElementById('personPhone').value.trim();if(!n)return;state.people.unshift({id:Date.now(),name:n,phone:p});document.getElementById('personName').value='';document.getElementById('personPhone').value='';save()}
function addTask(){var n=document.getElementById('taskName').value.trim(),o=document.getElementById('ownerName').value.trim();if(!n)return;state.tasks.unshift({id:Date.now(),name:n,owner:o||'???',done:false});document.getElementById('taskName').value='';document.getElementById('ownerName').value='';save()}
function addBudget(){var n=document.getElementById('budgetName').value.trim(),a=parseFloat(document.getElementById('budgetAmount').value);if(!n||!a)return;state.budgets.unshift({id:Date.now(),name:n,amount:a});document.getElementById('budgetName').value='';document.getElementById('budgetAmount').value='';save()}
function del(type,id){state[type]=state[type].filter(function(x){return x.id!==id});save()}
function toggleTask(id){var t=state.tasks.find(function(x){return x.id===id});if(t)t.done=!t.done;save()}
function render(){var total=state.budgets.reduce(function(s,x){return s+x.amount},0),open=state.tasks.filter(function(t){return !t.done}).length;document.getElementById('peopleCount').textContent=state.people.length;document.getElementById('taskCount').textContent=open;document.getElementById('budgetTotal').textContent='?'+total.toFixed(0);renderList('peopleList',state.people,'people');renderList('taskList',state.tasks,'tasks');renderList('budgetList',state.budgets,'budgets')}
function renderList(id,items,type){var el=document.getElementById(id);if(!items.length){el.innerHTML='<div class="empty">????</div>';return}el.innerHTML='';items.forEach(function(x){var html='',cls='item';if(type==='people')html='<div class="item-main"><b>'+x.name+'</b><p>'+x.phone+'</p></div><span class="chip">???</span>';if(type==='tasks'){cls+=' '+(x.done?'done':'');html='<div class="item-main" onclick="toggleTask('+x.id+')"><b>'+x.name+'</b><p>????'+x.owner+'</p></div><span class="chip">'+(x.done?'??':'??')+'</span>'}if(type==='budgets')html='<div class="item-main"><b>'+x.name+'</b><p>??????</p></div><span class="money">?'+x.amount.toFixed(0)+'</span>';el.innerHTML+='<div class="'+cls+'">'+html+'<button class="danger" onclick="del(\''+type+'\','+x.id+')">?</button></div>'})}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ??????
// ???????????????????????????????????????????????????????????????????????
private val pomodoroHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>???</title>
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
<h1>???</h1>
<p class="subtitle">??25????????</p>
<div class="timer-ring">
<div class="time" id="timer">25:00</div>
</div>
<div class="sound-bar">
<button class="sound-btn" onclick="setSound('none')">??</button>
<button class="sound-btn" onclick="setSound('rain')">??</button>
<button class="sound-btn" onclick="setSound('waves')">??</button>
<button class="sound-btn" onclick="setSound('fire')">??</button>
</div>
<div class="controls">
<button class="btn btn-primary" id="startBtn" onclick="toggleTimer()">????</button>
<button class="btn btn-secondary" onclick="resetTimer()">??</button>
</div>
<div class="stats">
<div class="stat-row"><span class="stat-label">??????</span><span class="stat-value" id="sessions">0 ?</span></div>
<div class="stat-row"><span class="stat-label">??????</span><span class="stat-value" id="totalTime">0 ??</span></div>
<div class="stat-row"><span class="stat-label">????</span><span class="stat-value" id="mode">????</span></div>
</div>
<script>
let time=25*60,running=false,interval=null,sessions=0,totalMinutes=0,sound='none';
const timerEl=document.getElementById('timer'),startBtn=document.getElementById('startBtn');
function updateDisplay(){const m=Math.floor(time/60),s=time%60;timerEl.textContent=(m<10?'0':'')+m+':'+(s<10?'0':'')+s}
function toggleTimer(){if(running){clearInterval(interval);running=false;startBtn.textContent='????'}else{running=true;startBtn.textContent='??';interval=setInterval(()=>{if(time>0){time--;updateDisplay()}else{clearInterval(interval);running=false;sessions++;totalMinutes+=25;document.getElementById('sessions').textContent=sessions+' ?';document.getElementById('totalTime').textContent=totalMinutes+' ??';startBtn.textContent='????';time=25*60;updateDisplay();if(navigator.vibrate)navigator.vibrate(500)}},1000)}}
function resetTimer(){clearInterval(interval);running=false;time=25*60;updateDisplay();startBtn.textContent='????'}
function setSound(s){sound=s;document.querySelectorAll('.sound-btn').forEach(b=>b.classList.remove('active'));event.target.classList.add('active')}
updateDisplay();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ???????
// ???????????????????????????????????????????????????????????????????????
private val todoHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>????</title>
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
.checkbox.checked::after{content:'?';color:#fff;font-size:14px}
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
<h1>????</h1>
<span class="count" id="count">0 ?</span>
</div>
<div id="list"></div>
<div style="text-align:center;padding:40px;color:#999" id="empty">
<div class="empty-icon">TODO</div>
<p>???????</p>
<p style="font-size:13px;margin-top:8px">???????????????</p>
</div>
<div class="input-bar">
<input id="input" placeholder="?????..." onkeypress="if(event.key==='Enter')addTodo()">
<button onclick="addTodo()">??</button>
</div>
<script>
let todos=JSON.parse(localStorage.getItem('todos')||'[]');
function save(){localStorage.setItem('todos',JSON.stringify(todos));render()}
function addTodo(){const v=document.getElementById('input').value.trim();if(!v)return;todos.unshift({text:v,done:false,id:Date.now()});document.getElementById('input').value='';save()}
function toggle(id){const t=todos.find(t=>t.id===id);if(t)t.done=!t.done;save()}
function del(id){todos=todos.filter(t=>t.id!==id);save()}
function render(){var list=document.getElementById('list'),empty=document.getElementById('empty'),count=document.getElementById('count');list.innerHTML='';var active=todos.filter(function(x){return !x.done});count.textContent=active.length+' ?';empty.style.display=todos.length?'none':'block';todos.forEach(function(t){var d=document.createElement('div');d.className='todo-item'+(t.done?' done':'');d.innerHTML='<div class="checkbox '+(t.done?'checked':'')+'" onclick="toggle('+t.id+')"></div><span class="todo-text">'+t.text+'</span><span class="delete-btn" onclick="del('+t.id+')">?</span>';list.appendChild(d)})}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ??????
// ???????????????????????????????????????????????????????????????????????
private val memoHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>???</title>
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
<h1>???</h1>
<input class="search" placeholder="?????..." oninput="render(this.value)">
<div id="list"></div>
<button class="add-btn" onclick="openModal()">+</button>
<div class="modal" id="modal">
<div class="modal-content">
<input id="titleInput" placeholder="??">
<textarea id="bodyInput" placeholder="??????..."></textarea>
<div class="modal-btns">
<button class="btn-cancel" onclick="closeModal()">??</button>
<button class="btn-save" onclick="saveMemo()">??</button>
</div>
</div>
</div>
<script>
let memos=JSON.parse(localStorage.getItem('memos')||'[]');
function openModal(){document.getElementById('modal').classList.add('show');document.getElementById('titleInput').value='';document.getElementById('bodyInput').value=''}
function closeModal(){document.getElementById('modal').classList.remove('show')}
function saveMemo(){const t=document.getElementById('titleInput').value.trim()||'???',b=document.getElementById('bodyInput').value.trim();if(!b)return;memos.unshift({title:t,body:b,time:new Date().toLocaleString('zh-CN'),id:Date.now()});localStorage.setItem('memos',JSON.stringify(memos));closeModal();render()}
function delMemo(id){memos=memos.filter(m=>m.id!==id);localStorage.setItem('memos',JSON.stringify(memos));render()}
function render(q){q=q||'';var list=document.getElementById('list');list.innerHTML='';var filtered=q?memos.filter(function(x){return x.title.includes(q)||x.body.includes(q)}):memos;filtered.forEach(function(m){list.innerHTML+='<div class="memo-card"><span class="memo-del" onclick="delMemo('+m.id+')">?</span><div class="memo-title">'+m.title+'</div><div class="memo-body">'+m.body+'</div><div class="memo-time">'+m.time+'</div></div>'})}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ??????
// ???????????????????????????????????????????????????????????????????????
private val expenseHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>???</title>
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
<h1>???</h1>
<div class="balance" id="balance">?0.00</div>
<div class="balance-label">????</div>
<div class="row">
<div class="row-item"><div class="row-amount" id="income">?0</div><div class="row-label">??</div></div>
<div class="row-item"><div class="row-amount" id="expense">?0</div><div class="row-label">??</div></div>
</div>
</div>
<div class="tabs">
<button class="tab active" onclick="filter='all';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">??</button>
<button class="tab" onclick="filter='expense';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">??</button>
<button class="tab" onclick="filter='income';render();document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));this.classList.add('active')">??</button>
</div>
<div id="list"></div>
<div class="add-bar">
<select id="type"><option value="expense">??</option><option value="income">??</option></select>
<select id="cat"><option>??</option><option>??</option><option>??</option><option>??</option><option>??</option><option>??</option></select>
<input id="amount" type="number" placeholder="??" step="0.01">
<button onclick="add()">??</button>
</div>
<script>
let records=JSON.parse(localStorage.getItem('records')||'[]'),filter='all';
const icons={??:'?',??:'?',??:'?',??:'?',??:'?',??:'?'};
function add(){const type=document.getElementById('type').value,cat=document.getElementById('cat').value,amount=parseFloat(document.getElementById('amount').value);if(!amount||amount<=0)return;records.unshift({type,cat,amount,time:new Date().toLocaleDateString('zh-CN'),id:Date.now()});localStorage.setItem('records',JSON.stringify(records));document.getElementById('amount').value='';render()}
function del(id){records=records.filter(r=>r.id!==id);localStorage.setItem('records',JSON.stringify(records));render()}
function render(){var list=document.getElementById('list');list.innerHTML='';var inc=0,exp=0;var filtered=filter==='all'?records:records.filter(function(r){return r.type===filter});filtered.forEach(function(r){if(r.type==='income')inc+=r.amount;else exp+=r.amount;list.innerHTML+='<div class="item"><div class="item-icon">'+(icons[r.cat]||'?')+'</div><div class="item-info"><div class="item-cat">'+r.cat+'</div></div><span class="item-amount '+r.type+'">'+(r.type==='expense'?'-':'+')+'?'+r.amount.toFixed(2)+'</span><span class="item-time">'+r.time+'</span><span style="cursor:pointer;color:#ccc;margin-left:8px" onclick="del('+r.id+')">?</span></div>'});document.getElementById('balance').textContent='?'+(inc-exp).toFixed(2);document.getElementById('income').textContent='?'+inc.toFixed(0);document.getElementById('expense').textContent='?'+exp.toFixed(0)}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ???????
// ???????????????????????????????????????????????????????????????????????
private val habitHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>????</title>
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
<h1>????</h1>
<p class="sub" id="dateSub"></p>
<div id="list"></div>
<div class="add-area">
<input id="newHabit" placeholder="?????..." onkeypress="if(event.key==='Enter')addHabit()">
<button onclick="addHabit()">??</button>
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
function render(){const list=document.getElementById('list');list.innerHTML='';if(!habits.length){list.innerHTML='<div style="text-align:center;padding:60px;color:#999"><div style="font-size:32px;margin-bottom:12px">HABIT</div><p>?????????????</p></div>';return}
habits.forEach(function(h){var streak=getStreak(h),doneToday=h.days&&h.days[today],weekDays=['?','?','?','?','?','?','?'],d=new Date(),dayOfWeek=d.getDay(),weekHtml=weekDays.map(function(w,i){var dd=new Date(d);dd.setDate(dd.getDate()-(dayOfWeek===0?6:dayOfWeek-1)+i);var k=dd.toISOString().slice(0,10),isToday=k===today,isDone=h.days&&h.days[k];return'<div class="check-day'+(isDone?' done':'')+(isToday?' today':'')+'">'+w+'</div>'}).join('');list.innerHTML+='<div class="habit-card"><div class="habit-header"><span class="habit-name">'+h.name+'</span><span class="habit-streak">??'+streak+'?</span></div><div class="check-row">'+weekHtml+'</div><button class="check-btn '+(doneToday?'done':'active')+'" onclick="toggleDay('+h.id+')">'+(doneToday?'?????':'??')+'</button><div style="text-align:right;margin-top:8px"><span style="font-size:12px;color:#ccc;cursor:pointer" onclick="delHabit('+h.id+')">??</span></div></div>'})}
render();
</script>
</body>
</html>
""".trimIndent()

// ???????????????????????????????????????????????????????????????????????
// HTML ???????
// ???????????????????????????????????????????????????????????????????????
private val genericHtml = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<title>????</title>
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
<div class="icon">AI</div>
<h1>????</h1>
<p>?????AI?????????????????????????????????????</p>
<div class="counter" id="count">0</div>
<button class="btn" onclick="document.getElementById('count').textContent=parseInt(document.getElementById('count').textContent)+1">????</button>
<button class="btn" style="background:#f0f0f0;color:#666" onclick="document.getElementById('count').textContent=0">??</button>
<div class="note">
<textarea placeholder="?????????..."></textarea>
</div>
</div>
</body>
</html>
""".trimIndent()
