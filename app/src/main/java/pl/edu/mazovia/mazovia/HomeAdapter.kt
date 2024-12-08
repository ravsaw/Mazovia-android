package pl.edu.mazovia.mazovia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(private val items: List<HomeDataModel>) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    // Tworzy nowy widok (wywoływane przez layout managera)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Wyświetla dane w danym wierszu (wywoływane przez layout managera)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    // Zwraca liczbę elementów w liście
    override fun getItemCount() = items.size

    // Klasa ViewHolder przechowuje widoki dla każdego elementu listy
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(item: HomeDataModel) {
            nameTextView.text = item.name
            descriptionTextView.text = item.description

            // Opcjonalnie: dodaj listener kliknięcia
            itemView.setOnClickListener {
                // Obsługa kliknięcia elementu
            }
        }
    }
}