package com.example.project2_thegameoflife

import kotlin.random.Random

class Grid(var cols: Int, var rows: Int) {
    private var cells:Array<Cell> = emptyArray() //Array(width * height) { Cell(Random.nextBoolean()) }
    var size = cols * rows

    init {
        cells = Array(size) {index ->
            val cell = Cell(Random.nextBoolean())

            val x: Int = index % cols
            val y: Int = index / cols

            for (i: Int in -1..1) {
                for (j: Int in -1..1) {
                    if (i == 0 && j == 0)
                        continue

                    val x2: Int = (x + i + cols) % cols
                    val y2: Int = (y + j + rows) % rows
                    val index2: Int = x2 + y2 * cols
                    cell.addNeighbor(index2)
                }
            }

            return@Array cell
        }
    }

    fun clear() {
        for(i in cells.indices) {
            cells[i].setIsAlive(false)
        }
    }

    fun getCell(index: Int): Cell {
        return cells[index]
    }

    fun getAliveNeighborCount(cellIndex: Int) : Int {
        val cell = getCell(cellIndex)
        val neighbors = cell.getNeighbors()

        var count = 0
        for (n in neighbors) {
            if (getCell(n).getIsAlive())
                count++
        }

        return count
    }

    fun nextGeneration(): MutableList<Int> {
        // pair.first = is alive and pair.second = number of alive neighbors
        val updatedIndices: MutableList<Int> = mutableListOf()
        val currentAlive: Array<Boolean> = Array(cells.size) { false }
        val currentAliveNeighbors: Array<Int> = Array(cells.size) {0}

        for (i: Int in cells.indices) {
            currentAlive[i] = cells[i].getIsAlive()
            currentAliveNeighbors[i] = getAliveNeighborCount(i)
        }

        for (i: Int in cells.indices) {
            if (currentAlive[i]) {
                if (currentAliveNeighbors[i] < 2 || currentAliveNeighbors[i] > 3) {
                    cells[i].setIsAlive(false)
                    updatedIndices.add(i)
                }
            } else {
                if (currentAliveNeighbors[i] == 3) {
                    cells[i].setIsAlive(true)
                    updatedIndices.add(i)
                }
            }
        }

        return updatedIndices
    }

    inner class Cell(private var isAlive: Boolean = false) {
        private var neighborIndices: MutableList<Int> = mutableListOf()

        fun getIsAlive(): Boolean {
            return isAlive
        }

        fun setIsAlive(value: Boolean) {
            if (isAlive == value)
                return
            isAlive = value
        }

        fun toggleIsAlive() {
            setIsAlive(!isAlive)
        }

        fun addNeighbor(neighborIndex: Int) {
            neighborIndices.add(neighborIndex)
        }

        fun getNeighbors(): MutableList<Int> {
            return neighborIndices
        }
    }
}

