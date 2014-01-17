#!/bin/sh

SRC_DIR="`dirname \"$0\"`"
DST_BASE_DIR="$SRC_DIR/../../res"

SRC="$SRC_DIR/icon.svg"
DST_FILENAME="ic_launcher.png"

process () {
    mkdir -p "$DST_BASE_DIR/drawable-${1}dpi"
    convert \
        -background transparent \
        "$SRC" \
        -gravity center -resize ${2}x${2} -extent ${2}x${2} \
        "$DST_BASE_DIR/drawable-${1}dpi/$DST_FILENAME"
}

process l 36
process m 48
process h 72
process xh 96
process xxh 144
process xxxh 192

