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

        val originalText = focusedNode.text?.toString() ?: ""
        val selectionStart = focusedNode.textSelectionStart.takeIf { it >= 0 } ?: originalText.length
        val selectionEnd = focusedNode.textSelectionEnd.takeIf { it >= 0 } ?: originalText.length

        val newText = StringBuilder(originalText)
            .replace(selectionStart, selectionEnd, text)
            .toString()

        // try to set text
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                newText
            )
        }
        val textSetSuccess = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        if (textSetSuccess) {
            // set cursor if supported
            val cursorPos = selectionStart + text.length
            val selectionArgs = Bundle().apply {
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, cursorPos)
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, cursorPos)
            }
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectionArgs)
        } else {
            // fallback: copy to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("special_letter", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "$text 已复制，可粘贴使用", Toast.LENGTH_SHORT).show()
        }
    }
//
//
//    // insert text at current cursor position and restore cursor
//    fun inputText(text: String) {
//        val rootNode = rootInActiveWindow ?: return
//        val focusedNode = findFocusedEditText(rootNode) ?: return
//
//        // original text
//        val originalText = focusedNode.text?.toString() ?: ""
//        // get the position of the cursor
//        val selectionStart = focusedNode.textSelectionStart
//        val selectionEnd = focusedNode.textSelectionEnd
//
//        // insert text at the cursor(or replace the selected part)
//        val newText = StringBuilder(originalText)
//            .replace(selectionStart, selectionEnd, text)
//            .toString()
//
//        // write to the EditText
//        val args = Bundle().apply {
//            putCharSequence(
//                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
//                newText
//            )
//        }
//        focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
//
//        // put the cursor back
//        val cursorPosition = selectionStart + text.length
//        val selectionArgs = Bundle().apply {
//            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, cursorPosition)
//            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, cursorPosition)
//        }
//        focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectionArgs)
//    }

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
