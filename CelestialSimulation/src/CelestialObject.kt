import javafx.scene.paint.Color

abstract class CelestialObject(
    val mass: Double,
    var position: Vector3D,
    var velocity: Vector3D,
    val radius: Double,
    val color: Color
) {
    abstract fun update(force: Vector3D, deltaTime: Double)

    protected fun updatePosition(force: Vector3D, deltaTime: Double) {
        val acceleration = force / mass
        velocity += acceleration * deltaTime
        position += velocity * deltaTime
    }
}