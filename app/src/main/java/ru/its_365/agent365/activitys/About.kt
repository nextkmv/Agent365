package ru.its_365.agent365.activitys

import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_about.*
import ru.its_365.agent365.R
import ru.its_365.agent365.tools.ValueHelper


class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.titles_about)
        setContentView(R.layout.activity_about)

        txtVersion.setText(ValueHelper.getVersionName(this))

        license_text.setText(this.resources.getString(R.string.content_license) + this.resources.getString(R.string.history_version))

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed();
        }
        return super.onOptionsItemSelected(item)
    }
}
