package co.ke.xently.shoppinglist.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun ShoppingList(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel,
    loadRemote: Boolean = false,
) {
    viewModel.shouldLoadRemote(loadRemote)
    val result = viewModel.shoppingListResult.collectAsState().value
    val modifier1 = modifier
        .fillMaxHeight()
        .fillMaxWidth()
    if (result.isSuccess) {
        val shoppingList = result.getOrThrow()
        if (shoppingList == null) {
            Box(contentAlignment = Alignment.Center, modifier = modifier1) {
                CircularProgressIndicator()
            }
        } else {
            if (shoppingList.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = modifier1) {
                    Text(text = "You have no shopping list, yet!")
                }
            } else {
                LazyColumn(modifier = modifier1) {
                    items(shoppingList) { item ->
                        Text(text = item.name)
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = modifier1) {
            Text(text = result.exceptionOrNull()?.localizedMessage ?: "An error occurred")
        }
    }
}

@Composable
fun ShoppingListDetail(
    modifier: Modifier = Modifier,
//    viewModel: ShoppingListViewModel = viewModel(),
) {
    Text("Shopping detail...", modifier = modifier)
}