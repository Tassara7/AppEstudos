package br.com.appestudos.data.local.converters

import androidx.room.TypeConverter
import br.com.appestudos.data.model.DifficultyLevel
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.data.model.MediaType
import br.com.appestudos.data.model.RichTextContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromFlashcardType(value: String): FlashcardType {
        return FlashcardType.valueOf(value)
    }

    @TypeConverter
    fun toFlashcardType(type: FlashcardType): String {
        return type.name
    }

    @TypeConverter
    fun fromMediaType(value: String): MediaType {
        return MediaType.valueOf(value)
    }

    @TypeConverter
    fun toMediaType(type: MediaType): String {
        return type.name
    }

    @TypeConverter
    fun fromDifficultyLevel(value: String): DifficultyLevel {
        return DifficultyLevel.valueOf(value)
    }

    @TypeConverter
    fun toDifficultyLevel(level: DifficultyLevel): String {
        return level.name
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromIntList(value: String?): List<Int>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromLongList(value: String?): List<Long>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toLongList(list: List<Long>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromRichTextContent(value: String?): RichTextContent? {
        if (value == null) return null
        return gson.fromJson(value, RichTextContent::class.java)
    }

    @TypeConverter
    fun toRichTextContent(content: RichTextContent?): String? {
        if (content == null) return null
        return gson.toJson(content)
    }

    @TypeConverter
    fun fromRichTextContentList(value: String?): List<RichTextContent>? {
        if (value == null) return null
        val listType = object : TypeToken<List<RichTextContent>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toRichTextContentList(list: List<RichTextContent>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }
}