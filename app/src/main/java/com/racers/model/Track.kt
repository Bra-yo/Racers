class Track(
    val name: String,
    val length: Double, // Length in kilometers
    val difficulty: Int // Difficulty level from 1 to 10
) {
    fun getTrackInfo(): String {
        return "Track Name: $name, Length: $length km, Difficulty: $difficulty"
    }
}