package com.example.k_content_app

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import java.io.File

class SearchingFragment : Fragment(), ImageModel.ImageSearchCallback {

    private lateinit var imageModel: ImageModel

    private var resView: TextView? = null
    private var imageBtn: ImageButton? = null
    private var imageView: ImageView? = null
    private var uploadChooser: UploadChooser? = null

    private val FILE_NAME = "picture.jpg"
    private lateinit var photoFile: File

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                // Handle gallery image
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    imageView?.setImageBitmap(bitmap)
                    imageModel.modelActivity(bitmap)
                    uploadChooser?.dismiss()
                    Log.d("ImageSearch", "Image loaded and processed successfully")
                } catch (e: Exception) {
                    Log.e("ImageSearch", "Error loading image", e)
                }
            } else {
                // Handle camera image
                try {
                    val photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, photoUri)
                    imageView?.setImageBitmap(bitmap)
                    imageModel.modelActivity(bitmap)
                    uploadChooser?.dismiss()
                    Log.d("ImageSearch", "Image captured and processed successfully")
                } catch (e: Exception) {
                    Log.e("ImageSearch", "Error loading image", e)
                }
            }
        } else {
            Log.e("ImageSearch", "Result not OK")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 레이아웃을 인플레이트 합니다.
        val view = inflater.inflate(R.layout.fragment_searching, container, false)
        imageView = view.findViewById(R.id.imageView6)
        resView = view.findViewById(R.id.searchcontent)
        imageBtn = view.findViewById(R.id.imageSearchBtn)

        // ImageModel 초기화
        imageModel = ImageModel(requireContext())
        imageModel.callback = this  // Set the callback

        // btn2(마이페이지 이동)가 ImageButton임을 확인하고 참조를 수정
        val btn2 = view.findViewById<ImageButton>(R.id.btn2)
        btn2?.setOnClickListener {
            Log.d("SearchingFragment", "btn2 clicked")
            it.findNavController().navigate(R.id.action_searchingFragment_to_userInfoFragment)
        }

        // btn3(홈화면 이동)가 ImageButton임을 확인하고 참조를 수정
        val btn3 = view.findViewById<ImageButton>(R.id.btn3)
        btn3?.setOnClickListener {
            Log.d("SearchingFragment", "btn3 clicked")
            it.findNavController().navigate(R.id.action_searchingFragment_to_mainHomeFragment)
        }

        // 이미지 검색 버튼
        val imageSearchButton = view.findViewById<ImageButton>(R.id.imageSearchBtn)
        imageSearchButton.setOnClickListener {
            // 로그 추가
            Log.d("ImageSearch", "Click ImageSearch Button")
            uploadChooser = UploadChooser().apply {
                addInterface(object : UploadChooser.UploadChooserInterface {
                    override fun cameraOnClick() {
                        Log.d("upload", "cameraOnClick")
                        openCamera()
                    }

                    override fun galleryOnClick() {
                        Log.d("upload", "galleryOnClick")
                        callImageSearch()
                    }
                })
            }
            uploadChooser!!.show(parentFragmentManager, "UploadChooser")
        }

        val searchButton = view.findViewById<ImageButton>(R.id.searchbtn)
        searchButton.setOnClickListener {
            val searchText = view.findViewById<EditText>(R.id.searchcontent).text.toString()
            Log.d("Search", "Search button clicked with text: $searchText")
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra("searchText", searchText)
            startActivity(intent)
        }

        return view
    }

    override fun onImageSearchResult(result: String) {
        Log.d("ImageSearch", "onImageSearchResult called with result: $result")
        resView?.text = result
    }

    private fun openCamera() {
        photoFile = createCameraFile()
        val photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
        // 이후 카메라 인텐트를 사용한 코드 추가
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imagePickerLauncher.launch(intent)
    }

    private fun createCameraFile(): File {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    private fun callImageSearch() {
        Log.d("ImageSearch", "callImageSearch called")
        imageSelect()
    }

    private fun imageSelect() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        Log.d("ImageSearch", "Launching image selector")
        imagePickerLauncher.launch(intent)
    }
}
