package ru.elron.examplemarshal.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.elron.examplemarshal.R
import ru.elron.examplemarshal.ui.easy.EasyActivity
import ru.elron.examplemarshal.ui.hard.HardActivity
import ru.elron.examplemarshal.ui.medium.MediumActivity

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        easyButton.setOnClickListener { EasyActivity.start(this) }
        mediumButton.setOnClickListener { MediumActivity.start(this) }
        hardButton.setOnClickListener { HardActivity.start(this) }

    }
}
