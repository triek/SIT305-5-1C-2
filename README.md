This project includes two Android applications developed for SIT305 Task 5.1, focusing on media and content handling
* App 1: Sports News Feed App
    * Built using single activity architecture with fragments
    * Home screen shows:
        * Horizontal list for featured matches
        * Vertical list for latest sports news
    * Clicking an item opens a detail screen with image, title, description, and related stories
    * Includes search/filter by sport category
    * Includes bookmark feature to save and view favourite stories locally
* App 2: iStream Video Playlist App
    * Includes login and signup system using Room database
    * Stores and validates user credentials locally
    * Users can enter a YouTube URL and play video using YouTube iFrame Player API
    * Users can save videos to a personal playlist
    * Playlist is user-specific and not shared between accounts
    * Includes logout functionality
    * Handles invalid URLs with error messages
* Technologies used
    * Kotlin
    * Android Studio
    * RecyclerView
    * Fragments
    * Room Database
    * YouTube iFrame Player API
* Key concepts demonstrated
    * Fragment-based navigation
    * Dynamic list rendering
    * Local data storage
    * User authentication
    * Media playback integration