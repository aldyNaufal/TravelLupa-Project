package com.example.travellupa.database

import android.util.Base64
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        return bytes?.let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    @TypeConverter
    fun toByteArray(encodedString: String?): ByteArray? {
        return encodedString?.let { Base64.decode(it, Base64.DEFAULT) }
    }
}
