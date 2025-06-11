package com.rafi607062330092.assesment3.model

data class BukuStatus(
    var success: Boolean,
    var message: String,
    var data: List<Buku>
)