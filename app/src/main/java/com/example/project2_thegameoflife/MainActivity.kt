package com.example.project2_thegameoflife

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.coroutines.*
import kotlin.math.abs
import kotlin.math.log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var runnable: Runnable

    private lateinit var grid: Grid

    var backgroundColor = Color.argb(1, 0, 0, 0)
    var cellColor = Color.argb(1, 255, 1, 1)
    var isPlaying: Boolean = false
    var updateGridTimer: Long = 1000L

    private lateinit var mainHandler: Handler
    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var startStopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "On Create")

        mainHandler = Handler(Looper.getMainLooper())
        gameRecyclerView = findViewById(R.id.game_recycler_view)
        startStopButton = findViewById<Button>(R.id.button_next_generation)
        startStopButton.setOnClickListener {
            toggleGameLoop()
        }

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            grid.clear()
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }


        var rows = 20
        var cols = 20
        grid = Grid(cols, rows)

        gameRecyclerView.layoutManager = GridLayoutManager(this, cols)
        gameRecyclerView.adapter = CellAdapter(grid)

        runnable = Runnable {
            run {
                update()
                mainHandler.postDelayed(runnable, updateGridTimer)
            }
        }

        mainHandler.postDelayed(runnable, updateGridTimer)
    }

    private fun toggleGameLoop() {
        isPlaying = !isPlaying
        startStopButton.text = getString(if (isPlaying) R.string.stop else R.string.start)
    }

    private fun update() {
        if (!isPlaying)
            return

        // Update the to the next generation
        getNextGeneration()

        // animate all alive cells

    }

    private fun getNextGeneration() {
        val updated: MutableList<Int> = grid.nextGeneration()
        for (i in updated) {
            gameRecyclerView.adapter?.notifyItemChanged(i)
        }
    }

    inner class CellHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val image: ImageView = itemView.findViewById(R.id.imageView)

        init {
            //Log.d(TAG, "CellHolder created")
            itemView.setOnClickListener(this)
        }

        fun bind(isAlive: Boolean) {
            val color = if (isAlive) Color.BLACK else Color.WHITE
            image.apply {
                setColorFilter(color)
                jumpDrawablesToCurrentState()
            }
        }

        override fun onClick(v: View?) {
            val cell = grid.getCell(this.layoutPosition)
            cell.toggleIsAlive()
            bind(cell.getIsAlive())
            //gameRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    inner class CellAdapter(var grid: Grid) : RecyclerView.Adapter<CellHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
            //Log.d(TAG, "CellAdapter ViewHolder created")
            val view: View = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
            view.layoutParams.height = parent.measuredWidth / grid.width
            return CellHolder(view)
        }

        override fun onBindViewHolder(holder: CellHolder, position: Int) {
            //Log.d(TAG, "Binding Data at position: $position")
            val cell: Grid.Cell = grid.getCell(position)
            holder.bind(cell.getIsAlive())
        }

        override fun getItemCount(): Int {
            return grid.count()
        }
    }
}