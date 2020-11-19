package com.example.project2_thegameoflife

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class GridViewModel : ViewModel() {
    private var grid: Grid? = null
    var cellDeadColor = Color.argb(255, 255, 255, 255)
    var cellAliveColor = Color.argb(255, 200, 0, 0)

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

    fun getGridToJson(): String? {
        val gson = Gson()
        return gson.toJson(getGrid())
    }

    fun setGridFromJson(json: String) {
        val gson = Gson()
        grid = gson.fromJson(json, Grid::class.java)
    }
}