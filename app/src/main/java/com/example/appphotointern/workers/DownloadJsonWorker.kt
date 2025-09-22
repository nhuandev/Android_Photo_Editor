package com.example.appphotointern.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.appphotointern.common.DOWN_FAIL
import com.example.appphotointern.common.URL_STORAGE
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException

class DownloadJsonWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val filename = inputData.getString("filename") ?: return failure("MissingFileName")
        return try {
            val cacheFile = File(applicationContext.filesDir, filename)
            if (!cacheFile.exists()) {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("$URL_STORAGE/file_json/$filename")

                val metadata = storageRef.metadata.await()
                val fileSize = metadata.sizeBytes

                val bytes = storageRef.getBytes(fileSize).await()
                cacheFile.writeBytes(bytes)
            }
            Result.success(workDataOf("filePath" to cacheFile.absolutePath))
        } catch (e: StorageException) {
            val error = when (e.errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> "ObjectNotFound"
                StorageException.ERROR_NOT_AUTHORIZED -> "NotAuthorized"
                StorageException.ERROR_QUOTA_EXCEEDED -> "QuotaExceeded"
                else -> "StorageError"
            }
            failure(error)
        } catch (e: IOException) {
            Log.d("Download Worker", "${e.message}")
            return failure("IOError: ${e.message}")
        } catch (e: Exception) {
            failure("UnknownError: ${e.message}")
        }
    }

    private fun failure(msg: String): Result {
        return Result.failure(workDataOf(DOWN_FAIL to msg))
    }
}
