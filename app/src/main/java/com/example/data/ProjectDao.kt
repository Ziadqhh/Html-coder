package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModifiedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Delete
    suspend fun deleteProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)
}
