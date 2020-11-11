package com.example.project2_thegameoflife

class Grid(var width: Int, var height: Int) {
    private var cells: Array<Cell> = Array(width * height) { Cell() }

    init {
        for (index: Int in cells.indices) {
            val cell: Cell = cells[index]
            val x: Int = index % width
            val y: Int = index / width

            for (i: Int in -1..1) {
                for (j: Int in -1..1) {
                    if (i == 0 && j == 0)
                        continue

                    val x2: Int = (x + i + width) % width
                    val y2: Int = (y + j + height) % height
                    val index2: Int = x2 + y2 * width
                    cell.addNeighbor(cells[index2])
                }
            }
        }
    }

    fun clear() {
        for(cell: Cell in cells) {
            cell.alive = false
        }
    }

    fun getCell(index: Int): Cell {
        return cells[index]
    }

    fun count(): Int {
        return cells.size
    }

    fun nextGeneration() {
        // pair.first = is alive and pair.second = number of alive neighbors
        val current: List<Pair<Boolean, Int>> = cells.map { c ->
            Pair(c.alive, c.getAliveNeighborCount())
        }

        for (i: Int in cells.indices) {
            if (current[i].first) {
                cells[i].alive = current[i].second == 2 || current[i].second == 3
            } else {
                cells[i].alive = current[i].second == 3
            }
        }
    }
}

class Cell {
    var alive: Boolean = false
    private var neighbors: MutableList<Cell> = mutableListOf()

    fun addNeighbor(neighbor: Cell) {
        neighbors.add(neighbor)
    }

    fun getNeighbors(): MutableList<Cell> {
        return neighbors
    }

    fun getAliveNeighborCount(): Int {
        return neighbors.count {n -> n.alive}
    }
}