class Car(
    val model: String,
    val speed: Int,
    val color: String
) {
    fun accelerate(increment: Int) {
        // Increase the speed of the car
        speed += increment
    }

    fun brake(decrement: Int) {
        // Decrease the speed of the car
        speed = (speed - decrement).coerceAtLeast(0)
    }

    fun getInfo(): String {
        return "Model: $model, Speed: $speed, Color: $color"
    }
}