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
import android.arch.paging.ItemKeyedDataSource
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.api.RestAPI
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/28.
 *
 */
class KeyedStatusDataSource(private val restAPI: RestAPI,
                            private val path: String,
                            private val uniqueId: String?,
                            private val appExecutors: AppExecutors) : ItemKeyedDataSource<String, Status>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     * <p>
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            appExecutors.networkIO().execute {
                it.invoke()
            }
        }
    }

    override fun getKey(item: Status) = item.id

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Status>) {
        val request = when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.requestedLoadSize)
            else -> restAPI.fetch_from_path(path = path, count = params.requestedLoadSize, id = uniqueId)
        }

        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            retry = null
            callback.onResult(response.body()!!)
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)

        } catch (e: IOException) {
            retry = { loadInitial(params, callback) }
            val error = NetworkState.error(e.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Status>) {

        val request = when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.requestedLoadSize, max = params.key)
            else -> restAPI.fetch_from_path(path = path, count = params.requestedLoadSize, id = uniqueId, max = params.key)
        }
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)

        try {
            val response = request.execute()
            if (response.isSuccessful) {
                val items = if (response.body() == null) {
                    mutableListOf()
                } else {
                    response.body()
                }
                retry = null
                when (items?.size) {
                    0 -> networkState.postValue(NetworkState.TERMINAL)
                    params.requestedLoadSize -> {
                        callback.onResult(items)
                        networkState.postValue(NetworkState.LOADED)
                    }
                    else -> {
                        items?.let { callback.onResult(it) }
                        networkState.postValue(NetworkState.TERMINAL)
                    }
                }
            } else {
                retry = { loadAfter(params, callback) }
                networkState.postValue(NetworkState.error("error code: ${response.code()}"))
            }

        } catch (e: IOException) {
            // keep a lambda for future retry
            retry = { loadAfter(params, callback) }
            // publish the error
            networkState.postValue(NetworkState.error(e.message ?: "unknown err"))
        }

    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Status>) {
        // ignored, since we only ever append to our initial load
    }
}