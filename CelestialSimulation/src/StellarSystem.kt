import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.math.pow

class StellarSystem {
    private val objects = mutableListOf<CelestialObject>()
    private val G = 6.67430e-11
    private val c = 299792458.0
    private val spatialGrid = SpatialGrid(1e12, 10)

    fun update(deltaTime: Double, executor: ExecutorService) {
        val forces = calculateForces(executor)
        val futures = objects.map { obj ->
            executor.submit<Unit> {
                obj.update(forces[obj] ?: Vector3D.ZERO, deltaTime)
            }
        }
        futures.forEach { it.get() }

        objects.filterIsInstance<Planet>().forEach { it.updateMoons(deltaTime) }
        applyPerturbations()
        correctOrbits()
        spatialGrid.updateGrid(objects)
    }

    private fun calculateForces(executor: ExecutorService): Map<CelestialObject, Vector3D> {
        val forces = ConcurrentHashMap<CelestialObject, Vector3D>()
        val futures = mutableListOf<Future<*>>()

        for (cell in spatialGrid.getCells()) {
            futures.add(executor.submit {
                for (obj1 in cell) {
                    var totalForce = Vector3D.ZERO
                    for (nearbyCell in spatialGrid.getNearbyCells(cell)) {
                        for (obj2 in nearbyCell) {
                            if (obj1 != obj2) {
                                totalForce += calculateForce(obj1, obj2)
                            }
                        }
                    }
                    forces.merge(obj1, totalForce) { old, new -> old + new }
                }
            })
        }

        futures.forEach { it.get() }
        return forces
    }

    private fun calculateForce(object1: CelestialObject, object2: CelestialObject): Vector3D {
        val distance = object2.position - object1.position
        val distanceSquared = distance.magnitudeSquared()
        var forceMagnitude = G * object1.mass * object2.mass / distanceSquared
        val schwarzschildRadius = 2 * G * object2.mass / (c * c)
        forceMagnitude *= (1 - schwarzschildRadius / distance.magnitude())
        val precessionFactor = 1 + (3 * G * object2.mass) / (c * c * distance.magnitude())
        val timeDilationFactor = 1 - (2 * G * object2.mass) / (c * c * distance.magnitude())
        forceMagnitude *= precessionFactor * timeDilationFactor

        return distance.normalize() * forceMagnitude
    }

    private fun applyPerturbations() {
        val planets = objects.filterIsInstance<Planet>()
        for (i in planets.indices) {
            for (j in i + 1 until planets.size) {
                applyPlanetaryPerturbation(planets[i], planets[j])
            }
        }
    }

    private fun applyPlanetaryPerturbation(planet1: Planet, planet2: Planet) {
        val distance = planet2.position - planet1.position
        val forceMagnitude = G * planet1.mass * planet2.mass / distance.magnitudeSquared()
        val force = distance.normalize() * forceMagnitude

        planet1.applyForce(force)
        planet2.applyForce(-force)
    }

    private fun correctOrbits() {
        val star = objects.find { it is Star } as? Star ?: return
        objects.filterIsInstance<Planet>().forEach { planet ->
            val r = planet.position - star.position
            val v = planet.velocity

            val a = 1.0 / (2.0 / r.magnitude() - v.magnitudeSquared() / (G * star.mass))
            val e = ((v.cross(r.cross(v)) / (G * star.mass)) - r.normalize()).magnitude()

            if (e > 0.2) {
                val newV = kotlin.math.sqrt(G * star.mass * (2.0 / r.magnitude() - 1.0 / a))
                planet.velocity = r.cross(v).normalize().cross(r.normalize()) * newV
            }
        }
    }

    fun getVisibleObjects(centerX: Double, centerY: Double, width: Double, height: Double, scaleFactor: Double): List<CelestialObject> {
        val visibleRect = Rectangle(
            (centerX - width / 2) / scaleFactor,
            (centerY - height / 2) / scaleFactor,
            width / scaleFactor,
            height / scaleFactor
        )
        return objects.filter { visibleRect.contains(it.position.x, it.position.y) }
    }

    fun addObject(celestialObject: CelestialObject) {
        objects.add(celestialObject)
        spatialGrid.addObject(celestialObject)
    }

    fun getObjects(): List<CelestialObject> = objects.toList()
}