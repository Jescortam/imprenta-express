package com.example.laboratoriodedispositivosmoviles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.laboratoriodedispositivosmoviles.databinding.FragmentViewProductBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val PRODUCT_ID = "productId"

class ViewProductFragment : Fragment(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var _binding: FragmentViewProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var productDatabase: ProductDatabase
    private lateinit var productId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getString(PRODUCT_ID).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProductBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productDatabase = ProductDatabase(requireActivity())

        launch { getProductData() }

        binding.buttonSalir.setOnClickListener { goToInventory() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        job.cancel()
    }

    private suspend fun getProductData() {
        val product = productDatabase.getProduct(productId)
        if (product != null) {
            initViewsWithData(product)
        } else {
            Toast.makeText(activity, "No se encontró el producto.", Toast.LENGTH_SHORT).show()
            goToInventory()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewsWithData(product: Product) {
        binding.textViewNombre.text = product.name
        binding.valueCantidad.text = product.quantity.toString()
        binding.valueTipo.text = product.type
        binding.valuePrecio.text = "$${String.format("%.2f", product.price)}"
        binding.editTextObservaciones.text = product.details

        val imageStorageHandler = ImageStorageHandler(requireActivity())
        val pathString = ImageStorageHandler.getPathString(productId)
        val imageView = binding.imageViewImagen
        imageStorageHandler.getImageFromStorageToImageView(pathString, imageView)

        binding.buttonMovimientos.setOnClickListener { goToMovements() }
        binding.buttonEditarDetalles.setOnClickListener { goToEditData() }
        binding.buttonEditarImagen.setOnClickListener { goToEditImage() }
        binding.buttonVerQr.setOnClickListener { goToGetQr() }
        binding.buttonBorrarProducto.setOnClickListener { showAlertDialog(product) }
    }

    private fun showAlertDialog(product: Product) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Eliminar producto")
            .setMessage("Está seguro de querer eliminar el producto?")
            .setNegativeButton("Cancelar") { _, _ -> }
            .setPositiveButton("Eliminar") { _, _ -> deleteProduct(product) }
            .show()
    }

    private fun deleteProduct(product: Product) {
        val imageStorageHandler = ImageStorageHandler(requireActivity())
        launch { imageStorageHandler.deleteImage(product.image) }

        productDatabase.deleteProduct(productId)
        goToInventory()
    }

    private fun goToMovements() {
        val action = ViewProductFragmentDirections.actionViewProductFragmentToProductMovementFragment(productId)
        requireView().findNavController().navigate(action)
    }

    private fun goToEditData() {
        val action = ViewProductFragmentDirections.actionViewProductFragmentToEditDataFragment(productId)
        requireView().findNavController().navigate(action)
    }

    private fun goToEditImage() {
        val action = ViewProductFragmentDirections.actionViewProductFragmentToEditImageFragment(productId)
        requireView().findNavController().navigate(action)
    }

    private fun goToGetQr() {
        val action = ViewProductFragmentDirections.actionViewProductFragmentToPrintQrFragment(productId)
        requireView().findNavController().navigate(action)
    }

    private fun goToInventory() {
        val action = ViewProductFragmentDirections.actionViewProductFragmentToInventoryFragment()
        requireView().findNavController().navigate(action)
    }
}