import kotlin.math.*

data class Vector3D(var x: Double, var y: Double, var z: Double) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Double) = Vector3D(x / scalar, y / scalar, z / scalar)
    operator fun unaryMinus() = Vector3D(-x, -y, -z)

    fun magnitude() = sqrt(x * x + y * y + z * z)
    fun magnitudeSquared() = x * x + y * y + z * z
    fun normalize() = this / magnitude()

    fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3D) = Vector3D(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun rotateX(angle: Double): Vector3D {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vector3D(x, y * cos - z * sin, y * sin + z * cos)
    }

    fun rotateY(angle: Double): Vector3D {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vector3D(x * cos + z * sin, y, -x * sin + z * cos)
    }

    fun rotateZ(angle: Double): Vector3D {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vector3D(x * cos - y * sin, x * sin + y * cos, z)
    }

    fun distanceTo(other: Vector3D) = (this - other).magnitude()

    companion object {
        val ZERO = Vector3D(0.0, 0.0, 0.0)

        fun random(min: Double, max: Double): Vector3D {
            return Vector3D(
                Math.random() * (max - min) + min,
                Math.random() * (max - min) + min,
                Math.random() * (max - min) + min
            )
        }
    }

    override fun toString(): String {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z)
    }
}