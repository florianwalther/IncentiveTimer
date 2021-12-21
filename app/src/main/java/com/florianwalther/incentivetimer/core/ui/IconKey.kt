package com.florianwalther.incentivetimer.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class IconKey(val rewardIcon: ImageVector) {
    STAR(Icons.Default.Star),
    CAKE(Icons.Default.Cake),
    BATH_TUB(Icons.Default.Bathtub),
    TV(Icons.Default.Tv),
    FAVORITE(Icons.Default.Favorite),
    PETS(Icons.Default.Pets),
    PHONE(Icons.Default.Phone),
    GIFT_CARD(Icons.Default.CardGiftcard),
    GAME_PAD(Icons.Default.Gamepad),
    MONEY(Icons.Default.Money),
    COMPUTER(Icons.Default.Computer),
    GROUP(Icons.Default.Group),
    HAPPY(Icons.Default.Mood),
    BEVERAGE(Icons.Default.EmojiFoodBeverage),
    MOTORBIKE(Icons.Default.SportsMotorsports),
    FOOTBALL(Icons.Default.SportsFootball),
    HEADPHONES(Icons.Default.Headphones),
    SHOPPING_CART(Icons.Default.ShoppingCart),
    BICYCLE(Icons.Default.DirectionsBike),
    PIZZA(Icons.Default.LocalPizza),

}

val defaultRewardIconKey = IconKey.STAR