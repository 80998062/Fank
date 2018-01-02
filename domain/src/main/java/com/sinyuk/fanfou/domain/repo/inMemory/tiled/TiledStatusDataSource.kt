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

package com.sinyuk.fanfou.domain.repo.inMemory.tiled

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PositionalDataSource
import com.sinyuk.fanfou.domain.AppExecutors
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.NetworkState
import com.sinyuk.fanfou.domain.TIMELINE_FAVORITES
import com.sinyuk.fanfou.domain.api.RestAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/29.
 *
 */
class TiledStatusDataSource(private val restAPI: RestAPI,
                            private val path: String,
                            private val uniqueId: String?,
                            private val appExecutors: AppExecutors) : PositionalDataSource<Status>() {

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

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Status>) {
        networkState.postValue(NetworkState.LOADING)

        try {
            val response = when (path) {
                TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.loadSize, page = ((params.startPosition / params.loadSize) + 1))
                else -> TODO()
            }.execute()
            if (response.body()?.isNotEmpty() == true) {
                retry = null
                networkState.postValue(NetworkState.LOADED)
                initialLoad.postValue(NetworkState.LOADED)
                callback.onResult(response.body()!!)
            } else {
                retry = { loadRange(params, callback) }
                networkState.postValue(NetworkState.error("error code: ${response.code()}"))
            }
        } catch (e: IOException) {
            retry = { loadRange(params, callback) }
            networkState.postValue(NetworkState.error(e.message ?: "unknown error"))
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Status>) {
        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        when (path) {
            TIMELINE_FAVORITES -> restAPI.fetch_favorites(id = uniqueId, count = params.pageSize, page = 1)
            else -> TODO()
        }.enqueue(object : Callback<MutableList<Status>> {
            override fun onResponse(call: Call<MutableList<Status>>?, response: Response<MutableList<Status>>) {
                if (response.isSuccessful) {
                    if (response.body()?.isNotEmpty() == true) {
                        // clear retry since last request succeeded
                        retry = null
                        callback.onResult(response.body()!!, 0, params.pageSize)
                        networkState.postValue(NetworkState.LOADED)
                        initialLoad.postValue(NetworkState.LOADED)

                    } else {
                        retry = null
                        networkState.postValue(NetworkState.LOADED)
                        initialLoad.postValue(NetworkState.LOADED)
                    }
                } else {
                    retry = {
                        loadInitial(params, callback)
                    }

                    val error = NetworkState.error("error code: ${response.code()}")
                    networkState.postValue(error)
                    initialLoad.postValue(error)
                }
            }

            override fun onFailure(call: Call<MutableList<Status>>?, t: Throwable) {
                // keep a lambda for future retry
                retry = {
                    loadInitial(params, callback)
                }
                // publish the error
                val error = NetworkState.error(t.message ?: "unknown error")
                networkState.postValue(error)
                initialLoad.postValue(error)
            }

        })

    }
}