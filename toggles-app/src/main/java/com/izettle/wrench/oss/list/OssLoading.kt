package com.izettle.wrench.oss.list

import android.content.Context
import com.izettle.wrench.oss.LicenceMetadata
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

object OssLoading {
    fun getThirdPartyLicenceMetadata(context: Context): ArrayList<LicenceMetadata> {
        val thirdPartyLicenseMetadata = resourcePartToString(context.applicationContext, "third_party_license_metadata", 0L, -1)
        val thirdPartyLicences = thirdPartyLicenseMetadata.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val licenceMetadataArrayList = ArrayList<LicenceMetadata>(thirdPartyLicences.size)

        for (licenceString in thirdPartyLicences) {
            val indexOfSpace = licenceString.indexOf(' ')
            val byteInformation = licenceString.substring(0, indexOfSpace).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val validByteInformation = byteInformation.size == 2 && indexOfSpace > 0

            if (!validByteInformation) {
                if (licenceString.isNotEmpty()) {
                    throw IllegalStateException("Invalid license meta-data line:\n$licenceString")
                } else {
                    throw IllegalStateException("Invalid license meta-data line:\n")
                }
            }

            val byteStart = java.lang.Long.parseLong(byteInformation[0])
            val byteEnd = Integer.parseInt(byteInformation[1])
            licenceMetadataArrayList.add(LicenceMetadata(licenceString.substring(indexOfSpace + 1), byteStart, byteEnd))
        }

        return licenceMetadataArrayList
    }

    fun getThirdPartyLicence(context: Context, licenceMetadata: LicenceMetadata): String {
        val skipBytes = licenceMetadata.skipBytes
        val length = licenceMetadata.length
        return resourcePartToString(context, "third_party_licenses", skipBytes, length)
    }

    private fun resourcePartToString(context: Context, resourceName: String, skipBytes: Long, length: Int): String {
        val resources = context.resources
        return inputStreamPartToString(resources.openRawResource(resources.getIdentifier(resourceName, "raw", context.packageName)), skipBytes, length)
    }

    private fun inputStreamPartToString(inputStream: InputStream, skipBytes: Long, length: Int): String {
        val buffer = ByteArray(1024)
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            inputStream.skip(skipBytes)

            val totalBytesToRead = if (length > 0) length else Integer.MAX_VALUE

            var bytesLeftToRead = totalBytesToRead
            while (true) {
                val bytesRead = inputStream.read(buffer, 0, Math.min(bytesLeftToRead, 1024))
                if (bytesLeftToRead <= 0 || bytesRead == -1) {
                    inputStream.close()
                    break
                }

                byteArrayOutputStream.write(buffer, 0, bytesRead)
                bytesLeftToRead -= bytesRead
            }
        } catch (exception: IOException) {
            throw RuntimeException("Failed to read license or metadata text.", exception)
        }

        try {
            return byteArrayOutputStream.toString("UTF-8")
        } catch (exception: UnsupportedEncodingException) {
            throw RuntimeException("Unsupported encoding UTF8. This should always be supported.", exception)
        }
    }
}
