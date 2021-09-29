package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.RecommendationReport.Recommendation
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*


@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel,
    navController: NavHostController,
    loadRemote: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    viewModel.shouldLoadRemote(loadRemote)
    val groupedShoppingListResult = viewModel.groupedShoppingListResult.collectAsState().value
    val groupedShoppingListCount = viewModel.groupedShoppingListCount.collectAsState().value
    var groupToRecommend by remember { mutableStateOf<Any?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Xently") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        sheetContent = {
            if (groupToRecommend != null) {
                ShoppingListRecommendationScreen(
                    group = groupToRecommend!!,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                )
            }
        },
        sheetPeekHeight = 0.dp,
    ) {
        if (groupedShoppingListResult.isSuccess) {
            val groupedShoppingList = groupedShoppingListResult.getOrThrow()
            when {
                groupedShoppingList == null -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        CircularProgressIndicator()
                    }
                }
                groupedShoppingList.isEmpty() -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        Text(text = stringResource(R.string.fsl_empty_shopping_list))
                    }
                }
                else -> {
                    LazyColumn(modifier = modifier) {
                        items(groupedShoppingList) { groupList ->
                            GroupedShoppingListCard(
                                groupList, groupedShoppingListCount, navController,
                                onRecommendGroupClicked = { group ->
                                    coroutineScope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                    groupToRecommend = group
                                },
                            )
                        }
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = modifier) {
                Text(
                    text = groupedShoppingListResult.exceptionOrNull()?.localizedMessage
                        ?: stringResource(R.string.fsl_generic_error_message)
                )
            }
        }
    }
}

@Composable
private fun ShoppingListRecommendationScreen(
    group: Any,
    viewModel: ShoppingListViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val recommendationReportResult by viewModel.getRecommendations(group = group)
        .collectAsState(Result.success(null), coroutineScope.coroutineContext)

    if (recommendationReportResult.isSuccess) {
        when (val report = recommendationReportResult.getOrThrow()) {
            null -> {
                Box(contentAlignment = Alignment.Center, modifier = modifier) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.fsl_data_loading))
                    }
                }
            }
            else -> {
                LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        // TODO: Show map
                        RecommendationReportItemGroup(title = "Synopsis") {
                            RecommendationReportSynopsisCard(
                                report = report,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                            )
                        }
                    }
                    if (report.count.hitItems > 0) {
                        item {
                            RecommendationReportItemGroup(title = "Recommendations") {
                                Column {
                                    report.recommendations.forEach {
                                        RecommendationCardItem(
                                            recommendation = it,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (report.count.missedItems > 0) {
                        item {
                            RecommendationReportItemGroup(title = "Missed items") {
                                report.missedItems.forEach { item ->
                                    ShoppingListCardItem(
                                        item = item,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        // TODO: Implement click listener...
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = modifier) {
            Text(
                text = recommendationReportResult.exceptionOrNull()?.localizedMessage
                    ?: stringResource(R.string.fsl_generic_error_message)
            )
        }
    }
}

@Composable
private fun GroupedShoppingListCard(
    groupList: GroupedShoppingList,
    listCount: Map<Any, Int>,
    navController: NavHostController? = null,
    onRecommendGroupClicked: (group: Any) -> Unit = {},
    onDuplicateGroupClicked: (group: Any) -> Unit = {},
    onDeleteGroupClicked: (group: Any) -> Unit = {},
    onSeeAllClicked: (group: Any) -> Unit = {},
) {
    val itemsPerCard = 3
    var showDropDownMenu by remember { mutableStateOf(false) }
    val numberOfItems = listCount.getOrElse(groupList.group) { groupList.numberOfItems }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(start = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = groupList.group, style = MaterialTheme.typography.h6)
                    Text(
                        text = LocalContext.current.resources.getQuantityString(
                            R.plurals.fsl_group_items_count,
                            numberOfItems, numberOfItems
                        ), style = MaterialTheme.typography.subtitle2
                    )
                }
                Box(modifier = Modifier.align(Alignment.Top)) {
                    IconButton(onClick = { showDropDownMenu = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.fsl_group_card_menu_content_desc_more)
                        )
                    }
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onRecommendGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_recommend)) }
                        DropdownMenuItem(
                            onClick = {
                                onDuplicateGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_duplicate)) }
                        DropdownMenuItem(
                            onClick = {
                                onDeleteGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_delete)) }
                    }
                }
            }
            Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                for (item in groupList.shoppingList.take(itemsPerCard)) {
                    ShoppingListCardItem(
                        item, modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .clickable {
                                navController?.navigate("shopping-list/${item.id}")
                            }
                    )
                }
            }
            if (numberOfItems > itemsPerCard) {
                OutlinedButton(
                    onClick = { onSeeAllClicked(groupList.group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fsl_group_button_see_all),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCardItem(
    item: ShoppingListItem, modifier: Modifier = Modifier,
    onRecommendClicked: ((id: Long) -> Unit) = {},
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.body1)
            Text(
                text = "${item.unitQuantity} ${item.unit}",
                style = MaterialTheme.typography.caption
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.purchaseQuantity}", style = MaterialTheme.typography.h6)
            Box {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(
                        if (showDropMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "${item.name} shopping list item options"
                    )
                }
                DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                    DropdownMenuItem(
                        onClick = {
                            onRecommendClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fsl_group_menu_recommend)) }
                    DropdownMenuItem(
                        onClick = {
                            onDeleteClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fsl_group_menu_delete)) }
                }
            }
        }
    }
}

