package com.egyabaah.tippy

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15

class MainActivity : AppCompatActivity() {
    private var numberOfPeople = 1
    private lateinit var etBaseAmount: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tvTipPercentLabel: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var btnRoundUp: Button
    private lateinit var btnRoundDown: Button
    private lateinit var switchSplitBill: Switch
    private lateinit var spNumOfPeople: Spinner
    private lateinit var tvToPayAmount: TextView
    private lateinit var groupSplitBill: Group



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get Views
        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tvTipPercentLabel = findViewById(R.id.tvTipPercentLabel)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        btnRoundUp = findViewById(R.id.btnRoundUp)
        btnRoundDown = findViewById(R.id.btnRoundDown)
        switchSplitBill = findViewById(R.id.switchSplitBill)
        spNumOfPeople = findViewById(R.id.spNumOfPeople)
        tvToPayAmount = findViewById(R.id.tvToPayAmount)
        groupSplitBill = findViewById(R.id.groupSplitBill)

        // Get string values
        val numPeople = resources.getStringArray(R.array.NumPeople)

        val spNumOfPeopleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, numPeople)
        spNumOfPeople.adapter = spNumOfPeopleAdapter

        // Assign default values to views
        seekBarTip.progress = INITIAL_TIP_PERCENT
        tvTipPercentLabel.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)

        // Event Listeners
        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged $progress")
                tvTipPercentLabel.text = "$progress%"
                computeTipAndTotal()
                updateTipDescription(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        etBaseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                computeTipAndTotal()
            }
        })
        val clickListener = ClickListener()
        btnRoundUp.setOnClickListener(clickListener)
        btnRoundDown.setOnClickListener(clickListener)

        spNumOfPeople.onItemSelectedListener = (object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                numberOfPeople = numPeople[position].toInt()
                computeTipAndTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                numberOfPeople = 1
            }

        } )
        switchSplitBill.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked){
                    groupSplitBill.visibility = View.VISIBLE
                }
                else{
                    groupSplitBill.visibility = View.INVISIBLE
                }
                computeTipAndTotal()
            }

        })

    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription = when (tipPercent) {
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        tvTipDescription.text = tipDescription

        // Update tvTipDescription color based on tipPercent
        var color = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.color_worst_tip),
            ContextCompat.getColor(this, R.color.color_best_tip)
        ) as Int
        tvTipDescription.setTextColor(color)
    }

    private fun computeTipAndTotal() {
        if (etBaseAmount.text.isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            disableRoundUpAndDownButtons()
            return
        }
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipPercent: Double = seekBarTip.progress / 100.0
        val tipAmount = baseAmount * tipPercent
        val totalAmount = baseAmount + tipAmount
        setTvTipAmountAndTvTotalAmountText(tipAmount, totalAmount)
        if (switchSplitBill.isChecked){
            splitBill(totalAmount)
        }
        else if (tvTotalAmount.text.toString().toDouble() % 1 != 0.0) {
            // Enable round up/down buttons if total amount is a decimal
            enableRoundUpAndDownButtons()
        } else {
            disableRoundUpAndDownButtons()
        }

    }

    private fun enableRoundUpAndDownButtons() {
        btnRoundUp.isEnabled = true
        btnRoundDown.isEnabled = true
    }

    private fun splitBill(totalAmount: Double) {
        val toPayEach = totalAmount / numberOfPeople
        tvToPayAmount.text = "%.2f".format(toPayEach)
        if (tvToPayAmount.text.toString().toDouble() % 1 != 0.0) {
            // Enable round up/down buttons if total amount is a decimal
            enableRoundUpAndDownButtons()
        }
        else {
            disableRoundUpAndDownButtons()
        }

    }

    private fun disableRoundUpAndDownButtons() {
        btnRoundUp.isEnabled = false
        btnRoundDown.isEnabled = false
    }

    private fun roundTotalAmount(up: Boolean, shouldSplitBill: Boolean) {
        if (tvTotalAmount.text.isEmpty() || (shouldSplitBill && tvToPayAmount.text.isEmpty())) {
            return
        }
        var totalAmount = if(shouldSplitBill) tvToPayAmount.text.toString().toDouble() else tvTotalAmount.text.toString().toDouble()
        totalAmount = when (up) {
            true -> {
                ceil(totalAmount)
            }

            else -> {
                floor(totalAmount)
            }
        }
        var toPayEach = 0.0
        // Get actual totalAmount to be paid since totalAmount above will be what an individual
        // in the group will pay when shouldSplitBill is true
        if (shouldSplitBill){
            toPayEach = totalAmount
            totalAmount *= numberOfPeople
        }
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipAmount = totalAmount - baseAmount
        val tipPercent: Int = ((tipAmount / baseAmount) * 100).roundToInt()
        setTvTipAmountAndTvTotalAmountText(tipAmount, totalAmount, toPayEach)
        seekBarTip.progress = tipPercent
        disableRoundUpAndDownButtons()
    }

    private fun setTvTipAmountAndTvTotalAmountText(tipAmount: Double, totalAmount: Double) {
        tvTipAmount.text = "%.2f".format(tipAmount)
        tvTotalAmount.text = "%.2f".format(totalAmount)
    }
    private fun setTvTipAmountAndTvTotalAmountText(tipAmount: Double, totalAmount: Double, toPayEach: Double) {
        tvTipAmount.text = "%.2f".format(tipAmount)
        tvTotalAmount.text = "%.2f".format(totalAmount)
        tvToPayAmount.text = "%.2f".format(toPayEach)
    }

    private inner class ClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) {
                return
            }
            when (v) {
                btnRoundUp -> {
                    roundTotalAmount(true, switchSplitBill.isChecked)
                }
                btnRoundDown -> {
                    roundTotalAmount(false, switchSplitBill.isChecked)
                }
            }

        }

    }
}