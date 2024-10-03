class SpatialGrid(private val gridSize: Double, private val cellCount: Int) {
    private val cellSize = gridSize / cellCount
    private val grid = Array(cellCount) { Array(cellCount) { mutableListOf<CelestialObject>() } }

    fun updateGrid(objects: List<CelestialObject>) {
        grid.forEach { it.forEach { cell -> cell.clear() } }
        objects.forEach { obj ->
            val (x, y) = getCellCoordinates(obj.position)
            if (x in 0 until cellCount && y in 0 until cellCount) {
                grid[x][y].add(obj)
            }
        }
    }

    fun getCells() = grid.flatten()

    fun getNearbyCells(cell: List<CelestialObject>): List<List<CelestialObject>> {
        val (x, y) = getCellCoordinates(cell.firstOrNull()?.position ?: return emptyList())
        return (-1..1).flatMap { dx ->
            (-1..1).mapNotNull { dy ->
                val newX = x + dx
                val newY = y + dy
                if (newX in 0 until cellCount && newY in 0 until cellCount) grid[newX][newY] else null
            }
        }
    }

    private fun getCellCoordinates(position: Vector3D): Pair<Int, Int> {
        val x = ((position.x + gridSize / 2) / cellSize).toInt().coerceIn(0, cellCount - 1)
        val y = ((position.y + gridSize / 2) / cellSize).toInt().coerceIn(0, cellCount - 1)
        return x to y
    }

    fun addObject(obj: CelestialObject) {
        val (x, y) = getCellCoordinates(obj.position)
        if (x in 0 until cellCount && y in 0 until cellCount) {
            grid[x][y].add(obj)
        }
    }
}