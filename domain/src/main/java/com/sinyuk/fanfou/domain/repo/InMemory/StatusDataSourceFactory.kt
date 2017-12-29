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

package com.sinyuk.fanfou.domain.repo.InMemory

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.RestAPI


/**
 * Created by sinyuk on 2017/12/29.
 * </p>
 *
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class StatusDataSourceFactory(private val restAPI: RestAPI,
                              private val path: String,
                              private val uniqueId: String?,
                              private val appExecutors: AppExecutors) : DataSource.Factory<String, Status> {
    val sourceLiveData = MutableLiveData<KeyedStatusDataSource>()

    override fun create(): DataSource<String, Status> {
        val source = KeyedStatusDataSource(restAPI, path, uniqueId, appExecutors)
        sourceLiveData.postValue(source)
        return source
    }
}