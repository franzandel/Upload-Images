package com.example.uploadimages

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

/**
 * Created by Franz Andel <franz.andel@ovo.id>
 * on 08 March 2022.
 */

class MainViewModel : ViewModel() {

    //    var bitmap by mutableStateOf<Bitmap?>(null)
    var imageUri by mutableStateOf<Uri?>(null)
    var imagesUri by mutableStateOf<List<Uri>>(listOf())
    var progress by mutableStateOf(0F)
    var uploadEnabled by mutableStateOf(true)
    var downloadEnabled by mutableStateOf(true)
    var onTitleChanged by mutableStateOf(TextFieldValue())
    var onDescriptionChanged by mutableStateOf(TextFieldValue())
    var submitEnabled by mutableStateOf(true)
    var showSubmitProgress by mutableStateOf(false)
    var retrieveEnabled by mutableStateOf(true)
}
