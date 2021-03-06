package com.bedessee.salesca.utilities

import android.content.Context
import android.widget.Toast
import com.bedessee.salesca.sharedprefs.SharedPrefsManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.*
import java.nio.charset.StandardCharsets

object FolderClearUp {
    const val clear_folder_json = "app_folders_to_clear.json"
    const val general_cleanup = "clear.json"

    fun clearFoldersByName(context: Context, fileName:String) : Boolean {
        var result = false
        val folder = SharedPrefsManager(context).dataFolder
        val sugarFolder = File(SharedPrefsManager(context).sugarSyncDir)
        val file = File(folder + File.separator + fileName)
        if (file.exists() && file.isFile) {
            val arrayFolders = object: TypeToken<ArrayList<Folder>>() {}.type
            val folders:List<Folder> = Gson().fromJson(loadJSONFromFile(file), arrayFolders)
            val list = sugarFolder.listFiles()
            folders.forEach {
                list?.forEach { file ->
                    if (file.name == it.FOLDER) {
                       Timber.d("folder found ${file.name}")
                       Timber.d("deleting contents of ${file.name}")
                       file.deleteRecursively()
                       result = true
                    }
                }
            }
        } else {
            Toast.makeText(context, "file $clear_folder_json not found", Toast.LENGTH_SHORT).show()
        }

        return result
    }

    data class Folder(val FOLDER:String)

    fun loadJSONFromFile(fileName: File): String? {
        val json: String
        json = try {
            val inputStream: InputStream = FileInputStream(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            Timber.e(ex)
            return null
        }
        return json
    }
}