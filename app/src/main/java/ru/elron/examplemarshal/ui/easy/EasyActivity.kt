package ru.elron.examplemarshal.ui.easy

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_easy.*
import ru.elron.examplemarshal.R
import ru.elron.examplemarshal.utils.Marshal

/**
 * Отображает какие есть разрешения, а каких нет
 */
class EasyActivity : AppCompatActivity(R.layout.activity_easy) {
    companion object {
        fun start(activity: FragmentActivity) {
            activity.startActivity((Intent(activity, EasyActivity::class.java)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stringBuilder = StringBuilder()
        stringBuilder.append("INTERNET = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.INTERNET)).append("\n")
        stringBuilder.append("ACCESS_NETWORK_STATE = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.ACCESS_NETWORK_STATE)).append("\n")
        stringBuilder.append("WRITE_EXTERNAL_STORAGE = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)).append("\n")
        stringBuilder.append("READ_PHONE_STATE = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)).append("\n")
        stringBuilder.append("WAKE_LOCK = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.WAKE_LOCK)).append("\n")
        stringBuilder.append("VIBRATE = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.VIBRATE)).append("\n")
        stringBuilder.append("ACCESS_COARSE_LOCATION = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)).append("\n")
        stringBuilder.append("ACCESS_FINE_LOCATION = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)).append("\n")
        stringBuilder.append("ACCESS_WIFI_STATE = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.ACCESS_WIFI_STATE)).append("\n")
        stringBuilder.append("RECEIVE_BOOT_COMPLETED = ")
            .append(Marshal.hasPermissions(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)).append("\n")

        textView.text = stringBuilder.toString()
    }
}
