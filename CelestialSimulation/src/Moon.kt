import javafx.scene.paint.Color
import kotlin.math.*

class Moon(
    mass: Double,
    position: Vector3D,
    velocity: Vector3D,
    radius: Double,
    color: Color,
    val name: String,
    val orbitRadius: Double,
    var orbitAngle: Double = 0.0,
    var parentMass: Double = 0.0,
    var parentPosition: Vector3D = Vector3D.ZERO
) : CelestialObject(mass, position, velocity, radius, color) {

    fun calculateOrbitalPeriod(): Double {
        return 2 * PI * sqrt(orbitRadius.pow(3) / (G * parentMass))
    }

    override fun update(force: Vector3D, deltaTime: Double) {
        val orbitalPeriod = calculateOrbitalPeriod()
        val angularVelocity = 2 * PI / orbitalPeriod
        orbitAngle += angularVelocity * deltaTime
        orbitAngle %= (2 * PI)

        val newX = parentPosition.x + orbitRadius * cos(orbitAngle)
        val newY = parentPosition.y + orbitRadius * sin(orbitAngle)
        position = Vector3D(newX, newY, parentPosition.z)

        val speed = sqrt(G * parentMass / orbitRadius)
        velocity = Vector3D(
            -speed * sin(orbitAngle),
            speed * cos(orbitAngle),
            0.0
        )

        velocity += force / mass * deltaTime
        position += velocity * deltaTime
    }

    companion object {
        const val G = 6.67430e-11
    }
}