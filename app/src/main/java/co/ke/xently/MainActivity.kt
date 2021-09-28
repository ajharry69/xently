package co.ke.xently

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import co.ke.xently.shoppinglist.ShoppingListActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Implement async start - https://developer.android.com/jetpack/compose/side-effects#rememberupdatedstate
        Intent(this, ShoppingListActivity::class.java).run {
            startActivity(this)
            if (!isFinishing) finish()
        }
    }
}