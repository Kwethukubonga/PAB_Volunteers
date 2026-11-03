package com.kantu.pab_volunteers.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens directions to an activity's place in whatever maps app the phone has, or in the browser
 * when there is none. Nothing about where the volunteer is goes through the app.
 */
object Directions {

    fun open(context: Context, place: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUri(place))))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl(place))))
        }
    }

    fun mapsUri(place: String): String = "geo:0,0?q=" + Uri.encode(place.trim())

    /** Works on any phone with a browser; kept apart so it can be tested. */
    fun webUrl(place: String): String =
        "https://www.google.com/maps/search/?api=1&query=" + java.net.URLEncoder.encode(place.trim(), "UTF-8")
}
