package com.example.memoria

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.core.graphics.PathUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentFormBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FormFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFormBinding? = null
    private lateinit var dao: PostDao
    private var currentPhotoPath: String = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: FormViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance(requireContext()).getPostDao()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToFeedButton.setOnClickListener {
            if(checkValidPost()) {
                val post = Post(null, binding.editTitle.text.toString(), binding.editDescription.text.toString(), DateTimeFormatter.ISO_INSTANT.format(
                    Instant.now()).toString(), binding.editTags.text.toString(), currentPhotoPath)
                val postID = runBlocking(Dispatchers.IO) {
                    dao.insert(post)
                }
                viewModel.makePost()
                val toast = Toast.makeText(context, "Post created!", Toast.LENGTH_LONG)
                toast.show()
                findNavController().navigate(R.id.action_FormFragment_to_FeedFragment)
            }
        }

        binding.cancel.setOnClickListener {
            findNavController().navigate(R.id.action_FormFragment_to_FeedFragment)
        }

        val imageView = view.findViewById<ImageView>(R.id.picture)
        val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            //val bitmap = it?.data?.extras?.get("data") as Bitmap
            val image: File = File(currentPhotoPath)
            val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
            var bitmap: Bitmap? = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
            //bitmap =
                //Bitmap.createScaledBitmap(bitmap!!, parent.getWidth(), parent.getHeight(), true)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        }

        val getStorageAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val usethis = it?.data?.data?.let { it1 -> context?.contentResolver?.openInputStream(it1) }
            val bitmap = BitmapFactory.decodeStream(usethis)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE

            val path = createImageFile()
            path.writeBitmap(bitmap, Bitmap.CompressFormat.JPEG, 100)
        }
        val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                Toast.makeText(context, "Permission not Granted", Toast.LENGTH_SHORT).show()
            }
            else{
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(requireContext().packageManager) != null) {
                    getAction.launch(cameraIntent)
                }

            }
        }

        val requestStorage = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                Toast.makeText(context, "Permission not Granted", Toast.LENGTH_SHORT).show()
            }
            else{
                val storageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                if(storageIntent.resolveActivity(requireContext().packageManager) != null){
                    getAction.launch(storageIntent)
                }

            }
        }

        binding.takePicture.setOnClickListener{
            if (checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(requireContext().packageManager) != null) {
                    val filePath = createImageFile()
                    val imageUri = context?.let { it1 ->
                        FileProvider.getUriForFile(
                            it1,
                            "com.example.memoria.example.provider",
                            filePath)
                    };
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    getAction.launch(cameraIntent)
                    //println(currentPhotoPath)
                }
            } else{
                requestCamera.launch(android.Manifest.permission.CAMERA)
            }

        }

        binding.cameraroll.setOnClickListener{
            if(checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                //TODO: need to update the way to grab image from external media
                val storageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                if(storageIntent.resolveActivity(requireContext().packageManager) != null){
                    getStorageAction.launch(storageIntent)
                }
            }else {

                requestStorage.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun checkValidPost(): Boolean{
        val titleField = binding.editTitleContainer
        val descriptionField = binding.editDescriptionContainter
        val tagField = binding.editTagsContainer
        val titleFieldText = binding.editTitle
        val descriptionFieldText = binding.editDescription
        val tagFieldText = binding.editTags


        if (titleFieldText.text.toString() == ""){
            titleField.isErrorEnabled = true
            titleField.error = "Post must have a title"
            return false
        }
        else {
            titleField.isErrorEnabled = false
        }
        if(currentPhotoPath == ""){
            val toast = Toast.makeText(context, "Post must have an image", Toast.LENGTH_LONG)
            toast.show()
            return false
        }
        if (tagFieldText.text.toString() == ""){
            tagField.isErrorEnabled = true
            tagField.error = "Post must have tags"
            return false
        }
        else {
            tagField.isErrorEnabled = false
        }
        if (descriptionFieldText.text.toString() == ""){
            descriptionField.isErrorEnabled = true
            descriptionField.error = "Post must have a description"
            return false
        }
        else {
            descriptionField.isErrorEnabled = false
        }
        return true
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FormFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}