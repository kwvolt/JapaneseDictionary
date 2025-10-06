package io.github.kwvolt.japanesedictionary

import android.animation.LayoutTransition
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible

class CollapsableLayoutManager (
    private val parentContainer: FrameLayout,
    private val nestedContainer: ViewGroup
) {
    private val layoutHeight: Int
    init {
        parentContainer.layoutTransition = LayoutTransition()
        parentContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        this.layoutHeight = parentContainer.layoutParams.height
    }
    fun expandOrCollapse(){
        if(this.nestedContainer.isVisible) {
            this.collapse()
        }
        else {
            this.expand()
        }
    }

    fun collapse(){
        this.adjustLayoutHeight(0, false)
    }

    fun expand(){
        this.adjustLayoutHeight(this.layoutHeight, true)

    }

    fun adjustLayoutHeight(height: Int, visibility: Boolean){
        this.parentContainer.layoutParams.height = height
        this.nestedContainer.isVisible = visibility
        this.parentContainer.requestLayout()

    }

}