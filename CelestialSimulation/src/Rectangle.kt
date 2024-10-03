data class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double) {
    fun contains(px: Double, py: Double): Boolean {
        return px >= x && px <= x + width && py >= y && py <= y + height
    }
}