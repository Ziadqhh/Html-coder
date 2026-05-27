package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val html: String,
    val css: String,
    val js: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "enabledPluginIds", defaultValue = "") val enabledPluginIds: String = "",
    @ColumnInfo(name = "customPluginsRaw", defaultValue = "") val customPluginsRaw: String = ""
)
