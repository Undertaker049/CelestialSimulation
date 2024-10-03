import kotlin.random.Random

class BackgroundStarPool(private val width: Double, private val height: Double, size: Int) {
    private val pool: List<BackgroundStar> = List(size) { createStar() }

    fun getStars(): List<BackgroundStar> = pool

    private fun createStar() = BackgroundStar(
        x = Random.nextDouble() * width,
        y = Random.nextDouble() * height,
        size = Random.nextDouble() * 2 + 0.5,
        alpha = Random.nextDouble() * 0.8 + 0.2
    )
}

data class BackgroundStar(val x: Double, val y: Double, val size: Double, val alpha: Double)