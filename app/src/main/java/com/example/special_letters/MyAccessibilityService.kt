package com.example.special_letters

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MyAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun inputText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val focusedNode = findFocusedEditText(rootNode) ?: return

        val currentText = focusedNode.text?.toString() ?: ""

        val newText = if (currentText == "在这里输入") {
            // If the content is exactly the placeholder, replace it entirely
            text
        } else {
            // Otherwise, insert at the current cursor position
            val start = focusedNode.textSelectionStart.takeIf { it >= 0 } ?: currentText.length
            val end = focusedNode.textSelectionEnd.takeIf { it >= 0 } ?: currentText.length
            StringBuilder(currentText).replace(start, end, text).toString()
        }

        // Set the new text
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
        }
        focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        // Move cursor to end of inserted text
        val cursorPos = if (currentText == "在这里输入") text.length else (focusedNode.textSelectionStart.takeIf { it >= 0 } ?: 0) + text.length
        val selArgs = Bundle().apply {
            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, cursorPos)
            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, cursorPos)
        }
        focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selArgs)
    }

    // find the current focused EditText
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
