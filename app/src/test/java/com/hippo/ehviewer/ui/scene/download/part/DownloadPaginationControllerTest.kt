package com.hippo.ehviewer.ui.scene.download.part

import androidx.recyclerview.widget.RecyclerView
import com.hippo.ehviewer.dao.DownloadInfo
import com.hippo.ehviewer.widget.MyEasyRecyclerView
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadPaginationControllerTest {
    private class Host(count: Int) : DownloadPaginationController.Host {
        override var list: MutableList<DownloadInfo>? = MutableList(count) { DownloadInfo(it.toLong()) }
        override val notifyAdapter: RecyclerView.Adapter<*>? = null
        override val recyclerView: MyEasyRecyclerView? = null
        override fun isGalleryAvailableLocally(info: DownloadInfo) = true
    }

    @Test fun thresholdDoesNotHideRows() {
        val controller = DownloadPaginationController(Host(500))
        controller.indexPage = 2
        assertEquals(500, controller.currentPageList.size)
        assertEquals(0, controller.positionInList(0))
    }

    @Test fun nextPageUsesCorrectSourceOffset() {
        val controller = DownloadPaginationController(Host(501))
        controller.indexPage = 2
        assertEquals(50, controller.currentPageList.size)
        assertEquals(50L, controller.currentPageList.first().gid)
        assertEquals(50, controller.positionInList(0))
        assertEquals(0, controller.listIndexInPage(50))
    }

    @Test fun lastPageContainsOnlyRemainingRows() {
        val controller = DownloadPaginationController(Host(501))
        controller.indexPage = 11
        assertEquals(1, controller.currentPageList.size)
        assertEquals(500L, controller.currentPageList.first().gid)
    }

    @Test fun disabledPaginationIncludesEverything() {
        val controller = DownloadPaginationController(Host(501))
        controller.isCanPagination = false
        controller.indexPage = 3
        assertEquals(501, controller.currentPageList.size)
        assertEquals(0, controller.positionInList(0))
    }

    @Test fun recreatedViewUsesCurrentHostList() {
        val host = Host(501)
        val controller = DownloadPaginationController(host)
        controller.indexPage = 2
        host.list = MutableList(530) { DownloadInfo(1000L + it) }
        assertEquals(1050L, controller.currentPageList.first().gid)
        host.list = null
        assertEquals(0, controller.currentPageList.size)
    }
}
