package com.main.accessible.tz.utils

import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.ConcurrentHashMap

object AccessibilityUtils {
     suspend fun fillEditText(input: String, rootNode : AccessibilityNodeInfo ) {
        // 这里我们查找 EditText 控件。通常 EditText 控件的类名是 "android.widget.EditText"。
        val editTextList: List<AccessibilityNodeInfo> = findNodesByClassName(rootNode, "android.widget.EditText")!!
        for (editText in editTextList) {
            // 输入自定义文字
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                input
            )
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }

     private fun findNodesByClassName(
        rootNode: AccessibilityNodeInfo,
        className: String
    ): ArrayList<AccessibilityNodeInfo>? {
        val matchingNodes: ArrayList<AccessibilityNodeInfo> = ArrayList()
        recursiveSearchByClassName(rootNode, className, matchingNodes)
        return matchingNodes
    }


    @Synchronized
    private fun recursiveSearchByClassName(
        node: AccessibilityNodeInfo?,
        className: String,
        resultList: ArrayList<AccessibilityNodeInfo>
    ) {
        if (node == null) {
            return
        }
        if (className == node.className) {
            resultList.add(node)
        }
        for (i in 0 until node.childCount) {
            recursiveSearchByClassName(node.getChild(i), className, resultList)
        }
    }

    var mapIds:ConcurrentHashMap<String,String> = ConcurrentHashMap()
    /*发送*/
    suspend fun clickViewByContentDescription(description: String,input:String,rootNode: AccessibilityNodeInfo) : Boolean {
        /*if (rootNode == null || id.equals("1") || id.equals("2")) {
            return;
        }*/
        val matchingNodes: ArrayList<AccessibilityNodeInfo> =
            findNodesByContentDescription(rootNode, description)!!
        for (node in matchingNodes) {
            val isSend = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.i("message_----发送send:", "执行了几次send$isSend")
            return isSend
        }
        return false

    }
    private fun findNodesByContentDescription(
        rootNode: AccessibilityNodeInfo,
        description: String
    ): ArrayList<AccessibilityNodeInfo>? {
        val matchingNodes: ArrayList<AccessibilityNodeInfo> = java.util.ArrayList()
        recursiveSearchByContentDescription(rootNode, description, matchingNodes)
        return matchingNodes
    }

    private fun recursiveSearchByContentDescription(
        node: AccessibilityNodeInfo?,
        description: String,
        resultList: MutableList<AccessibilityNodeInfo>
    ) {
        if (node == null) {
            return
        }
        if (node.contentDescription != null && description == node.contentDescription.toString()) {
            resultList.add(node)
        }
        for (i in 0 until node.childCount) {
            recursiveSearchByContentDescription(node.getChild(i), description, resultList)
        }
    }

}