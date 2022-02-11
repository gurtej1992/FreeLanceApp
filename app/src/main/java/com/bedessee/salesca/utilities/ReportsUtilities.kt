package com.bedessee.salesca.utilities

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.bedessee.salesca.main.MainActivity
import com.bedessee.salesca.reportsmenu.ReportsMenu
import com.bedessee.salesca.sharedprefs.SharedPrefsManager
import com.bedessee.salesca.store.Store
import com.bedessee.salesca.store.WebViewer.Companion.show
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader


class ReportsUtilities {
    companion object {
        /**
         * Opens first report with property with the property defaultOpenAfterCustomerSelect equals to YES
         */
        fun openFirstDefaultOpenReport(context: Context, store: Store) {
            try {
                val type = object : TypeToken<List<ReportsMenu?>?>() {}.type
                val reader = BufferedReader(FileReader(SharedPrefsManager(context).sugarSyncDir + "/data/bottom_left_reports_menu.json"))
                val reports = Gson().fromJson<List<ReportsMenu>>(reader, type)
                for (i in reports.indices) {
                    if (isYES(reports[i].defaultOpenAfterCustomerSelect)) {
                        openReportMenu(context, reports[i], store)
                        break
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }

        private fun isYES(value:String): Boolean {
            return (value == "YES" || value == "Y")
        }

        private fun notSuccessfullyOpen(context: Context) {
            if (context is MainActivity) {
                context.clearReportMenu()
            }
        }


        fun openReportMenu(context: Context, reportsMenu: ReportsMenu, store: Store) {
            val file: File = FileUtilities.getFile(
                context,
                store.baseNumber,
                reportsMenu.popupType,
                reportsMenu.deviceFolder
            )
            if (file.exists()) {
                when (reportsMenu.popupType) {
                    "TXT" -> {
                        show(context, file.absolutePath)
                    }
                    "PDF" -> {
                        val result = FileUtilities.openPDF(context, file)
                        if (!result) notSuccessfullyOpen(context)
                    }
                    "CSV" -> {
                        val result = FileUtilities.openCSV(context, file)
                        if (!result) notSuccessfullyOpen(context)
                    }
                    "HTM" -> {
                        show(context, FileUtilities.getFile(
                            context,
                            store.baseNumber,
                            reportsMenu.popupType,
                            reportsMenu.deviceFolder
                        ).absolutePath)
//                        val f = FileUtilities.getFile(
//                                context,
//                        store.baseNumber,
//                        reportsMenu.popupType,
//                        reportsMenu.deviceFolder
//                        )
//                        val intent = Intent(ACTION_VIEW)
//                        intent.addCategory(Intent.CATEGORY_BROWSABLE)
//                        intent.setDataAndType(Uri.fromFile(f), "application/x-webarchive-xml")
//// Have to add this one in order to work on Target 2.3.3 (API 10)
//// Have to add this one in order to work on Target 2.3.3 (API 10)
//                        intent.setClassName(
//                            "com.android.browser",
//                            "com.android.browser.BrowserActivity"
//                        )
//
//                        context.startActivity(intent);
                    }
                }
            } else {
                FileUtilities.showToastFileNotFound(context)
                notSuccessfullyOpen(context)
            }
        }



    }
}