package com.bedessee.salesca.customview

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bedessee.salesca.R
import com.bedessee.salesca.update.UpdateActivity
import com.bedessee.salesca.utilities.ViewUtilities
import kotlinx.android.synthetic.main.update_selector.view.*

class UpdateSelector : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.update_selector, container, false)
        val sh = requireActivity().getSharedPreferences("setting", AppCompatActivity.MODE_PRIVATE)
        val orient = sh.getString("orientation", "landscape")
        requireActivity().requestedOrientation = if (orient == "landscape") {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        view.positiveButton.setOnClickListener {
            var updateDir = ""

            val radioButtonID = view.options.checkedRadioButtonId
            val radioButton = view.options.findViewById<View>(radioButtonID)

            when (view.options.indexOfChild(radioButton)) {
                0 -> updateDir = "01-MON"
                1 -> updateDir = "02-TUE"
                2 -> updateDir = "03-WED"
                3 -> updateDir = "04-THUR"
                4 -> updateDir = "05-FRI"
                5 -> updateDir = "R-D"
            }

            val updateIntent = Intent(context, UpdateActivity::class.java)
            updateIntent.putExtra(UpdateActivity.KEY_UPDATE_DIR, updateDir)
            requireContext().startActivity(updateIntent)

            dismiss()
        }

        view.negativeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        ViewUtilities.setSmallDialogWindowSize(dialog!!.window!!)

    }
}