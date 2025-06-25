package com.vibeconnect

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform