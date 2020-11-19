package com.example.project2_thegameoflife

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


private const val TAG = "MainActivity"

private const val REQUEST_ALIVE_CELL_COLOR: Int = 0
private const val REQUEST_DEAD_CELL_COLOR: Int = 1

class MainActivity : AppCompatActivity(), ColorPickerDialog.Callbacks {

    private val gridViewModel: GridViewModel by lazy {
        return@lazy GridViewModel()
    }

    var cellDeadColor = Color.argb(255, 255, 255, 255)
    var cellAliveColor = Color.argb(255, 200, 0, 0)
    var isPlaying: Boolean = false
    var updateGridTimer: Long = 1000L

    private lateinit var mainHandler: Handler
    private lateinit var runnable: Runnable

    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var startStopButton: Button
    private lateinit var clearButton: Button
    private lateinit var pickDeadColorButton: Button
    private lateinit var pickAliveColorButton: Button
    private lateinit var cloneButton: Button
    private lateinit var saveButton: Button
    private lateinit var openButton: Button

    private var adapter: CellAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "On Create")

        val rows = 20
        val cols = 20

        gameRecyclerView = findViewById(R.id.game_recycler_view)
        startStopButton = findViewById(R.id.button_next_generation)
        clearButton = findViewById(R.id.button_clear)
        pickDeadColorButton = findViewById(R.id.button_dead_color)
        pickAliveColorButton = findViewById(R.id.button_alive_color)
        cloneButton = findViewById(R.id.button_clone)
        saveButton = findViewById(R.id.button_save)
        openButton = findViewById(R.id.button_open)

        startStopButton.setOnClickListener {
            toggleGameLoop()
        }
        clearButton.setOnClickListener {
            gridViewModel.clearGrid()
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }
        pickDeadColorButton.setOnClickListener {
            ColorPickerDialog.newInstance(cellDeadColor, REQUEST_DEAD_CELL_COLOR).show(supportFragmentManager, ColorPickerDialog.TAG)
        }
        pickAliveColorButton.setOnClickListener {
            ColorPickerDialog.newInstance(cellAliveColor, REQUEST_ALIVE_CELL_COLOR).show(supportFragmentManager, ColorPickerDialog.TAG)
        }

        mainHandler = Handler(Looper.getMainLooper())

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

    override fun onColorSelected(color: Int, requestCode: Int) {

        Log.d(TAG, "On color selected")

        when (requestCode) {
            REQUEST_DEAD_CELL_COLOR -> {
                cellDeadColor = color
            }
            REQUEST_ALIVE_CELL_COLOR -> {
                cellAliveColor = color
            }
            else -> return
        }

        updateUI(gridViewModel.getGrid())
    }
}