package com.example.project2_thegameoflife

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

class GridViewModel : ViewModel() {
    private var grid: Grid? = null

    fun createGrid(cols: Int, rows: Int) {
        grid = Grid(cols, rows)
    }

    fun getGrid(): Grid {
        return grid!!
    }

    fun clearGrid() {
        getGrid().clear()
    }

    fun nextGeneration() {
        getGrid().nextGeneration()
    }

    fun getCell(index: Int): Grid.Cell {
        return getGrid().getCell(index)
    }
}