package com.arcelik.glasslauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.appsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 5)

        val apps = getInstalledTvApps()
        recyclerView.adapter = AppAdapter(apps)
    }

    private fun getInstalledTvApps(): List<ResolveInfo> {
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackApps = packageManager.queryIntentActivities(leanbackIntent, 0)

        val normalIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val normalApps = packageManager.queryIntentActivities(normalIntent, 0)

        val combined = (leanbackApps + normalApps)
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

        return combined
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
                val pkg = app.activityInfo.packageName
                val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(pkg)
                    ?: packageManager.getLaunchIntentForPackage(pkg)
                launchIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(it)
                }
            }
        }

        override fun getItemCount() = appList.size
    }
}
