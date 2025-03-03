package com.example.sw0b_001.Settings

import com.example.sw0b_001.R
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class LogoutDeleteConfirmationModalFragment(val onSuccessRunnable: Runnable) :
    BottomSheetDialogFragment(R.layout.fragment_modal_logout_delete_confirmation){
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = view.findViewById<View>(R.id.logout_delete_confirmation_layout)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        view.findViewById<MaterialButton>(R.id.logout_delete_confirmation_btn).setOnClickListener {
            onSuccessRunnable.run()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.logout_delete_confirmation_cancel_btn).setOnClickListener {
            dismiss()
        }
    }
}