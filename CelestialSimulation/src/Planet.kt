import javafx.scene.paint.Color
import kotlin.math.cos
import kotlin.math.sin

class Planet(
    mass: Double,
    position: Vector3D,
    velocity: Vector3D,
    radius: Double,
    color: Color,
    val name: String
) : CelestialObject(mass, position, velocity, radius, color) {
    val moons = mutableListOf<Moon>()

    fun addMoon(moon: Moon) {
        moons.add(moon)
        updateMoonPosition(moon)
    }

    private fun updateMoonPosition(moon: Moon) {
        moon.position = position + Vector3D(
            moon.orbitRadius * cos(moon.orbitAngle),
            moon.orbitRadius * sin(moon.orbitAngle),
            0.0
        )
    }

    override fun update(force: Vector3D, deltaTime: Double) {
        updatePosition(force, deltaTime)
        moons.forEach { moon ->
            moon.update(Vector3D.ZERO, deltaTime)
            updateMoonPosition(moon)
        }
    }

    fun updateMoons(deltaTime: Double) {
        moons.forEach { moon ->
            val relativePosition = moon.position - position
            val force = StellarSimulation.G * mass * moon.mass / relativePosition.magnitudeSquared()
            val acceleration = relativePosition.normalize() * force / moon.mass
            moon.velocity += acceleration * deltaTime
            moon.position += moon.velocity * deltaTime
        }
    }

    fun applyForce(force: Vector3D) {
        velocity += force / mass
    }
}