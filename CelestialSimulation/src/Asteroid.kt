import javafx.scene.paint.Color

class Asteroid(
    mass: Double,
    position: Vector3D,
    velocity: Vector3D,
    radius: Double
) : CelestialObject(mass, position, velocity, radius, Color.GRAY) {
    override fun update(force: Vector3D, deltaTime: Double) {
        updatePosition(force, deltaTime)
    }
}