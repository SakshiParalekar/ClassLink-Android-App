package com.example.classlink; // Make sure this matches your package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private List<Category> categoriesFiltered; // For search functionality
    private Context context;
    private OnCategoryClickListener listener; // Interface for click events

    // Interface to handle clicks on category items
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>(categories); // Copy of original data
        this.categoriesFiltered = new ArrayList<>(categories); // Initially same as original
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the category_item layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Get the current category from the filtered list
        Category category = categoriesFiltered.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryIcon.setImageResource(category.getIconResId());

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the count of items in the filtered list
        return categoriesFiltered.size();
    }

    // --- Search / Filter method ---
    public void filter(String text) {
        categoriesFiltered.clear(); // Clear current filtered list
        if (text.isEmpty()) {
            categoriesFiltered.addAll(categories); // If search text is empty, show all original categories
        } else {
            text = text.toLowerCase();
            for (Category item : categories) {
                // If category name contains the search text (case-insensitive)
                if (item.getName().toLowerCase().contains(text)) {
                    categoriesFiltered.add(item); // Add to filtered list
                }
            }
        }
        notifyDataSetChanged(); // Tell RecyclerView to refresh its view
    }

    // --- Update method for "View All" functionality ---
    public void updateDisplayedCategories(List<Category> newCategoriesToDisplay) {
        this.categoriesFiltered.clear();
        this.categoriesFiltered.addAll(newCategoriesToDisplay);
        notifyDataSetChanged();
    }


    // ViewHolder class to hold references to the views in category_item.xml
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}