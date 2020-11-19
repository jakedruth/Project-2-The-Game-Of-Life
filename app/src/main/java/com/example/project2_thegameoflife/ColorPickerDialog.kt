package com.example.project2_thegameoflife

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment


class ColorPickerDialog : DialogFragment() {

    interface Callbacks {
        fun onColorSelected(color: Int, requestCode: Int)
    }

    companion object {
        const val TAG = "ColorPickerDialog"

        private const val KEY_COLOR = "KEY_COLOR"
        private const val REQUEST_CODE = "REQUEST_CODE"

        fun newInstance(initialColor: Int, requestCode: Int): ColorPickerDialog {
            Log.d(TAG, "Getting New Instance of ColorPickerDialog")
            val args = Bundle()
            args.putInt(KEY_COLOR, initialColor)
            args.putInt(REQUEST_CODE, requestCode)
            val fragment = ColorPickerDialog()
            fragment.arguments = args
            return fragment
        }

        fun getAFromColorInt(color: Int): Int {
            return color shr 24 and 0xff
        }

        fun getRFromColorInt(color: Int): Int {
            return color shr 16 and 0xff
        }

        fun getGFromColorInt(color: Int): Int {
            return color shr 8 and 0xff
        }

        fun getBFromColorInt(color: Int): Int {
            return color and 0xff
        }

        fun decodeColorInt(color: Int) : Array<Int> {
//            val a: Int = color shr 24 and 0xff // or color >>> 24
//            val r: Int = color shr 16 and 0xff
//            val g: Int = color shr 8 and 0xff
//            val b: Int = color and 0xff
            return arrayOf(
                getAFromColorInt(color),
                getRFromColorInt(color),
                getGFromColorInt(color),
                getBFromColorInt(color))
        }

        fun encodeColorInt(a: Int, r: Int, g: Int, b: Int): Int {
            return  (a and 0xff shl 24) or
                    (r and 0xff shl 16) or
                    (g and 0xff shl 8) or
                    (b and 0xff)
        }

        fun colorIntToHex(color: Int): String {
            return color.toUInt().toString(16)
        }

        fun hexToColorInt(hex: String): Int? {
            return hex.toIntOrNull(16)
        }
    }

    var listener: Callbacks? = null

    private lateinit var viewOutput: View

    private lateinit var textViewR: TextView
    private lateinit var textViewG: TextView
    private lateinit var textViewB: TextView

    private lateinit var seekBarR: SeekBar
    private lateinit var seekBarG: SeekBar
    private lateinit var seekBarB: SeekBar

    private lateinit var editTextHex: EditText
    private lateinit var okButton: Button

    private var color: Int = Color.WHITE
    private var requestCode: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_color_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "View Created...")
        setupView(view)
        setupListeners()

        val color = arguments?.getInt(KEY_COLOR)!!
        requestCode = arguments?.getInt(REQUEST_CODE)!!

        seekBarR.progress = getRFromColorInt(color)
        seekBarG.progress = getGFromColorInt(color)
        seekBarB.progress = getBFromColorInt(color)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupView(view: View) {
        viewOutput = view.findViewById(R.id.view_output)

        // R
        textViewR = view.findViewById(R.id.textView_r_value)
        seekBarR = view.findViewById(R.id.seekBar_r)

        // G
        textViewG = view.findViewById(R.id.textView_g_value)
        seekBarG = view.findViewById(R.id.seekBar_g)

        // B
        textViewB = view.findViewById(R.id.textView_b_value)
        seekBarB = view.findViewById(R.id.seekBar_b)

        editTextHex = view.findViewById(R.id.editText_hex)
        okButton = view.findViewById(R.id.button_ok)
    }

    private fun setupListeners() {
        seekBarR.setOnSeekBarChangeListener(getOnSeekBarChangeListener(textViewR))
        seekBarG.setOnSeekBarChangeListener(getOnSeekBarChangeListener(textViewG))
        seekBarB.setOnSeekBarChangeListener(getOnSeekBarChangeListener(textViewB))

        editTextHex.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO("Not yet implemented")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 6) {
                    // Error coding color
                } else {
                    val colorInt: Int? = hexToColorInt(s.toString())
                    if (colorInt != null) {
                        seekBarR.progress = getRFromColorInt(colorInt)
                        seekBarG.progress = getGFromColorInt(colorInt)
                        seekBarB.progress = getBFromColorInt(colorInt)
                    }
                }
            }
        })

        okButton.setOnClickListener {
            listener?.onColorSelected(color, requestCode)
            dismiss()
        }
    }

    private fun getOnSeekBarChangeListener(textView: TextView): SeekBar.OnSeekBarChangeListener {
        return object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
                updateViewTint()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //TODO("Not yet implemented")
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //TODO("Not yet implemented")
            }
        }
    }

    private fun updateViewTint() {
        color = encodeColorInt(255, seekBarR.progress, seekBarG.progress, seekBarB.progress)
        viewOutput.setBackgroundColor(color)
        editTextHex.setText(colorIntToHex(color).subSequence(2, 8), TextView.BufferType.EDITABLE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        listener = try {
            // Instantiate the EditNameDialogListener so we can send events to the host
            context as Callbacks
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(context.toString().toString() + " must implement EditNameDialogListener")
        }
    }
}