/*
 *
 *  * Apache License
 *  *
 *  * Copyright [2017] Sinyuk
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.sinyuk.fanfou.domain.db.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import com.sinyuk.fanfou.domain.DO.Status


/**
 * Created by sinyuk on 2017/11/30.
 *
 */
@Dao
interface StatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserts(statuses: MutableList<Status>): MutableList<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(status: Status): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updates(statuses: MutableList<Status>): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(status: Status): Int

    @Query("SELECT * FROM statuses WHERE id = :id LIMIT 1")
    fun query(id: String): Status?

    @Query("SELECT * FROM statuses WHERE pathFlag & :path = :path ORDER BY createdAt DESC LIMIT 1")
    fun first(path: Int): Status?

    @Query("SELECT * FROM statuses WHERE pathFlag & :path = :path ORDER BY createdAt DESC")
    fun haha(path: Int): DataSource.Factory<Int, Status>

    @Query("SELECT * FROM statuses WHERE pathFlag & :path = :path ORDER BY createdAt DESC LIMIT :limit")
    fun loadInitial(path: Int, limit: Int): MutableList<Status>


    @Query("SELECT * FROM statuses WHERE pathFlag & :path = :path AND" +
            " createdAt < (SELECT createdAt FROM statuses WHERE id = :id)" +
            " ORDER BY createdAt DESC LIMIT :limit")
    fun loadAfter(path: Int, id: String, limit: Int): MutableList<Status>
}