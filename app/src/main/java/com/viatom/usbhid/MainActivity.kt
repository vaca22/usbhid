package com.viatom.usbhid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.MutableLiveData

class MainActivity : AppCompatActivity() {
    lateinit var text:TextView
    companion object{
        val mm=MutableLiveData<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text=findViewById(R.id.text)
       // startService(Intent(this,Service_usb::class.java))
        mm.observe(this,{
            text.text=it
        })
    }
}