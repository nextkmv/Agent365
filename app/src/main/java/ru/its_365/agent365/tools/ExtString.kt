package ru.its_365.agent365.tools

fun String.isNotFreeUid() : Boolean{
    return this != "00000000-0000-0000-0000-000000000000"
}