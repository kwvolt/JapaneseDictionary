package io.github.kwvolt.japanesedictionary.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> Fragment.viewBinding(factory: (View) -> T): Lazy<T> =
    object : Lazy<T> {
        private var binding: T? = null

        override val value: T
            get() {
                val view = view
                    ?: throw IllegalStateException("Fragment view is null or not yet created.")
                return binding ?: factory(view).also {
                    binding = it
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }

        override fun isInitialized(): Boolean = binding != null
    }