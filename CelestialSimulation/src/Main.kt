import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.animation.AnimationTimer
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import kotlin.math.*
import kotlin.random.Random

class Main : Application() {
    private lateinit var simulation: StellarSimulation

    override fun start(stage: Stage) {
        val canvas = Canvas(1200.0, 800.0)
        val gc = canvas.graphicsContext2D

        val root = StackPane(canvas)
        val scene = Scene(root)

        stage.title = "Celestial Simulation"
        stage.scene = scene
        stage.show()

        simulation = StellarSimulation(createSolarSystem())

        canvas.setOnMousePressed { event ->
            canvas.requestFocus()
            simulation.startCameraMove(event.x, event.y)
        }

        canvas.setOnMouseDragged { event ->
            simulation.moveCamera(event.x, event.y)
        }

        canvas.setOnScroll { event ->
            val zoomFactor = if (event.deltaY > 0) 1.1 else 0.9
            simulation.zoom(zoomFactor, event.x, event.y)
        }

        canvas.setOnKeyPressed { event: KeyEvent ->
            when (event.code) {
                KeyCode.UP -> simulation.changeTimeScale(2.0)
                KeyCode.DOWN -> simulation.changeTimeScale(0.5)
                else -> {}
            }
        }

        canvas.isFocusTraversable = true

        object : AnimationTimer() {
            private var lastUpdate = System.nanoTime()

            override fun handle(now: Long) {
                val elapsedTime = (now - lastUpdate) / 1e9
                lastUpdate = now

                simulation.update(elapsedTime)
                simulation.render(gc)
            }
        }.start()
    }

