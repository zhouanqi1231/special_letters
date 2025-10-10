package com.example.special_letters

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MyAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 可以监听界面变化或焦点变化，如果需要扩展功能可以在这里处理
    }

    override fun onInterrupt() {
        // 服务被中断时
    }

    /**
     * 在当前焦点输入框插入文本
     */
    fun inputText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val focusedNode = findFocusedEditText(rootNode)
        focusedNode?.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
            }
        )
    }

    /**
     * 递归查找当前获得焦点的 EditText
     */
    private fun findFocusedEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.EditText" && node.isFocused) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val result = findFocusedEditText(child)
                if (result != null) return result
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
