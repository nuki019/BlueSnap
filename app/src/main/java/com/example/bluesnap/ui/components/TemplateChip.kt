package com.example.bluesnap.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class Template(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val prompt: String
)

val defaultTemplates = listOf(
    Template("活动筹备", Icons.Filled.Assignment, "报名、分工、预算一屏管理", "帮我做一个校园活动筹备工具，要能记录报名、任务分工、预算和进度"),
    Template("课程任务板", Icons.Filled.TrackChanges, "作业、考试、DDL 统一追踪", "帮我做一个课程任务板，能记录作业、考试、DDL 和完成状态"),
    Template("小组分工", Icons.Filled.Groups, "成员任务和进度清楚同步", "帮我做一个小组作业分工工具，能记录成员、任务、负责人和进度"),
    Template("求职投递表", Icons.Filled.WorkOutline, "简历、投递、面试一表跟进", "帮我做一个求职投递表，能记录公司、岗位、投递状态和面试安排"),
    Template("生活预算", Icons.Filled.Savings, "校园日常开销快速记录", "帮我做一个生活预算工具，能记录支出、分类和本月剩余额度"),
    Template("习惯打卡", Icons.Filled.AccountTree, "运动、背词、早睡连续打卡", "帮我做一个习惯打卡工具，能记录每天完成情况和连续天数")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateChip(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(text = template.name, style = MaterialTheme.typography.labelLarge)
        },
        icon = {
            Icon(
                imageVector = template.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier.padding(end = 8.dp, bottom = 8.dp)
    )
}
