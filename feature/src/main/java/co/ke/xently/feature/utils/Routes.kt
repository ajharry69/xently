package co.ke.xently.feature.utils

@Suppress("RegExpRedundantEscape")
fun String.buildRoute(vararg args: Pair<String, Any>): String {
    /*
    TODO: Consider throwing an error if args have a key that wasn't matched or if route path
      has a (required) argument that's not fulfilled i.e. path still contains path/{xx}/
     */
    var route = this
    for (arg in args) {
        route = route.replace("{${arg.first}}", arg.second.toString())
    }
    val segments = route.split("?", limit = 2)
    val path = segments[0].replace(Regex("\\{.+\\}"), "")
    var queries = ""
    if (segments.size > 1) {
        queries = segments[1].replace(Regex("=\\{.+\\}"), "").split("&").filter { it.contains('=') }
            .joinToString("&")
    }
    return path + if (queries.isNotBlank()) {
        "?$queries"
    } else {
        ""
    }
}

object Routes {
    object Products {
        const val LIST = "products/"
        const val DETAIL = "products/{id}/"
        const val FILTERED_BY_SHOP = "products/shops/{shopId}/"
        override fun toString(): String {
            return javaClass.name
        }

        object Deeplinks {
            const val FILTERED_BY_SHOP = "xently://${Routes.Products.FILTERED_BY_SHOP}"
        }
    }

    object Shops {
        const val LIST = "shops/"
        const val DETAIL = "shops/{id}/?name={name}&moveBack={moveBack}"
        override fun toString(): String {
            return javaClass.name
        }

        object Deeplinks {
            const val DETAIL = "xently://${Routes.Shops.DETAIL}"
        }
    }

    object Account {
        const val PROFILE = "profile/"
        const val SIGN_IN = "signin/?username={username}&password={password}"
        const val SIGN_UP = "signup/?username={username}&password={password}"
        const val PASSWORD_RESET_REQUEST = "request-password-reset/?email={email}"
        const val RESET_PASSWORD = "reset-password/?isChange={isChange}"
        const val VERIFY = "verify-account/?code={code}"
        override fun toString(): String {
            return javaClass.name
        }

        object Deeplinks {
            const val SIGN_IN = "xently://accounts/signin/"
            const val SIGN_UP = "xently://accounts/signup/"
        }
    }

    object ShoppingList {
        object Recommendation {
            const val LIST = "shopping-list/recommendations/{lookupId}/?numberOfItems={numberOfItems}"
            const val FILTER = "shopping-list/recommendation/?itemId={itemId}&group={group}&groupBy={groupBy}"
            override fun toString(): String {
                return javaClass.name
            }
        }

        const val GROUPED = "shopping-list-grouped/"
        const val LIST = "shopping-list/?group={group}&groupBy={groupBy}"
        const val DETAIL = "shopping-list/{id}/"
        override fun toString(): String {
            return javaClass.name
        }
    }
}