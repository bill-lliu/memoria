package com.example.memoria

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class PostAdapter(private val dataSet: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView?
        val image: ImageView?
        val description: TextView?
        val tags: TextView?

        init {
            title = view.findViewById(R.id.postTitle)
            image = view.findViewById(R.id.postImage)
            description = view.findViewById(R.id.postDescription)
            tags = view.findViewById(R.id.postTags)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        println(viewGroup)
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.feed_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        println(viewHolder)
        println(position)
        println(viewHolder.description)
        viewHolder.title?.text = dataSet[position].title
        val imageFromPath: File = File(dataSet[position].picturePath)
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        var bitmap: Bitmap? = BitmapFactory.decodeFile(imageFromPath.absolutePath, bmOptions)
        viewHolder.image?.setImageBitmap(bitmap)
        viewHolder.description?.text = dataSet[position].description
        viewHolder.tags?.text = dataSet[position].tags

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

