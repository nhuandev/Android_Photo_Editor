package com.example.appphotointern.data.storage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.appphotointern.common.URL_STORAGE
import com.example.appphotointern.utils.PurchasePrefs
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException

class DownloadImageWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val storage = FirebaseStorage.getInstance()

    override suspend fun doWork(): Result {
        val folder = inputData.getString("sticker_folder") ?: return Result.failure()
        val name = inputData.getString("sticker_name") ?: return Result.failure()
        val isPremium = inputData.getBoolean("sticker_premium", false)
        val hasPremium = PurchasePrefs(applicationContext).hasPremium

        return try {
            val path = "$URL_STORAGE/sticker/$folder/$name.webp"
            val imgRef = storage.reference.child(path)

            val dir = File(applicationContext.filesDir, folder)
            if (!dir.exists()) dir.mkdirs()
            val localFile = File(dir, "$name.webp")

            if (localFile.exists()) {
                return Result.success(workDataOf("path" to localFile.absolutePath))
            }

            if (isPremium && !hasPremium) {
                return Result.failure(workDataOf("error" to "NotAuthorized"))
            }

            imgRef.getFile(localFile).await()

            Result.success(workDataOf("path" to localFile.absolutePath))
        } catch (e: StorageException) {
            val error = when (e.errorCode) {
                StorageException.ERROR_NOT_AUTHORIZED -> "NotAuthorized"
                StorageException.ERROR_OBJECT_NOT_FOUND -> "ObjectNotFound"
                StorageException.ERROR_QUOTA_EXCEEDED -> "QuotaExceeded"
                else -> "StorageError"
            }
            Result.failure(workDataOf("error" to error))
        } catch (e: Exception) {
            if (e is IOException) {
                return Result.retry()
            }
            Result.failure(workDataOf("error" to (e.message ?: "Unknown")))
        }
    }
}