    private fun createSolarSystem(): StellarSystem {
        val system = StellarSystem()

        // Sun
        val sun = Star(1.989e30, Vector3D(0.0, 0.0, 0.0), 696340e3, Color.YELLOW, "Sun")
        system.addObject(sun)

        // Mercury
        val mercury = Planet(3.3011e23, Vector3D(57.9e9, 0.0, 0.0), Vector3D(0.0, 47.36e3, 0.0), 2439.7e3, Color.GRAY, "Mercury")
        system.addObject(mercury)

        // Venus
        val venus = Planet(4.8675e24, Vector3D(108.2e9, 0.0, 0.0), Vector3D(0.0, 35.02e3, 0.0), 6051.8e3, Color.ORANGE, "Venus")
        system.addObject(venus)

        // Earth
        val earth = Planet(5.97237e24, Vector3D(149.6e9, 0.0, 0.0), Vector3D(0.0, 29.78e3, 0.0), 6371.0e3, Color.BLUE, "Earth")
        system.addObject(earth)

        val moon = Moon(7.342e22, Vector3D.ZERO, Vector3D(0.0, 1.022e3, 0.0), 1737.1e3, Color.LIGHTGRAY, "Moon", 384400e3)
        earth.addMoon(moon)

        // Mars
        val mars = Planet(6.4171e23, Vector3D(227.9e9, 0.0, 0.0), Vector3D(0.0, 24.07e3, 0.0), 3389.5e3, Color.RED, "Mars")
        system.addObject(mars)

        val phobos = Moon(1.0659e16, Vector3D.ZERO, Vector3D(0.0, 2.138e3, 0.0), 11.2667e3, Color.LIGHTGRAY, "Phobos", 9377.2e3)
        mars.addMoon(phobos)

        val deimos = Moon(1.4762e15, Vector3D.ZERO, Vector3D(0.0, 1.351e3, 0.0), 6.2e3, Color.LIGHTGRAY, "Deimos", 23460e3)
        mars.addMoon(deimos)

        // Jupiter
        val jupiter = Planet(1.8982e27, Vector3D(778.5e9, 0.0, 0.0), Vector3D(0.0, 13.07e3, 0.0), 69911e3, Color.ORANGE, "Jupiter")
        system.addObject(jupiter)

        val io = Moon(8.9319e22, Vector3D.ZERO, Vector3D(0.0, 17.334e3, 0.0), 1821.6e3, Color.YELLOW, "Io", 421700e3)
        jupiter.addMoon(io)

        val europa = Moon(4.7998e22, Vector3D.ZERO, Vector3D(0.0, 13.740e3, 0.0), 1560.8e3, Color.LIGHTGRAY, "Europa", 671100e3)
        jupiter.addMoon(europa)

        val ganymede = Moon(1.4819e23, Vector3D.ZERO, Vector3D(0.0, 10.880e3, 0.0), 2634.1e3, Color.LIGHTGRAY, "Ganymede", 1070400e3)
        jupiter.addMoon(ganymede)

        val callisto = Moon(1.0759e23, Vector3D.ZERO, Vector3D(0.0, 8.204e3, 0.0), 2410.3e3, Color.GRAY, "Callisto", 1882700e3)
        jupiter.addMoon(callisto)

        // Saturn
        val saturn = Planet(5.6834e26, Vector3D(1.434e12, 0.0, 0.0), Vector3D(0.0, 9.68e3, 0.0), 58232e3, Color.YELLOW, "Saturn")
        system.addObject(saturn)

        val titan = Moon(1.3452e23, Vector3D.ZERO, Vector3D(0.0, 5.570e3, 0.0), 2574.7e3, Color.ORANGE, "Titan", 1221870e3)
        saturn.addMoon(titan)

        val rhea = Moon(2.3065e21, Vector3D.ZERO, Vector3D(0.0, 8.48e3, 0.0), 763.8e3, Color.LIGHTGRAY, "Rhea", 527108e3)
        saturn.addMoon(rhea)

        // Uranus
        val uranus = Planet(8.6810e25, Vector3D(2.871e12, 0.0, 0.0), Vector3D(0.0, 6.80e3, 0.0), 25362e3, Color.LIGHTBLUE, "Uranus")
        system.addObject(uranus)

        val titania = Moon(3.4e21, Vector3D.ZERO, Vector3D(0.0, 3.64e3, 0.0), 788.9e3, Color.GRAY, "Titania", 436300e3)
        uranus.addMoon(titania)

        val oberon = Moon(3.076e21, Vector3D.ZERO, Vector3D(0.0, 3.15e3, 0.0), 761.4e3, Color.GRAY, "Oberon", 583500e3)
        uranus.addMoon(oberon)

        // Neptune
        val neptune = Planet(1.02413e26, Vector3D(4.495e12, 0.0, 0.0), Vector3D(0.0, 5.43e3, 0.0), 24622e3, Color.BLUE, "Neptune")
        system.addObject(neptune)

        val triton = Moon(2.14e22, Vector3D.ZERO, Vector3D(0.0, -4.39e3, 0.0), 1353.4e3, Color.PINK, "Triton", 354759e3)
        neptune.addMoon(triton)

        // Add asteroid belts
        val asteroidBelt = createAsteroidBelt(2.2, 3.2, 1000)
        asteroidBelt.forEach { system.addObject(it) }

        val kuiperBelt = createAsteroidBelt(30.0, 50.0, 2000)
        kuiperBelt.forEach { system.addObject(it) }

        return system
    }

    private fun createAsteroidBelt(innerRadius: Double, outerRadius: Double, count: Int): List<Asteroid> {
        val asteroids = mutableListOf<Asteroid>()
        val random = Random.Default
        for (i in 0 until count) {
            val radius = innerRadius + (outerRadius - innerRadius) * random.nextDouble()
            val angle = random.nextDouble() * 2 * PI
            val x = radius * cos(angle) * 1.496e11 // convert AU to meters
            val y = radius * sin(angle) * 1.496e11
            val z = random.nextDouble(-1e10, 1e10)
            val position = Vector3D(x, y, z)

            val speed = sqrt(G * 1.989e30 / (radius * 1.496e11)) // calculate orbital velocity
            val velocityX = -speed * sin(angle)
            val velocityY = speed * cos(angle)
            val velocity = Vector3D(velocityX, velocityY, 0.0)

            val mass = random.nextDouble(1e13, 1e17)
            val asteroidRadius = (mass / (4.0 / 3.0 * PI * 3000.0)).pow(1.0 / 3.0) // assume average density of 3000 kg/m³

            asteroids.add(Asteroid(mass, position, velocity, asteroidRadius))
        }
        return asteroids
    }

    companion object {
        const val G = 6.67430e-11 // Gravitational constant
    }
}

fun main() {
    Application.launch(Main::class.java)
}