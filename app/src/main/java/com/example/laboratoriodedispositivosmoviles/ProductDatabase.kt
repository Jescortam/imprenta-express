package com.example.laboratoriodedispositivosmoviles

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProductDatabase(val activity: FragmentActivity) {
    private var productDatabase: DatabaseReference = Firebase.database.getReference("products")

    fun setChildEventListener(adapter: ProductAdapter, recyclerView: RecyclerView) {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val data = dataSnapshot.value as HashMap<*, *>
                val product = ProductParser.parseProductFromHashMap(dataSnapshot.key!!, data)

                adapter.products = (arrayListOf(product) + adapter.products) as ArrayList<Product>
                adapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                var i = 0
                while (i < adapter.products.size) {
                    if (adapter.products[i].id == dataSnapshot.key) {
                        val data = dataSnapshot.value as HashMap<*, *>
                        adapter.products[i] =
                            ProductParser.parseProductFromHashMap(dataSnapshot.key!!, data)
                        adapter.notifyItemChanged(i)
                        break
                    }

                    i++
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                var i = 0
                while (i < adapter.products.size) {
                    if (adapter.products[i].id == dataSnapshot.key) {
                        adapter.products.removeAt(i)
                        adapter.notifyItemRemoved(i)
                        break
                    }

                    i++
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        productDatabase.addChildEventListener(childEventListener)
    }

    suspend fun getProduct(productId: String): Product? = suspendCoroutine { continuation ->
        var data: HashMap<*, *>
        val productRef = productDatabase.child(productId)
        productRef.get().addOnSuccessListener {
            if (it.value != null) {
                data = it.value as HashMap<*, *>
                if (data.isNotEmpty()) {
                    continuation.resume(ProductParser.parseProductFromHashMap(productId, data))
                }
            } else {
                Toast.makeText(activity, "No se encontró el producto.", Toast.LENGTH_SHORT).show()
                continuation.resume(null)
            }
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }

    fun setProduct(productId: String, product: Product) {
        Log.d(TAG, product.operations.toString())
        productDatabase.child(productId).setValue(product).addOnSuccessListener {
            Toast.makeText(activity, "Producto editado de manera exitosa", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(activity, "Error en la edición del producto", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProduct(productId: String) {
        productDatabase.child(productId).setValue(null).addOnSuccessListener {
            Toast.makeText(activity, "Producto eliminado de manera exitosa", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(activity, "Error en la eliminación del producto", Toast.LENGTH_SHORT).show()
        }
    }
}