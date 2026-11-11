package com.arcelik.glasslauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 5)

        val apps = getInstalledTvApps()
        recyclerView.adapter = AppAdapter(apps)
    }

    private fun getInstalledTvApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackApps = packageManager.queryIntentActivities(intent, 0)
        
        if (leanbackApps.size < 3) {
            val normalIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            return packageManager.queryIntentActivities(normalIntent, 0)
        }
        return leanbackApps
    }

    inner class AppAdapter(private val appList: List<ResolveInfo>) :
        RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.appIcon)
            val name: TextView = view.findViewById(R.id.appName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_glass, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = appList[position]
            holder.name.text = app.loadLabel(packageManager)
            holder.icon.setImageDrawable(app.loadIcon(packageManager))

            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                val scale = if (hasFocus) 1.12f else 1.0f
                v.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
            }

            holder.itemView.setOnClickListener {
                val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(app.activityInfo.packageName)
                    ?: packageManager.getLaunchIntentForPackage(app.activityInfo.packageName)
                launchIntent?.let { startActivity(it) }
            }
        }

        override fun getItemCount() = appList.size
    }
}
