import javafx.scene.paint.Color
import javafx.scene.paint.RadialGradient

class Star(
    mass: Double,
    position: Vector3D,
    radius: Double,
    color: Color,
    val name: String
) : CelestialObject(mass, position, Vector3D.ZERO, radius, color) {
    var glow: RadialGradient? = null

    override fun update(force: Vector3D, deltaTime: Double) {
    }
}