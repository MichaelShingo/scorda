package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Tag

class TagRepository(
    private val db: AppDatabase
) {
    val tagDao = db.tagDao()

    fun observeTags() = tagDao.getAllTags()

    fun observeTag(id: Long) = tagDao.getTagById(id)

    fun searchTags(query: String) = tagDao.searchTags(query)

    suspend fun insertTag(tag: Tag): Long = tagDao.insert(tag)

    suspend fun updateTag(tag: Tag) {
        val updatedTag = tag.copy(updatedAt = System.currentTimeMillis())
        tagDao.update(updatedTag)
    }

    suspend fun deleteTag(tag: Tag) = tagDao.delete(tag)
}
