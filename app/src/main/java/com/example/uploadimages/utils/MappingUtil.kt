package com.example.uploadimages.utils

import com.google.common.reflect.TypeToken
import com.google.gson.Gson

/**
 * Created by Franz Andel <franz.andel@ovo.id>
 * on 21 March 2022.
 */

val gson = Gson()

fun <T> T.serializeToMap(): Map<String, Any> {
 return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
 return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
 val json = gson.toJson(this)
 return gson.fromJson(json, object : TypeToken<O>() {}.type)
}
