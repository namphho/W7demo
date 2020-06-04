package com.hnam.w7demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hnam.w7demo.location.LocationActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_map.setOnClickListener {
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        }

        btn_location.setOnClickListener {
            val i = Intent(this, LocationActivity::class.java)
            startActivity(i)
        }

        btn_firebase.setOnClickListener {
            val i = Intent(this, FirebaseActivity::class.java)
            startActivity(i)
        }

    }
}
