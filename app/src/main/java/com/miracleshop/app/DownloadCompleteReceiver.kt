package com.miracleshop.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)

        manager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            if (status != DownloadManager.STATUS_SUCCESSFUL) return

            val localUriString = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            )
            if (localUriString.isNullOrBlank()) return

            val localUri = Uri.parse(localUriString)
            val installUri = if (localUri.scheme == "file") {
                FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    File(localUri.path ?: return)
                )
            } else {
                localUri
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(installUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(installIntent)
            } catch (_: Exception) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            }
        }
    }
}
