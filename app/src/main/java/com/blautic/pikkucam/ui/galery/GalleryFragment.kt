package com.blautic.pikkucam.ui.galery

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blautic.pikkucam.R
import com.blautic.pikkucam.adapters.GalleryAdapter
import com.blautic.pikkucam.adapters.VideoUriAndID
import com.blautic.pikkucam.databinding.FragmentGalleryBinding
import com.blautic.pikkucam.model.VideoMetadata
import com.blautic.pikkucam.ui.CameraFragment
import com.blautic.pikkucam.ui.SecondFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    lateinit var galleryAdapter: GalleryAdapter

    var cameraFragment: CameraFragment? = null
    var secondFragment: SecondFragment? = null

    var videoListMetadata = mutableListOf<VideoMetadata>()
    private var videoIds = mutableListOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraFragment = parentFragment as CameraFragment?

        secondFragment =
            cameraFragment!!.childFragmentManager.findFragmentByTag("SecondFragment") as SecondFragment?

        binding.progressBar.visibility = View.VISIBLE

        MainScope().launch {
            withContext(Dispatchers.Main) {

                videoListMetadata = getVideosFinal()

                galleryAdapter = GalleryAdapter(onClick = {
                    readMetaData(it.file, it.idThumb)
                    isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                },
                    onDelete = {
                        isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                    },
                    context = requireContext(),
                    myVideoIds = videoListMetadata.map { videoMetadata -> videoMetadata.idThumb }
                        .toList(),
                    originalList = videoListMetadata)

                galleryAdapter.submitList(videoListMetadata)

                binding.recyclerView.adapter = galleryAdapter
                binding.progressBar.visibility = View.INVISIBLE
            }
        }

        binding.buttonPlay.setOnClickListener {
            if (galleryAdapter.selectedItems.size == 1) {
                val intent = Intent(Intent.ACTION_VIEW, galleryAdapter.selectedItems.last().file)
                intent.setDataAndType(galleryAdapter.selectedItems.last().file, "video/mp4")
                startActivity(intent)
            }
        }

        binding.buttonDelete.setOnClickListener {
            if (galleryAdapter.selectedItems.isNotEmpty()){
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle(getString(R.string.video_eliminar))
                builder.setPositiveButton("OK") { dialog, which ->
                    deleteVideos()
                    isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                }
                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                builder.show()
            }

        }

        binding.buttonShare.setOnClickListener {
            if (galleryAdapter.selectedItems.size >= 1) {
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                intent.type = "video/*"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Título del mensaje")
                intent.putExtra(Intent.EXTRA_TEXT, "Descripción del mensaje")
                val files = arrayListOf<Uri>()
                galleryAdapter.selectedItems.forEach { uri ->
                    files.add(uri.file)
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                startActivity(intent)
            }
        }

        binding.selectAllVideos.setOnCheckedChangeListener { compoundButton, b ->
            selectAllVideos(b)
            isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
        }

        binding.linearLayoutCountVideos.setOnClickListener {
            selectAllVideos(false)
            isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
        }

        binding.fab.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Filtrar").setItems(R.array.filter_type) { _, which ->
                when (which) {
                    0 -> {
                        filterDescending()
                    }

                    1 -> {
                        filterAscending()
                    }
                }
            }
            builder.create()
            builder.show()
        }

        binding.textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No se necesita implementación aquí
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Verificar si el campo de texto está vacío
                if (s.toString().isEmpty()) {
                    binding.textInputLayout.hint = "Filtrar por subtitulo"
                } else {
                    binding.textInputLayout.hint = null
                }
            }

            override fun afterTextChanged(s: Editable) {
                // No se necesita implementación aquí
            }

        })

        binding.linearLayoutFilternameVideos.setOnClickListener {
            binding.textInputEditText.text = null
            (binding.recyclerView.adapter as GalleryAdapter).submitList(videoListMetadata)
            selectAllVideos(false)
            isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
            binding.linearLayoutFilternameVideos.visibility = View.GONE
        }

        binding.textInputEditText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                binding.textInputLayout.clearFocus()
                binding.textInputEditText.clearFocus()

                val inputMethodManager =
                    getSystemService(requireContext(), InputMethodManager::class.java)
                inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)

                val query = textView.text.toString().lowercase(Locale.getDefault())
                if (query.isNotEmpty()) {
                    (binding.recyclerView.adapter as GalleryAdapter).filterByName(
                        query, videoListMetadata
                    )
                    galleryAdapter.notifyDataSetChanged()
                    selectAllVideos(false)
                    isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                    binding.videoFilternameSelected.text = query
                    binding.linearLayoutFilternameVideos.visibility = View.VISIBLE
                } else {
                    (binding.recyclerView.adapter as GalleryAdapter).submitList(videoListMetadata)
                    binding.linearLayoutFilternameVideos.visibility = View.GONE
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun filterAscending() {
        (binding.recyclerView.adapter as GalleryAdapter).filterByAscending(
            binding.textInputEditText.text.toString(), videoListMetadata
        )
    }

    private fun filterDescending() {
        (binding.recyclerView.adapter as GalleryAdapter).filterByDescending(
            binding.textInputEditText.text.toString(), videoListMetadata
        )
    }

    private fun isMoreThanOneVideoSelected(b: Boolean) {
        if (galleryAdapter.selectedItems.isEmpty()) {
            binding.linearLayout2.visibility = View.GONE
            binding.linearLayoutCountVideos.visibility = View.GONE
        } else {
            binding.linearLayout2.visibility = View.VISIBLE
            if (b) {
                binding.linearLayoutCountVideos.visibility = View.VISIBLE
                binding.videoCountSelected.text = galleryAdapter.selectedItems.size.toString()
            } else {
                binding.linearLayoutCountVideos.visibility = View.GONE
            }
        }
    }

    private fun selectAllVideos(b: Boolean) {
        galleryAdapter.selectAll(b)
    }

    private fun deleteVideos() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            requestDeletePermission( galleryAdapter.selectedItems.map { videoMetadata -> videoMetadata.file })
/*            galleryAdapter.selectedItems.forEach { uri ->
                // Use MediaStore API to delete media files
                deleteVideo(uri.file)
            }*/
        } else {
            requestDeletePermission( galleryAdapter.selectedItems.map { videoMetadata -> videoMetadata.file })
//            galleryAdapter.selectedItems.forEach { uri ->
//                // Use MediaStore API to delete media files
//                deleteVideo(uri.file)
//            }
        }

        binding.progressBar.visibility = View.VISIBLE
    }

    private fun requestDeletePermission(uriList: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), uriList)
            try {
                startIntentSenderForResult(
                    pi.intentSender, 10, null, 0, 0,0, null)
            } catch (e: SendIntentException) {
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            MainScope().launch {
                withContext(Dispatchers.Main) {}

                videoListMetadata = getVideosFinal()

                galleryAdapter = GalleryAdapter(onClick = {
                    readMetaData(it.file, it.idThumb)
                    isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                },
                    onDelete = {
                        isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                    },
                    context = requireContext(),
                    myVideoIds = videoListMetadata.map { videoMetadata -> videoMetadata.idThumb }
                        .toList(),
                    originalList = videoListMetadata)

                galleryAdapter.submitList(videoListMetadata)

                binding.recyclerView.adapter = galleryAdapter

                isMoreThanOneVideoSelected(galleryAdapter.selectedItems.size > 1)
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    fun deleteVideo(videoUri: Uri?) {
        val contentResolver: ContentResolver = requireActivity().contentResolver

        // Borra el video utilizando la URI
        contentResolver.delete(videoUri!!, null, null)

        // También puedes eliminar el archivo físico en la tarjeta SD o almacenamiento interno si es necesario
        // Solo si tienes acceso de escritura en el almacenamiento externo
        val videoPath = getRealPathFromURI(videoUri)
        if (videoPath != null) {
            val file = File(videoPath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    // Método para obtener la ruta real desde la URI
    fun getRealPathFromURI(contentUri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor =
            requireActivity().contentResolver.query(contentUri!!, projection, null, null, null)
                ?: return null
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }


    private fun readMetaData(file: Uri, id: Long): VideoMetadata {
        if (file.path!!.isNotEmpty()) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireActivity(), file)
            var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val profile = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            var slowmo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

            val dateString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy  hh:mm:ss")
            val date2 = dateFormat.parse(dateString)

            val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

            slowmo = if (slowmo != null) {
                if (slowmo == "0" || slowmo == "Blues" || slowmo == "no") {
                    "No"
                } else {
                    "Sí"
                }
            } else {
                "No"
            }
            if (title != null) {
                if (title == "") {
                    title = "--"
                }
            } else {
                title = "--"
            }

            val name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)

            var duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toInt() / 1000
            val longDuration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                    .toFloat() / 1000
            if (longDuration % 1 >= 0.5) {
                duration++
            }
            val qualityValue =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            val quality: String = if (qualityValue != null) {
                when (qualityValue) {
                    "0" -> {
                        getString(R.string.baja)
                    }

                    "1" -> {
                        getString(R.string.media)
                    }

                    else -> {
                        getString(R.string.alta)
                    }
                }
            } else {
                ""
            }

            binding.duration.text = duration.toString()
            binding.date.text = date
            binding.perfil.text = profile
            binding.quality.text = quality
            binding.subtittle.text = title
            binding.slowmotion.text = slowmo

            return VideoMetadata(file, name, title, profile, slowmo, date2, duration, quality, id)
        }
        return VideoMetadata()
    }

    private fun getVideosFinal(): MutableList<VideoMetadata> {

        val filteredFiles = ArrayList<Uri>()
        val videoIds = mutableListOf<Long>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.MediaColumns.DATA
        )
        val selection: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ? "
        } else {
            MediaStore.Images.Media.DATA + " LIKE ? "
        }
        val selectionArgs = arrayOf("%PikkuCam%")
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        try {
            val cursor: Cursor? = requireActivity().applicationContext.contentResolver.query(
                collection, projection, selection, selectionArgs, sortOrder
            )

            val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            while (cursor?.moveToNext() == true) {

                val id = idColumn?.let { cursor.getLong(it) }

                nameColumn?.let { cursor.getString(it) }
                val f = File(cursor.getString(3))

                val contentUri = id?.let {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it)
                }
                val exists = f.exists()

                if (exists) {
                    if (contentUri != null) {
                        filteredFiles.add(contentUri)
                        videoIds.add(id)
                    }
                }
            }
        } catch (_: Exception) {

        }

        val listUriAndId = mutableListOf<VideoUriAndID>()

        filteredFiles.forEachIndexed { index, uri ->
            listUriAndId.add(VideoUriAndID(uri, videoIds[index]))
        }

        val finalVideoMetadata = mutableListOf<VideoMetadata>()

        listUriAndId.forEach {
            finalVideoMetadata.add(readMetaData(it.uri, it.id))
        }

        return finalVideoMetadata
    }

    fun removeFragment() {
        val transaction: FragmentTransaction =
            cameraFragment?.childFragmentManager!!.beginTransaction()
        transaction.remove(this@GalleryFragment)
        transaction.show(secondFragment!!)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoListMetadata.clear()
        videoIds.clear()
    }

}