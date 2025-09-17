package com.example.appphotointern.common

const val PREFS_NAME = "MyPrefsAccount"
const val KEY_SEEN_WELCOME = "hasSeenWelcome"
const val KEY_CHOOSE_LANGUAGE = "hasChooseLanguage"
const val SPLASH_DELAY: Long = 2000

// WelcomeFragment
const val ARG_TITLE = "title"
const val ARG_DESCRIPTION = "description"
const val ARG_IMAGE_RESOURCE = "imageResource"

// Uri image
const val IMAGE_URI = "imageUri"
const val outputDirectory = "/PhotoIntern"

// Url firebase storage
const val URL_STORAGE = "app_editor_snapmagic"

// Tag feature
const val TAG_FEATURE_EDIT = 1
const val TAG_FEATURE_CAMERA = 2
const val TAG_FEATURE_ALBUM = 3
const val TAG_FEATURE_ANALYTICS = 4
const val FEATURE_STICKER = "Sticker"
const val FEATURE_TEXT = "Text"

// Result code
const val RESULT_STICKER = 2
const val RESULT_TEXT = 3

// Feature text
const val TEXT_COLOR = 0
const val TEXT_FONT = 1
const val TEXT_DATA_COLOR = "Color"
const val TEXT_DATA_FONT = "Font"

// Ratio camera
const val CUSTOM_RATIO_1_1 = 3
const val CUSTOM_FULL = 4

// EventBus
const val CROP_CLOSED = "CROP_CLOSED"
const val PURCHASED = "PURCHASED"

const val KEY_LANGUAGE = "app_language"

// Remote config
const val KEY_BANNER = "banner_main"
const val KEY_SHOW_BANNER = "show_banner"
const val KEY_BANNER_TITLE = "title"
const val KEY_BANNER_MESSAGE = "message"
const val KEY_BANNER_IMAGE_URL = "image_url"
const val KEY_FRAME_CAMERA = "fea_frame_camera"
const val KEY_FRAME = "frame_main"

// Sticker fragment
const val STICKER_BASIC = 0
const val STICKER_FESTIVAL = 1
const val STICKER_CREATIVE = 2

// Analytics fragment
const val ANALYTICS_FILTER = 0
const val ANALYTICS_STICKER = 1

// Share preference premium
const val PREFS_NAME_BILLING = "billing_prefs"
const val KEY_IS_PREMIUM = "key_is_premium"

// Load fail/success
const val LOAD_SUCCESS = "LOAD_SUCCESS"
const val LOAD_FAIL = "LOAD_FAIL"