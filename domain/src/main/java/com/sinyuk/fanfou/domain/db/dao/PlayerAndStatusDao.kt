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

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.sinyuk.fanfou.domain.vo.PlayerAndStatus
import com.sinyuk.fanfou.domain.vo.Status

/**
 * Created by sinyuk on 2017/12/2.
 */
@Dao
interface PlayerAndStatusDao {
    @Insert
    fun insert(mapper: PlayerAndStatus): Long

    @Query("SELECT * from statuses WHERE id IN " +
            "(SELECT statusId from player_status WHERE playerId = :uniqueId)" +
            "AND createdAt > (SELECT createdAt FROM statuses WHERE id = :since)" +
            "ORDER BY createdAt DESC LIMIT :limit")
    fun before(uniqueId: String, since: String?, limit: Int): LiveData<MutableList<Status>?>


    @Query("SELECT * from statuses WHERE id IN " +
            "(SELECT statusId from player_status WHERE playerId = :uniqueId)" +
            "AND createdAt < (SELECT createdAt FROM statuses WHERE id = :max)" +
            "ORDER BY createdAt DESC LIMIT :limit")
    fun after(uniqueId: String, max: String?, limit: Int): LiveData<MutableList<Status>?>


    @Query("SELECT * from statuses WHERE id IN " +
            "(SELECT statusId from player_status WHERE playerId = :uniqueId)" +
            "ORDER BY createdAt DESC LIMIT :limit")
    fun initial(uniqueId: String, limit: Int): LiveData<MutableList<Status>?>
}