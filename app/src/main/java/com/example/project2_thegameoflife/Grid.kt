package com.example.project2_thegameoflife

import kotlin.random.Random

class Grid(var width: Int, var height: Int) {
    private var cells = mutableListOf<Cell>() //Array(width * height) { Cell(Random.nextBoolean()) }

    init {
        val size: Int = width * height
        for (index: Int in 0 until size) {
            val cell: Cell = Cell(index, Random.nextBoolean())
            cells.add(cell)

            val x: Int = index % width
            val y: Int = index / width

            for (i: Int in -1..1) {
                for (j: Int in -1..1) {
                    if (i == 0 && j == 0)
                        continue

                    val x2: Int = (x + i + width) % width
                    val y2: Int = (y + j + height) % height
                    val index2: Int = x2 + y2 * width
                    cell.addNeighbor(index2)
                }
            }
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

    fun count(): Int {
        return cells.size
    }

    fun nextGeneration(): MutableList<Int> {
        // pair.first = is alive and pair.second = number of alive neighbors
        val updatedIndices: MutableList<Int> = mutableListOf()
        val current: List<Pair<Boolean, Int>> = cells.map { c ->
            Pair(c.getIsAlive(), c.getAliveNeighborCount())
        }

        for (i: Int in cells.indices) {
            if (current[i].first) {
                if (current[i].second < 2 || current[i].second > 3) {
                    cells[i].setIsAlive(false)
                    updatedIndices.add(i)
                }
            } else {
                if (current[i].second == 3) {
                    cells[i].setIsAlive(true)
                    updatedIndices.add(i)
                }
            }
        }
        return updatedIndices
    }

    inner class Cell(private var index: Int, private var isAlive: Boolean = false) {
        private var neighborIndices: MutableList<Int> = mutableListOf()

        fun getIndex(): Int {
            return index
        }

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

        fun getAliveNeighborCount(): Int {
            return neighborIndices.count { n -> getCell(n).isAlive }
        }
    }
}

