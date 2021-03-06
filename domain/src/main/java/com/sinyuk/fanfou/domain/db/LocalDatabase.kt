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

package com.sinyuk.fanfou.domain.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.DO.Keyword
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.DO.Trend
import com.sinyuk.fanfou.domain.db.dao.KeywordDao
import com.sinyuk.fanfou.domain.db.dao.PlayerDao
import com.sinyuk.fanfou.domain.db.dao.StatusDao
import com.sinyuk.fanfou.domain.db.dao.TrendDao


/**
 * Created by sinyuk on 2017/11/27.
 *
 */
@Database(entities = [(Player::class), (Status::class), (Keyword::class), (Trend::class)],
        version = BuildConfig.VERSION_CODE, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun statusDao(): StatusDao
    abstract fun keywordDao(): KeywordDao
    abstract fun trendDao(): TrendDao

    companion object {

        private var INSTANCE: LocalDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): LocalDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, LocalDatabase::class.java, "fanfou.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                }
                return INSTANCE!!
            }
        }

        private var INSTANCE2: LocalDatabase? = null

        fun getInMemory(context: Context): LocalDatabase {
            synchronized(lock) {
                if (INSTANCE2 == null) {
                    INSTANCE2 = Room.inMemoryDatabaseBuilder(context.applicationContext, LocalDatabase::class.java)
                            .fallbackToDestructiveMigration()
                            .build()
                }
                return INSTANCE2!!
            }
        }
    }
}