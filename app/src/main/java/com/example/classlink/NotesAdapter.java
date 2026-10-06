
package com.example.classlink;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.widget.Button;
import com.airbnb.lottie.LottieAnimationView;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.Toast;
import java.util.List;

//image note
import android.graphics.Bitmap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ClassNote> notesList;

    private static final int TYPE_TEXT_NOTE = 0;
    private static final int TYPE_IMAGE_NOTE = 1;

    public NotesAdapter(List<ClassNote> notesList) {
        this.notesList = notesList;
    }

    @Override
    public int getItemViewType(int position) {
        ClassNote note = notesList.get(position);
        if ("text".equalsIgnoreCase(note.getType())) {
            return TYPE_TEXT_NOTE;
        } else if ("image".equalsIgnoreCase(note.getType())) {
            return TYPE_IMAGE_NOTE;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_TEXT_NOTE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_note, parent, false);
            return new TextNoteViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_note, parent, false);
            return new ImageNoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClassNote note = notesList.get(position);
        if (holder instanceof TextNoteViewHolder) {
            ((TextNoteViewHolder) holder).bind(note);
        } else if (holder instanceof ImageNoteViewHolder) {
            ((ImageNoteViewHolder) holder).bind(note);
        }
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void setNotes(List<ClassNote> newNotes) {
        this.notesList = newNotes;
        notifyDataSetChanged();
    }

    // ------------------- ViewHolder for Text Notes -------------------
    static class TextNoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, summaryTextView;
        Button summarizeButton, btnCopy;
        LottieAnimationView aiLoader;
        View aiBox;
        ExecutorService executor;
        Handler mainHandler;

        TextView uploadedTextView, estimatedTimeTextView;




        public TextNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title_text_view);
            contentTextView = itemView.findViewById(R.id.note_content_text_view);
            summarizeButton = itemView.findViewById(R.id.summarize_button);
            aiLoader = itemView.findViewById(R.id.ai_loader);
            summaryTextView = itemView.findViewById(R.id.tv_summary);
            btnCopy = itemView.findViewById(R.id.btn_copy);
            aiBox = itemView.findViewById(R.id.ai_summary_box);
            uploadedTextView = itemView.findViewById(R.id.note_uploaded_by);
            estimatedTimeTextView = itemView.findViewById(R.id.note_estimated_time);

            executor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());
        }

        void bind(ClassNote note) {
            titleTextView.setText(note.getTitle());
            contentTextView.setText(note.getDescription());
            uploadedTextView.setText("Uploaded by: " + note.getUploadedBy());


            if (note.getEstimatedReadTime() != null && !note.getEstimatedReadTime().isEmpty()) {
                estimatedTimeTextView.setVisibility(View.VISIBLE);
                estimatedTimeTextView.setText("⏱ " + note.getEstimatedReadTime());
            } else {
                estimatedTimeTextView.setVisibility(View.GONE);
            }


            // reset UI
            aiLoader.cancelAnimation();
            aiLoader.setVisibility(View.GONE);
            summaryTextView.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            aiBox.setVisibility(View.GONE);

            if (note.getAiSummary() != null && !note.getAiSummary().isEmpty()) {
                aiBox.setVisibility(View.VISIBLE);
                summaryTextView.setText(note.getAiSummary());
                summaryTextView.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
            }

            summarizeButton.setOnClickListener(v -> {
                aiBox.setVisibility(View.VISIBLE);
                summaryTextView.setVisibility(View.GONE);
                btnCopy.setVisibility(View.GONE);

                aiLoader.setVisibility(View.VISIBLE);
                aiLoader.playAnimation();

                executor.execute(() -> {
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}

                    // ✅ Get CharSequence summary with bullets
                    CharSequence summary = ExtractiveSummarizer.getSummary(note.getDescription());

                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}

                    note.setAiSummary(summary.toString());

                    mainHandler.post(() -> {
                        aiLoader.cancelAnimation();
                        aiLoader.setVisibility(View.GONE);

                        // Animate fade-in (not line by line)
                        summaryTextView.setText(summary);
                        summaryTextView.setAlpha(0f);
                        summaryTextView.setVisibility(View.VISIBLE);
                        summaryTextView.animate().alpha(1f).setDuration(400).start();

                        btnCopy.setVisibility(View.VISIBLE);

                        // Copy button
                        btnCopy.setOnClickListener(copyView -> {
                            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("AI Summary", summaryTextView.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(v.getContext(), "Summary copied!", Toast.LENGTH_SHORT).show();
                        });
                    });
                });
            });
        }
    }

    // ------------------- ViewHolder for Image Notes -------------------
    static class ImageNoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, summaryTextView;
        ImageView imageView;
        Button summarizeButton, btnCopy;
        LottieAnimationView aiLoader;
        View aiBox;
        ExecutorService executor;
        Handler mainHandler;
        TextView uploadedTextView;

        public ImageNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title_image_view);
            imageView = itemView.findViewById(R.id.note_image_view);
            summarizeButton = itemView.findViewById(R.id.summarize_button);
            summaryTextView = itemView.findViewById(R.id.tv_summary);
            btnCopy = itemView.findViewById(R.id.btn_copy);
            aiLoader = itemView.findViewById(R.id.ai_loader);
            aiBox = itemView.findViewById(R.id.ai_summary_box);
            uploadedTextView = itemView.findViewById(R.id.note_uploaded_by);

            executor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());
        }

        void bind(ClassNote note) {
            titleTextView.setText(note.getTitle());
            uploadedTextView.setText("Uploaded by: " + note.getUploadedBy());
            Glide.with(itemView.getContext())
                    .load(note.getDownloadUrl())
                    .into(imageView);

            // reset UI
            aiLoader.cancelAnimation();
            aiLoader.setVisibility(View.GONE);
            summaryTextView.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            aiBox.setVisibility(View.GONE);

            if (note.getAiSummary() != null && !note.getAiSummary().isEmpty()) {
                aiBox.setVisibility(View.VISIBLE);
                summaryTextView.setText(note.getAiSummary());
                summaryTextView.setVisibility(View.VISIBLE);
                btnCopy.setVisibility(View.VISIBLE);
            }

            summarizeButton.setOnClickListener(v -> {
                aiBox.setVisibility(View.VISIBLE);
                summaryTextView.setVisibility(View.GONE);
                btnCopy.setVisibility(View.GONE);
                aiLoader.setVisibility(View.VISIBLE);
                aiLoader.playAnimation();

                executor.execute(() -> {
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}

                    // Convert ImageView to Bitmap
                    imageView.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
                    imageView.setDrawingCacheEnabled(false);

                    // OCR using ML Kit
                    InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            .process(inputImage)
                            .addOnSuccessListener(visionText -> {
                                String extractedText = visionText.getText().replaceAll("\\s+", " ").trim();

                                if (!extractedText.isEmpty()) {
                                    CharSequence summary = ExtractiveSummarizer.getSummary(extractedText);
                                    note.setAiSummary(summary.toString());

                                    mainHandler.post(() -> {
                                        aiLoader.cancelAnimation();
                                        aiLoader.setVisibility(View.GONE);
                                        summaryTextView.setText(summary);
                                        summaryTextView.setAlpha(0f);
                                        summaryTextView.setVisibility(View.VISIBLE);
                                        summaryTextView.animate().alpha(1f).setDuration(400).start();
                                        btnCopy.setVisibility(View.VISIBLE);

                                        btnCopy.setOnClickListener(copyView -> {
                                            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("AI Summary", summaryTextView.getText().toString());
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(v.getContext(), "Summary copied!", Toast.LENGTH_SHORT).show();
                                        });
                                    });
                                } else {
                                    mainHandler.post(() -> {
                                        aiLoader.cancelAnimation();
                                        aiLoader.setVisibility(View.GONE);
                                        summaryTextView.setVisibility(View.VISIBLE);
                                        summaryTextView.setText("⚡ No text found in image.");
                                    });
                                }
                            })
                            .addOnFailureListener(e -> mainHandler.post(() -> {
                                aiLoader.cancelAnimation();
                                aiLoader.setVisibility(View.GONE);
                                summaryTextView.setVisibility(View.VISIBLE);
                                summaryTextView.setText("⚡ Failed to extract text.");
                            }));
                });
            });
        }
    }

}


