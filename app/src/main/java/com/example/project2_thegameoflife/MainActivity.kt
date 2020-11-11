package com.example.project2_thegameoflife

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var grid: Grid

    var backgroundColor = Color.argb(1, 0, 0, 0)
    var cellColor = Color.argb(1, 255, 1, 1)

    private lateinit var gameRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "On Create")

        gameRecyclerView = findViewById(R.id.game_recycler_view)
        findViewById<Button>(R.id.button_next_generation).setOnClickListener {
            getNextGeneration()
        }
        findViewById<Button>(R.id.button_clear).setOnClickListener {
            grid.clear()
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }


        var rows = 10
        var cols = 10
        grid = Grid(cols, rows)

        gameRecyclerView.layoutManager = GridLayoutManager(this, cols)
        gameRecyclerView.adapter = CellAdapter(grid)
    }

    fun getNextGeneration() {
        grid.nextGeneration()
        gameRecyclerView.adapter?.notifyDataSetChanged()
    }

    private inner class CellHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val container = itemView.findViewById<ConstraintLayout>(R.id.layout)

        init {
            //Log.d(TAG, "CellHolder created")
            itemView.setOnClickListener(this)
        }

        fun bind(cell: Cell) {
            if (cell.alive) {
                container.setBackgroundColor(Color.BLACK)
            } else {
                container.setBackgroundColor(Color.WHITE)
            }
        }

        override fun onClick(v: View?) {
            val cell = grid.getCell(this.layoutPosition)
            cell.alive = !cell.alive

            //bind(cell)
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private inner class CellAdapter(var grid: Grid) : RecyclerView.Adapter<CellHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
            //Log.d(TAG, "CellAdapter ViewHolder created")
            val view: View = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
            view.layoutParams.height = parent.measuredWidth / grid.width
            return CellHolder(view)
        }

        override fun onBindViewHolder(holder: CellHolder, position: Int) {
            //Log.d(TAG, "Binding Data at position: $position")
            val cell: Cell = grid.getCell(position)
            holder.bind(cell)
        }

        override fun getItemCount(): Int {
            return grid.count()
        }
    }
}