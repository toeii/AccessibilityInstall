package com.toeii.install.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AutoInstallService : AccessibilityService() {
    private val mHandler = Handler()

    override fun onServiceConnected() {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        mHandler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, DELAY_PAGE.toLong())
    }

    override fun onDestroy() {
        // 服务停止，重新进入系统设置界面
        AccessibilityUtil.jumpToSetting(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !event.packageName.toString()
                .contains("packageinstaller")
        )
            return
        val eventNode = event.source
        if (eventNode == null) {
            Log.i(TAG, "eventNode: null, 重新获取eventNode...")
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            mHandler.postDelayed({
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }, DELAY_PAGE.toLong())
            return
        }
        val rootNode = rootInActiveWindow ?: return
        Log.i(TAG, "rootNode: $rootNode")
        if (isNotAD(rootNode))
            findTxtClick(rootNode, "安装") //安装->下一步->打开
        findTxtClick(rootNode, "继续安装")
        findTxtClick(rootNode, "下一步")
        findTxtClick(rootNode, "打开")
        eventNode.recycle()
        rootNode.recycle()
    }

    private fun findTxtClick(nodeInfo: AccessibilityNodeInfo, txt: String) {
        val nodes = nodeInfo.findAccessibilityNodeInfosByText(txt)
        if (nodes == null || nodes.isEmpty())
            return
        for (node in nodes) {
            if (node.isEnabled && node.isClickable && (node.className == "android.widget.Button" || node.className == "android.widget.CheckBox"))
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    // 排除广告[安装]按钮
    private fun isNotAD(rootNode: AccessibilityNodeInfo): Boolean {
        return (isNotFind(rootNode, "还喜欢") //小米
                && isNotFind(rootNode, "官方安装")) //华为
    }

    private fun isNotFind(rootNode: AccessibilityNodeInfo, txt: String): Boolean {
        val nodes = rootNode.findAccessibilityNodeInfosByText(txt)
        return nodes == null || nodes.isEmpty()
    }

    override fun onInterrupt() {}

    companion object {
        private val TAG = AutoInstallService::class.java.simpleName
        private val DELAY_PAGE = 320
    }

}