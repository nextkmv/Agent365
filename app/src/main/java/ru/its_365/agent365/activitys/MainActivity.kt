package ru.its_365.agent365.activitys

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import ru.its_365.agent365.R
import ru.its_365.agent365.tools.Const
import ru.its_365.agent365.tools.PreferenceStore
import ru.its_365.agent365.R.id.toolbar
import android.os.StrictMode





class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener, OnClickListener {


    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerClosed(drawerView: View) {

    }

    override fun onDrawerOpened(drawerView: View) {
        updateText()
    }

    fun updateText(){
        var headerLayout = nav_view.getHeaderView(0)
        headerLayout.findViewById<TextView>(R.id.userName).setText(PreferenceStore.getString(this, Const.SETTINGS_PROFILE_AGENT_NAME))
        headerLayout.findViewById<TextView>(R.id.companyName).setText(PreferenceStore.getString(this, Const.SETTINGS_PROFILE_COMPANY_NAME))
    }

    fun openSettings(){
        val intent = Intent(this,SettingsActivity::class.java);
        startActivity(intent);
    }

    fun openAbout(){
        val intent = Intent(this,About::class.java);
        startActivity(intent);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // INIT REALM
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .name("db.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config)



        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        var headerLayout = nav_view.getHeaderView(0)
        headerLayout.findViewById<TextView>(R.id.userName).setOnClickListener(this)
        headerLayout.findViewById<TextView>(R.id.companyName).setOnClickListener(this)
        //headerLayout.findViewById<ImageButton>(R.id.settingsButton).setOnClickListener(this)
        //headerLayout.findViewById<ImageView>(R.id.userImage).setOnClickListener(this)
        updateText()

        val logedIn = PreferenceStore.getBoolean(this, Const.SETTINGS_PROFILE_LOGED_IN)
        if(!logedIn) {
            openSettings()
        }

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onClick(v: View?) {
        when(v?.id){
            //R.id.settingsButton -> openSettings()
            R.id.userName -> openSettings()
            R.id.companyName -> openSettings()
            R.id.userName -> openSettings()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun changeFragment(f:Fragment,title: String){
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.content_main_content, f, title)
        transaction.commit()
        toolbar.title = title
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_customer -> {
                changeFragment(CustomerFragment(),"Покупатели")
            }

            R.id.nav_orders-> {
                changeFragment(OrderFragment(),"Заказы")
            }

            R.id.nav_manage -> {
                openSettings()
            }

            R.id.nav_about ->  openAbout()

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
