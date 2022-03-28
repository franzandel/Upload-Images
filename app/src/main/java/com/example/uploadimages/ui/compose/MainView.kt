package com.example.uploadimages.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.uploadimages.MainViewModel
import com.example.uploadimages.ui.theme.UploadImagesTheme
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

/**
 * Created by Franz Andel <franz.andel@ovo.id>
 * on 21 March 2022.
 */

@Composable
fun MainView(
    onGetPhotoClick: () -> Unit,
    onUploadPhotoClick: () -> Unit,
    onDownloadPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit,
    onSubmitPhotoClick: () -> Unit,
    onRetrievePhotoClick: () -> Unit,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        val context = LocalContext.current.applicationContext
//        Image(
//            bitmap = viewModel.bitmap?.asImageBitmap()
//                ?: ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)!!
//                    .toBitmap().asImageBitmap(),
////            painter = rememberImagePainter(viewModel.imageUrl),
////            painter = rememberImagePainter("https://firebasestorage.googleapis.com/v0/b/upload-images-8607a.appspot.com/o/first_image.jpg?alt=media&token=0cdb3442-222b-4cae-8e75-874e361733e4"),
//            contentDescription = "default image",
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//        )
        LazyRow(contentPadding = PaddingValues(8.dp)) {
            items(items = viewModel.imagesUri) {
                GlideImage(
                    imageModel = it,
                    modifier = Modifier
                        .size(128.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    // shows an image with the circular reveal animation.
                    circularReveal = CircularReveal(duration = 350),
//            shimmerParams = ShimmerParams(
//                baseColor = MaterialTheme.colors.background,
//                highlightColor = Color.Blue,
//                durationMillis = 350,
//                dropOff = 0.65f,
//                tilt = 20f
//            ),
                    // shows an indicator while loading an image.
                    loading = {
                        Box(modifier = Modifier.matchParentSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    },
                    // shows an error text if fail to load an image.
                    failure = {
                        Text(text = "image request failed.")
                    })
            }
        }
        LinearProgressIndicator(
            progress = viewModel.progress,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
        )
        Button(
            onClick = onClearPhotoClick,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Clear")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onGetPhotoClick,
                modifier = Modifier
                    .padding(top = 8.dp)
            ) {
                Text(text = "Get Photo")
            }

            Button(
                onClick = onUploadPhotoClick,
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp),
                enabled = viewModel.uploadEnabled
            ) {
                Text(text = "Upload Photo")
            }

            Button(
                onClick = onDownloadPhotoClick,
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp),
                enabled = viewModel.downloadEnabled
            ) {
                Text(text = "Download Photo")
            }
        }
        TextField(
            value = viewModel.onTitleChanged,
            onValueChange = { viewModel.onTitleChanged = it },
            label = { Text(text = "Title") },
            modifier = Modifier.padding(top = 8.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = viewModel.onDescriptionChanged,
            onValueChange = { viewModel.onDescriptionChanged = it },
            label = { Text(text = "Description") },
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onSubmitPhotoClick,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            enabled = viewModel.submitEnabled
        ) {
            Text(text = "Submit")
        }
        Button(
            onClick = onRetrievePhotoClick,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            enabled = viewModel.retrieveEnabled
        ) {
            Text(text = "Retrieve")
        }
        if (viewModel.showSubmitProgress) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UploadImagesTheme {
        MainView({}, {}, {}, {}, {}, {}, MainViewModel())
    }
}
