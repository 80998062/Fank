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

package com.sinyuk.fanfou.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseViewHolder
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Player
import com.sinyuk.fanfou.glide.GlideRequests
import kotlinx.android.synthetic.main.player_list_item.view.*

/**
 * Created by sinyuk on 2018/1/15.
 *
 */
class PlayerItemViewHolder(private val view: View, private val glide: GlideRequests) : BaseViewHolder(view) {
    private val userIdFormat = "@%s"
    fun bind(player: Player?) {
        glide.asBitmap().avatar().load(player?.profileImageUrlLarge).into(view.avatar)
        view.screenName.text = player?.screenName
        view.userId.text = String.format(userIdFormat, player?.id)
    }

    fun clear() {

    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): PlayerItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.player_list_item, parent, false)
            return PlayerItemViewHolder(view, glide)
        }
    }
}
