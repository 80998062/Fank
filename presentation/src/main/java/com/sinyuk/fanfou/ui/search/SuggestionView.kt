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

package com.sinyuk.fanfou.ui.search

import android.os.Bundle
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.base.AbstractFragment
import com.sinyuk.fanfou.di.Injectable
import com.sinyuk.fanfou.ui.NestedScrollCoordinatorLayout
import kotlinx.android.synthetic.main.suggestion_view.*

/**
 * Created by sinyuk on 2018/1/3.
 *
 */
class SuggestionView : AbstractFragment(), Injectable {


    override fun layoutId() = R.layout.suggestion_view

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        setupKeyboard()
        coordinator.setPassMode(NestedScrollCoordinatorLayout.PASS_MODE_BOTH)
        loadRootFragment(R.id.historyViewContainer, HistoryView.newInstance(true))
    }


    private fun setupKeyboard() {
        KeyboardUtil.attach(activity, panelRoot) {}
    }

}