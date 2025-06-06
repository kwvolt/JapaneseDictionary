package io.github.kwvolt.japanesedictionary

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import io.github.kwvolt.japanesedictionary.ui.addUpdate.AddUpdateRecyclerViewFragment


class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null){
            val fragment = AddUpdateRecyclerViewFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)  // Add the fragment to the container
                .commit()  // Commit the transaction
        }
    }
}