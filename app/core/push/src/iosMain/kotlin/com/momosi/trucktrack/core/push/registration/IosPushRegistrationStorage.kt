@file:OptIn(ExperimentalForeignApi::class)

package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.logger.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.numberWithBool
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

private const val TAG = "PushRegistration"
private const val DIRECTORY_NAME = "PushRegistration"
private const val FILE_NAME = "push_registration.json"
private const val LAST_SENT_FIREBASE_INSTALLATION_ID_KEY = "lastSentFirebaseInstallationId"

internal class IosPushRegistrationStorage(private val file: NSURL?) : PushRegistrationStorage {

    private var values: Map<String, String> = read()

    override var lastSentFirebaseInstallationId: String?
        get() = values[LAST_SENT_FIREBASE_INSTALLATION_ID_KEY]
        set(value) = store(LAST_SENT_FIREBASE_INSTALLATION_ID_KEY, value)

    private fun store(key: String, value: String?) {
        values = if (value != null) values + (key to value) else values - key
        write()
    }

    private fun read(): Map<String, String> {
        val text = file?.let { NSString.stringWithContentsOfURL(it, NSUTF8StringEncoding, null) } ?: return emptyMap()
        return runCatching {
            Json.parseToJsonElement(text).jsonObject.mapNotNull { (key, value) -> value.jsonPrimitive.contentOrNull?.let { key to it } }.toMap()
        }.getOrElse { emptyMap() }
    }

    private fun write() {
        val text = JsonObject(values.mapValues { JsonPrimitive(it.value) }).toString()
        val written = file != null && text.encodeToByteArray().toNSData().writeToURL(file, atomically = true)
        if (!written) Logger.w(TAG, "Storing the push registration state failed")
    }

    companion object {
        fun inBackupExcludedDirectory(): IosPushRegistrationStorage = IosPushRegistrationStorage(backupExcludedDirectory()?.URLByAppendingPathComponent(FILE_NAME))

        private fun backupExcludedDirectory(): NSURL? {
            val fileManager = NSFileManager.defaultManager
            val applicationSupport = fileManager.URLForDirectory(NSApplicationSupportDirectory, NSUserDomainMask, null, true, null) ?: return null
            val directory = applicationSupport.URLByAppendingPathComponent(DIRECTORY_NAME, isDirectory = true) ?: return null
            fileManager.createDirectoryAtURL(directory, withIntermediateDirectories = true, attributes = null, error = null)
            directory.setResourceValue(NSNumber.numberWithBool(true), forKey = NSURLIsExcludedFromBackupKey, error = null)
            return directory
        }
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }
