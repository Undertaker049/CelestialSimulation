import javafx.geometry.BoundingBox
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.util.concurrent.Executors
import kotlin.math.*
import kotlin.random.Random

class StellarSimulation(private val stellarSystem: StellarSystem) {
    private var scaleFactor = 5e-10
    private val backgroundStars = BackgroundStarPool(1200.0, 800.0, 1000)
    private var cameraX = 0.0
    private var cameraY = 0.0
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var objectSizeMultiplier = 50.0
    private var timeScale = 86400.0
    private var elapsedTime = 0.0
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val labelPositions = mutableListOf<Pair<String, BoundingBox>>()

    fun update(deltaTime: Double) {
        val scaledDeltaTime = deltaTime * timeScale
        elapsedTime += scaledDeltaTime
        stellarSystem.update(scaledDeltaTime, executor)
    }

    fun render(gc: GraphicsContext) {
        gc.save()
        gc.clearRect(0.0, 0.0, gc.canvas.width, gc.canvas.height)

        drawBackground(gc)

        val centerX = gc.canvas.width / 2
        val centerY = gc.canvas.height / 2

        labelPositions.clear()

        drawAsteroidBelts(gc, centerX, centerY)
        drawOrbits(gc, centerX, centerY)

        stellarSystem.getObjects().forEach { celestialObject ->
            drawCelestialObject(gc, celestialObject, centerX, centerY)
        }

        drawLabels(gc)

        drawSimulationInfo(gc)

        gc.restore()
    }

    private fun drawBackground(gc: GraphicsContext) {
        gc.fill = Color.rgb(0, 0, 20)
        gc.fillRect(0.0, 0.0, gc.canvas.width, gc.canvas.height)
        for (star in backgroundStars.getStars()) {
            gc.fill = Color.WHITE.deriveColor(1.0, 1.0, 1.0, star.alpha)
            gc.fillOval(star.x, star.y, star.size, star.size)
        }
    }

    private fun drawCelestialObject(gc: GraphicsContext, celestialObject: CelestialObject, centerX: Double, centerY: Double) {
        val x = centerX + (celestialObject.position.x - cameraX) * scaleFactor
        val y = centerY + (celestialObject.position.y - cameraY) * scaleFactor
        val scale = calculateScaleFactor(celestialObject.position.z)

        when (celestialObject) {
            is Star -> {
                drawStar(gc, celestialObject, x, y, scale)
                addLabel(celestialObject.name, x + celestialObject.radius * scaleFactor * scale * objectSizeMultiplier + 5, y)
            }
            is Planet -> {
                drawPlanet(gc, celestialObject, x, y, scale)
                addLabel(celestialObject.name, x + celestialObject.radius * scaleFactor * scale * objectSizeMultiplier + 5, y)
                drawMoons(gc, celestialObject, x, y, scale)
            }
            is Asteroid -> drawAsteroid(gc, celestialObject, x, y, scale)
        }
    }

    private fun drawStar(gc: GraphicsContext, star: Star, x: Double, y: Double, scale: Double) {
        val radius = star.radius * scaleFactor * scale * objectSizeMultiplier
        val glowRadius = radius * 1.2

        if (star.glow == null) {
            star.glow = RadialGradient(
                0.0, 0.0,
                0.5, 0.5, 1.0,
                true,
                CycleMethod.NO_CYCLE,
                Stop(0.0, star.color),
                Stop(1.0, star.color.deriveColor(1.0, 1.0, 1.0, 0.0))
            )
        }

        gc.save()
        gc.translate(x, y)
        gc.fill = star.glow
        gc.fillOval(-glowRadius, -glowRadius, glowRadius * 2, glowRadius * 2)
        gc.fill = star.color
        gc.fillOval(-radius, -radius, radius * 2, radius * 2)
        gc.restore()
    }

    private fun drawPlanet(gc: GraphicsContext, planet: Planet, x: Double, y: Double, scale: Double) {
        val radius = planet.radius * scaleFactor * scale * objectSizeMultiplier
        gc.fill = planet.color
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    }

    private fun drawMoons(gc: GraphicsContext, planet: Planet, planetX: Double, planetY: Double, scale: Double) {
        planet.moons.forEach { moon ->
            val moonOrbitRadius = moon.orbitRadius * scaleFactor * objectSizeMultiplier
            val moonX = planetX + moonOrbitRadius * cos(moon.orbitAngle)
            val moonY = planetY + moonOrbitRadius * sin(moon.orbitAngle)
            drawMoon(gc, moon, moonX, moonY, scale)
            drawMoonOrbit(gc, planetX, planetY, moonOrbitRadius)

            addLabel(moon.name, moonX + moon.radius * scaleFactor * scale * objectSizeMultiplier + 2, moonY)
        }
    }

    private fun drawMoon(gc: GraphicsContext, moon: Moon, x: Double, y: Double, scale: Double) {
        gc.fill = moon.color
        val radius = moon.radius * scaleFactor * scale * objectSizeMultiplier
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    }

    private fun drawMoonOrbit(gc: GraphicsContext, planetX: Double, planetY: Double, orbitRadius: Double) {
        gc.stroke = Color.LIGHTGRAY.deriveColor(1.0, 1.0, 1.0, 0.3)
        gc.lineWidth = 0.5
        gc.strokeOval(
            planetX - orbitRadius,
            planetY - orbitRadius,
            orbitRadius * 2,
            orbitRadius * 2
        )
    }

    private fun drawAsteroid(gc: GraphicsContext, asteroid: Asteroid, x: Double, y: Double, scale: Double) {
        gc.fill = asteroid.color
        val radius = asteroid.radius * scaleFactor * scale * objectSizeMultiplier
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2)
    }

    private fun drawAsteroidBelts(gc: GraphicsContext, centerX: Double, centerY: Double) {
        val sun = stellarSystem.getObjects().find { it is Star } as? Star
        if (sun != null) {
            val sunX = centerX + (sun.position.x - cameraX) * scaleFactor
            val sunY = centerY + (sun.position.y - cameraY) * scaleFactor

            drawAsteroidBelt(gc, sunX, sunY, 2.2, 3.2, 10000, Color.BROWN.deriveColor(1.0, 1.0, 1.0, 0.3))
            drawAsteroidBelt(gc, sunX, sunY, 30.0, 50.0, 20000, Color.LIGHTBLUE.deriveColor(1.0, 1.0, 1.0, 0.2))
        }
    }

    private fun drawAsteroidBelt(gc: GraphicsContext, centerX: Double, centerY: Double,
                                 innerRadius: Double, outerRadius: Double,
                                 count: Int, color: Color) {
        val innerRadiusScaled = innerRadius * 1.496e11 * scaleFactor
        val outerRadiusScaled = outerRadius * 1.496e11 * scaleFactor

        gc.fill = color
        repeat(count) {
            val radius = Random.nextDouble(innerRadiusScaled, outerRadiusScaled)
            val angle = Random.nextDouble() * 2 * PI
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val size = 1.0
            gc.fillOval(x - size / 2, y - size / 2, size, size)
        }
    }

    private fun drawOrbits(gc: GraphicsContext, centerX: Double, centerY: Double) {
        gc.stroke = Color.LIGHTGRAY.deriveColor(1.0, 1.0, 1.0, 0.5)
        gc.lineWidth = 0.5

        val sun = stellarSystem.getObjects().find { it is Star } as? Star
        if (sun != null) {
            val sunX = centerX + (sun.position.x - cameraX) * scaleFactor
            val sunY = centerY + (sun.position.y - cameraY) * scaleFactor

            stellarSystem.getObjects().forEach { celestialObject ->
                if (celestialObject is Planet) {
                    val orbitRadius = celestialObject.position.magnitude() * scaleFactor
                    gc.strokeOval(
                        sunX - orbitRadius,
                        sunY - orbitRadius,
                        orbitRadius * 2,
                        orbitRadius * 2
                    )
                }
            }
        }
    }

    private fun addLabel(text: String, x: Double, y: Double) {
        val textBounds = Text(text).boundsInLocal
        val labelWidth = textBounds.width
        val labelHeight = textBounds.height

        var labelX = x
        var labelY = y

        while (labelPositions.any { it.second.intersects(labelX, labelY, labelWidth, labelHeight) }) {
            labelY += labelHeight + 2
        }

        labelPositions.add(Pair(text, BoundingBox(labelX, labelY, labelWidth, labelHeight)))
    }

    private fun drawLabels(gc: GraphicsContext) {
        gc.font = Font.font(10.0)
        gc.fill = Color.WHITE
        labelPositions.forEach { (text, box) ->
            gc.fillText(text, box.minX, box.minY + box.height)
        }
    }

    private fun drawSimulationInfo(gc: GraphicsContext) {
        gc.fill = Color.WHITE
        gc.font = Font.font(14.0)
        gc.fillText("Elapsed time: ${formatElapsedTime(elapsedTime)}", 10.0, 20.0)
        gc.fillText("Scale: 1:${1/scaleFactor}", 10.0, 40.0)
        gc.fillText("Time scale: ${formatTimeScale(timeScale)}", 10.0, 60.0)
    }

    private fun formatElapsedTime(time: Double): String {
        val days = (time / 86400).toInt()
        val years = days / 365
        val remainingDays = days % 365
        return "$years years, $remainingDays days"
    }

    private fun formatTimeScale(scale: Double): String {
        return when {
            scale < 60 -> "${scale.toInt()} seconds/second"
            scale < 3600 -> "${(scale / 60).toInt()} minutes/second"
            scale < 86400 -> "${(scale / 3600).toInt()} hours/second"
            else -> "${(scale / 86400).toInt()} days/second"
        }
    }

    fun changeTimeScale(factor: Double) {
        timeScale *= factor
        timeScale = timeScale.coerceIn(1.0, 86400.0 * 365.0)
    }

    private fun calculateScaleFactor(z: Double): Double {
        val maxZ = 1e11
        return 1.0 - (z / maxZ).coerceIn(0.0, 0.9)
    }

    fun startCameraMove(mouseX: Double, mouseY: Double) {
        lastMouseX = mouseX
        lastMouseY = mouseY
    }

    fun moveCamera(mouseX: Double, mouseY: Double) {
        val dx = mouseX - lastMouseX
        val dy = mouseY - lastMouseY
        cameraX += dx / scaleFactor
        cameraY += dy / scaleFactor
        lastMouseX = mouseX
        lastMouseY = mouseY
    }

    fun zoom(factor: Double, mouseX: Double, mouseY: Double) {
        val oldScaleFactor = scaleFactor
        scaleFactor *= factor
        scaleFactor = scaleFactor.coerceIn(1e-11, 1e-7)
        cameraX = mouseX - (mouseX - cameraX) * (scaleFactor / oldScaleFactor)
        cameraY = mouseY - (mouseY - cameraY) * (scaleFactor / oldScaleFactor)
        objectSizeMultiplier = 50.0 * (1e-9 / scaleFactor).pow(0.2)
    }

    companion object {
        const val G = 6.67430e-11
    }
}