package com.example.laboratoriodedispositivosmoviles

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import layout.com.example.laboratoriodedispositivosmoviles.ProductCardClickListener

class ProductAdapter(var products: ArrayList<Product>, private val productCardClickListener: ProductCardClickListener): RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    private lateinit var glideRef: RequestManager

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var productView: CardView
        var productImage: ImageView
        var productName: TextView
        var productQuantity: TextView
        var productType: TextView
        var productPrice: TextView
        var productDetails: TextView

        init {
            productView = view.findViewById(R.id.productView)
            productImage = view.findViewById(R.id.productImageView)
            productName = view.findViewById(R.id.productNameTextView)
            productQuantity = view.findViewById(R.id.productQuantityTextView)
            productType = view.findViewById(R.id.productTypeTextView)
            productPrice = view.findViewById(R.id.productPriceTextView)
            productDetails = view.findViewById(R.id.productDetailsTextView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.product_layout, viewGroup, false)

        glideRef = Glide.with(view)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val storageRef = Firebase.storage.reference

        storageRef.child(products[position].image).downloadUrl.addOnSuccessListener { uri ->
            glideRef.load(uri).into(viewHolder.productImage)
        }

        viewHolder.productName.text = products[position].name
        viewHolder.productQuantity.text = "Cantidad: ${products[position].quantity}"
        viewHolder.productType.text = "Tipo: ${products[position].type}"
        viewHolder.productPrice.text = "$${String.format("%.2f", products[position].price)}"
        viewHolder.productDetails.text = "Observaciones: ${products[position].details}"

        viewHolder.productView.setOnClickListener { productCardClickListener.onProductCardClick(products[position].id) }
    }

    override fun getItemCount() = products.size
}
