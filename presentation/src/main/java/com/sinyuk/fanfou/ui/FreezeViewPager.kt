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

package com.sinyuk.fanfou.ui

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 *
 */
class FreezeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var freeze: Boolean = false


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.freeze && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.freeze && super.onInterceptTouchEvent(event)
    }

    /**
     * Enable or disable the swipe navigation
     *
     * @param enabled
     */
    fun setPagingEnabled(freezed: Boolean) {
        this.freeze = freezed
    }


}