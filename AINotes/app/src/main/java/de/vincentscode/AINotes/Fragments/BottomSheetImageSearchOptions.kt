package de.vincentscode.AINotes.Fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.ImageEditing.ImageConstants
import de.vincentscode.AINotes.Plugins.ImagePlugin
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.bottom_sheet_image_options.*


class BottomSheetImageSearchOptions(private val imagePlugin: ImagePlugin) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.bottom_sheet_image_options, container, false)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        insert_from_camera.setOnClickListener { openCamera() }
        insert_from_gallery.setOnClickListener { openGallery() }
    }

    // variable is needed so plugin is not removed directly after bottom sheet is being dismissed
    private var dismissOnSearch = false

    private fun openCamera() {
        dismissOnSearch = true

        App.mainActivity.currentImagePlugin = imagePlugin

        val permission = ContextCompat.checkSelfPermission(
            App.mainActivity,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Permission to record denied")
            ActivityCompat.requestPermissions(
                App.mainActivity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )

            dismiss()
        } else {
            dismiss()

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(App.mainActivity.packageManager)?.also {
                    App.mainActivity.startActivityForResult(
                        takePictureIntent,
                        ImageConstants.IMAGE_FROM_CAMERA_REQUEST
                    )
                }
            }
        }
    }

    private fun openGallery() {
        dismissOnSearch = true

        dismiss()

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        App.mainActivity.startActivityForResult(intent, ImageConstants.IMAGE_SEARCH_GALLERY_REQUEST)
        App.mainActivity.currentImagePlugin = imagePlugin
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (!dismissOnSearch) {
            App.mainActivity.editorScreen.removePlugin(imagePlugin)
        }

        dismissOnSearch = false
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet: View? = dialog?.findViewById(R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
    }
}