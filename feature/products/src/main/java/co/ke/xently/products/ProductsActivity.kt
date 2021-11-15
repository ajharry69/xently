package co.ke.xently.products

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import co.ke.xently.feature.theme.XentlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                }
            }
        }
    }
}