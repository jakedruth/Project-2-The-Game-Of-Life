package com.example.project2_thegameoflife

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.*


private const val TAG = "MainActivity"

private const val REQUEST_ALIVE_CELL_COLOR: Int = 0
private const val REQUEST_DEAD_CELL_COLOR: Int = 1

private const val CREATE_FILE = 10
private const val OPEN_FILE = 11

class MainActivity : AppCompatActivity(), ColorPickerDialog.Callbacks {

    private val gridViewModel: GridViewModel by lazy {
        return@lazy GridViewModel()
    }

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
        gridViewModel.createGrid(cols, rows)

        setUpView()
        setUpListeners()

        mainHandler = Handler(Looper.getMainLooper())
        gameRecyclerView.layoutManager = GridLayoutManager(this, cols)
        updateUI(gridViewModel.getGrid())
    }

    private fun setUpView() {
        gameRecyclerView = findViewById(R.id.game_recycler_view)
        startStopButton = findViewById(R.id.button_next_generation)
        clearButton = findViewById(R.id.button_clear)
        pickDeadColorButton = findViewById(R.id.button_dead_color)
        pickAliveColorButton = findViewById(R.id.button_alive_color)
        cloneButton = findViewById(R.id.button_clone)
        saveButton = findViewById(R.id.button_save)
        openButton = findViewById(R.id.button_open)
    }

    private fun setUpListeners() {
        startStopButton.setOnClickListener {
            toggleGameLoop()
        }
        clearButton.setOnClickListener {
            gridViewModel.clearGrid()
            gameRecyclerView.adapter?.notifyDataSetChanged()
        }
        pickDeadColorButton.setOnClickListener {
            ColorPickerDialog.newInstance(gridViewModel.cellDeadColor, REQUEST_DEAD_CELL_COLOR).show(supportFragmentManager, ColorPickerDialog.TAG)
        }
        pickAliveColorButton.setOnClickListener {
            ColorPickerDialog.newInstance(gridViewModel.cellAliveColor, REQUEST_ALIVE_CELL_COLOR).show(supportFragmentManager, ColorPickerDialog.TAG)
        }
        saveButton.setOnClickListener {
            createFile(Uri.fromFile(filesDir))
        }
        openButton.setOnClickListener {
            openFile(Uri.fromFile(filesDir))
        }
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
        gridViewModel.nextGeneration()
    }

    private fun createFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "game-of-life.txt")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }


        Log.d(TAG, "Starting intent to save file")
        startActivityForResult(intent, CREATE_FILE)
    }

    private fun openFile(pickerInitialUri: Uri) {
        val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        Log.d(TAG, "Starting intent to open file")
        startActivityForResult(intent, OPEN_FILE)
    }

    private fun writeGridToUri(uri: Uri) {
        Log.d(TAG, "Writing grid to file")
        val cr = applicationContext.contentResolver
        try {
            cr.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor: ParcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { stream ->
                    val grid = gridViewModel.getGridToJson()
                    if (grid != null) {
                        stream.write(grid.toByteArray())
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun readGridFromUri(uri: Uri) {
        Log.d(TAG, "Reading grid from file")
        val cr = applicationContext.contentResolver
        val stringBuilder = StringBuilder()
        cr.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        gridViewModel.setGridFromJson(stringBuilder.toString())
        updateUI(gridViewModel.getGrid())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "Received Activity Result. Request Code: $requestCode\tResult Code: $resultCode\tResult Ok: $RESULT_OK")

        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            val uri = data?.data as Uri
            writeGridToUri(uri)
        } else if (requestCode == OPEN_FILE && resultCode == RESULT_OK) {
            val uri = data?.data as Uri
            readGridFromUri(uri)
        }
    }

    override fun onColorSelected(color: Int, requestCode: Int) {
        Log.d(TAG, "On color selected")
        when (requestCode) {
            REQUEST_DEAD_CELL_COLOR -> {
                gridViewModel.cellDeadColor = color
            }
            REQUEST_ALIVE_CELL_COLOR -> {
                gridViewModel.cellAliveColor = color
            }
            else -> return
        }

        updateUI(gridViewModel.getGrid())
    }

    inner class CellHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val image: ImageView = itemView.findViewById(R.id.imageView)
        private var isAlive: Boolean = false

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(value: Boolean) {
            isAlive = value
            val color = if (isAlive) gridViewModel.cellAliveColor else gridViewModel.cellDeadColor
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
            Log.d(TAG, "CellAdapter ViewHolder created")
            val view: View = layoutInflater.inflate(R.layout.list_item_cell, parent, false)
            view.layoutParams.height = parent.measuredWidth / grid.cols
            return CellHolder(view)
        }

        override fun onBindViewHolder(holder: CellHolder, position: Int) {
            Log.d(TAG, "Binding Data at position: $position")
            val cell: Grid.Cell = grid.getCell(position)
            holder.bind(cell.getIsAlive())
        }

        override fun getItemCount(): Int {
            return gridViewModel.getGrid().size
        }
    }
}