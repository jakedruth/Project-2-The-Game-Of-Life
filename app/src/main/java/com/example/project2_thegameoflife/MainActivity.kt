package com.example.project2_thegameoflife

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val gridViewModel: GridViewModel by lazy {
        return@lazy GridViewModel()
    }

    var cellDeadColor = Color.argb(255, 255, 255, 255)
    var cellAliveColor = Color.argb(255, 255, 0, 0)
    var isPlaying: Boolean = false
    var updateGridTimer: Long = 1000L

    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var startStopButton: Button
    private lateinit var mainHandler: Handler
    private lateinit var runnable: Runnable

    private var adapter: CellAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "On Create")

        val rows = 20
        val cols = 20

        gameRecyclerView = findViewById(R.id.game_recycler_view)
        startStopButton = findViewById<Button>(R.id.button_next_generation)
        mainHandler = Handler(Looper.getMainLooper())

        startStopButton.setOnClickListener {
            toggleGameLoop()
        }
        findViewById<Button>(R.id.button_clear).setOnClickListener {
            gridViewModel.clearGrid()
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }

        gridViewModel.createGrid(cols, rows)
        gameRecyclerView.layoutManager = GridLayoutManager(this, cols)
        adapter = CellAdapter(gridViewModel.getGrid())
        gameRecyclerView.adapter = adapter
    }

    private fun updateUI(grid: Grid) {
        adapter = CellAdapter(grid)
        gameRecyclerView.adapter = adapter
    }

    private fun toggleGameLoop() {
        if (isPlaying) {
            mainHandler.removeCallbacks(runnable)
        } else {
            runnable = Runnable {
                run {
                    update()
                    mainHandler.postDelayed(runnable, updateGridTimer)
                }
            }

            mainHandler.post(runnable)
        }

        isPlaying = !isPlaying
        startStopButton.text = getString(if (isPlaying) R.string.stop else R.string.start)
    }

    private fun update() {
        // Update the to the next generation
        getNextGeneration()

        // Update UI
        updateUI(gridViewModel.getGrid())
    }

    private fun getNextGeneration() {
        Log.d(TAG, "Getting the next generation")
        gridViewModel.nextGeneration()
    }

    inner class CellHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val image: ImageView = itemView.findViewById(R.id.imageView)
        private var isAlive: Boolean = false
        private var test: Boolean = false

        init {
            //Log.d(TAG, "CellHolder created")
            itemView.setOnClickListener(this)
        }

        fun bind(value: Boolean) {
            isAlive = value
            val color = if (isAlive) cellAliveColor else cellDeadColor
            image.apply {
                setColorFilter(color)
            }
        }

        override fun onClick(v: View?) {
            val cell = gridViewModel.getCell(this.layoutPosition)
            cell.toggleIsAlive()
            bind(cell.getIsAlive())
        }

        fun startAnimation() {
            //image.animate().start()
        }
    }

    inner class CellAdapter(var grid: Grid) : RecyclerView.Adapter<CellHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
            //Log.d(TAG, "CellAdapter ViewHolder created")
            val view: View = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
            view.layoutParams.height = parent.measuredWidth / grid.cols
            return CellHolder(view)
        }

        override fun onBindViewHolder(holder: CellHolder, position: Int) {
            //Log.d(TAG, "Binding Data at position: $position")
            val cell: Grid.Cell = grid.getCell(position)
            holder.bind(cell.getIsAlive())
        }

        override fun getItemCount(): Int {
            return gridViewModel.getGrid().size
        }
    }
}