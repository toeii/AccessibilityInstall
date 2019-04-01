package com.toeii.install


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.toeii.install.accessibility.AccessibilityUtil
import com.toeii.install.accessibility.InstallService
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mEditText: EditText = findViewById(R.id.et_apk_path)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        AccessibilityUtil.checkSetting(this@MainActivity, InstallService::class.java)


        findViewById<View>(R.id.btn_start).setOnClickListener {
            AccessibilityUtil.install(this@MainActivity, File(mEditText!!.getText().toString()))
        }

    }

}