@Composable
fun RecommendationReportItemGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(text = title.uppercase(), style = MaterialTheme.typography.h6)
        content()
    }
}

@Composable
private fun RecommendationReportSynopsisCard(
    modifier: Modifier = Modifier,
    report: RecommendationReport
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(end = 8.dp)
        ) {
            mutableListOf<Pair<String, String>>().apply {
                if (report.count.hitItems > 0) add("Hits" to "${report.count.hitItems}")
                if (report.count.missedItems > 0) add("Misses" to "${report.count.missedItems}")
                if (report.count.shopsVisited > 0) add("Shops visited" to "${report.count.shopsVisited}")
                if (report.count.recommendations > 0) add("Recommendations" to "${report.count.recommendations}")
                add("Lookup duration" to DecimalFormat("###,###.##s").format(report.lookupDuration))
            }.forEach {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it.first, modifier = Modifier.weight(2f))
                    Text(text = it.second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardItem(
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
    onDirectionClicked: ((Recommendation) -> Unit) = {},
    onHitsClicked: ((Recommendation) -> Unit) = {},
    onDetailsClicked: ((Recommendation) -> Unit) = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(text = recommendation.name, style = MaterialTheme.typography.body1)
            val totalPrice = DecimalFormat("KES ###,###.##").format(recommendation.hits.totalPrice)
            Text(
                text = "$totalPrice | ${recommendation.estimatedDistance} away",
                style = MaterialTheme.typography.caption
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${recommendation.hits.count}", style = MaterialTheme.typography.h6)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        if (showMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "${recommendation.name} recommendation options",
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        onClick = {
                            onDirectionClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Directions") }
                    DropdownMenuItem(
                        onClick = {
                            onHitsClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Hits") }
                    DropdownMenuItem(
                        onClick = {
                            onDetailsClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Details") }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun GroupedShoppingListCardPreview() {
    val shoppingList = listOf(
        ShoppingListItem(1L, "Bread", "grams", 400f, 1f, Date()),
        ShoppingListItem(2L, "Milk", "litres", 1f, 1f, Date()),
        ShoppingListItem(3L, "Sugar", "kilograms", 2f, 1f, Date()),
        ShoppingListItem(4L, "Toothpaste", "millilitres", 75f, 1f, Date()),
        ShoppingListItem(5L, "Book", "piece", 1f, 1f, Date()),
    )
    GroupedShoppingListCard(
        GroupedShoppingList(group = "2021-09-29", shoppingList = shoppingList),
        mapOf(Pair("2021-09-29", shoppingList.size)),
    )
}