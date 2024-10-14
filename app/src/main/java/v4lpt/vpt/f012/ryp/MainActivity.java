package v4lpt.vpt.f012.ryp;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.documentfile.provider.DocumentFile;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class MainActivity extends AppCompatActivity {

    private static final int PICK_FOLDER_REQUEST = 1;
    private List<DocumentFile> imageFiles;
    private Set<DocumentFile> zeroStarImages;
    private int currentImageIndex = 0;
    private ImageView imageView;
    private OverlayStars ratingOverlayView;
    private TextView tvFileName;
    private View welcomeLayout, ratingLayout, infoLayout;
    private ExecutorService executorService;
    private boolean showRatingOverlay = false;
    private Handler handler = new Handler();
    private SettingsManager settingsManager;
    private Button infoButton;
    private LoadingStars loadingStars;
    private LoadingStars toggleAnimationView;
    private TextView toggleStatusText;
    private Handler toggleHandler = new Handler();
    private boolean isHolding = false;
    private static final long TOGGLE_HOLD_TIME = 500; // 500ms hold time to toggle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsManager = new SettingsManager(this);
        showRatingOverlay = settingsManager.isShowRatingOverlay();
        TextView titleBar2 = findViewById(R.id.titleBar2);
        if (Math.random() < 0.01) { // 1% chance
            titleBar2.setText(R.string.rate_your_pigs);
        }
        welcomeLayout = findViewById(R.id.welcomeLayout);
        ratingLayout = findViewById(R.id.ratingLayout);
        infoLayout = findViewById(R.id.infoLayout);
        imageView = ratingLayout.findViewById(R.id.imageView);
        loadingStars = findViewById(R.id.loadingStars);
        toggleAnimationView = findViewById(R.id.toggleAnimationView);
        toggleStatusText = findViewById(R.id.toggleStatusText);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        ratingOverlayView = ratingLayout.findViewById(R.id.ratingOverlayView);
        tvFileName = ratingLayout.findViewById(R.id.tvFileName);

        MaterialButton btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnSelectFolder.setOnClickListener(v -> selectFolder());

        infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> showInfoLayout());

        View backButton23 = infoLayout.findViewById(R.id.backButton23);
        backButton23.setOnClickListener(v -> showWelcomeLayout());


        setupRatingButtons();
        setupNavigationButtons();
        imageFiles = new ArrayList<>();
        zeroStarImages = new HashSet<>();
        executorService = Executors.newSingleThreadExecutor();
        showWelcomeLayout();


        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    toggleHandler.postDelayed(toggleRunnable, TOGGLE_HOLD_TIME);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    toggleHandler.removeCallbacks(toggleRunnable);
                    break;
            }
            return true;
        });
    }

    private Runnable toggleRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHolding) {
                toggleOverlay();
            }
        }
    };

    private void toggleOverlay() {
        showRatingOverlay = !showRatingOverlay;
        settingsManager.setShowRatingOverlay(showRatingOverlay);

        toggleAnimationView.setVisibility(View.VISIBLE);
        toggleAnimationView.startAnimation();
        toggleStatusText.setVisibility(View.VISIBLE);
        toggleStatusText.setText(showRatingOverlay ? "On" : "Off");

        handler.postDelayed(() -> {
            toggleAnimationView.setVisibility(View.GONE);
            toggleStatusText.setVisibility(View.GONE);
        }, 1670);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Adjust the layout params of the rating layout
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        getWindow().setAttributes(params);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Reset the layout params
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags &= ~(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setAttributes(params);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteZeroStarImages();
        executorService.shutdown();
    }
    @Override
    protected void onPause() {
        super.onPause();
        deleteZeroStarImages();
    }
    private void selectFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FOLDER_REQUEST && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                loadImagesAsync(pickedDir);
            }
        }
    }


    private void loadImagesAsync(final DocumentFile directory) {
        showRatingLayout();
        //Toast.makeText(this, "Loading images...", Toast.LENGTH_SHORT).show();
        showLoadingAnimation();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final List<DocumentFile> tempImageFiles = new ArrayList<>();
                loadImagesRecursively(directory, tempImageFiles);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageFiles = tempImageFiles;
                        if (!imageFiles.isEmpty()) {
                            currentImageIndex = 0;
                            hideLoadingAnimation();
                            displayNextUnratedImage();
                            //Toast.makeText(MainActivity.this, "Images loaded", Toast.LENGTH_SHORT).show();
                        } else {
                            hideLoadingAnimation();
                            Toast.makeText(MainActivity.this, "No images found in the selected folder", Toast.LENGTH_SHORT).show();
                            showWelcomeLayout();
                        }
                    }
                });
            }
        });
    }

    private void loadImagesRecursively(DocumentFile directory, List<DocumentFile> tempImageFiles) {
        DocumentFile[] files = directory.listFiles();
        if (files != null) {
            for (DocumentFile file : files) {
                if (file.isDirectory()) {
                    loadImagesRecursively(file, tempImageFiles);
                } else if (file.isFile() && file.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    tempImageFiles.add(file);
                    if (file.getName().startsWith("☆☆☆☆☆")) {
                        zeroStarImages.add(file);
                    }
                }
            }
        }
    }

    private void showWelcomeLayout() {
        welcomeLayout.setVisibility(View.VISIBLE);
        ratingLayout.setVisibility(View.GONE);
        infoLayout.setVisibility(View.GONE);
        showSystemUI();
    }

    private void showRatingLayout() {
        welcomeLayout.setVisibility(View.GONE);
        ratingLayout.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
        hideSystemUI();
    }

    private void showInfoLayout() {
        welcomeLayout.setVisibility(View.GONE);
        ratingLayout.setVisibility(View.GONE);
        infoLayout.setVisibility(View.VISIBLE);

        showSystemUI();
    }



    private void displayCurrentImage() {
        if (currentImageIndex >= 0 && currentImageIndex < imageFiles.size()) {
            DocumentFile currentImage = imageFiles.get(currentImageIndex);
            try {
                hideLoadingAnimation();
                Glide.with(this)
                        .load(currentImage.getUri())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView);
                tvFileName.setText(currentImage.getName());
            } catch (Exception e) {
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void setupRatingButtons() {
        int[] buttonIds = {R.id.btnRate0, R.id.btnRate1, R.id.btnRate2, R.id.btnRate3, R.id.btnRate4, R.id.btnRate5};
        for (int i = 0; i < buttonIds.length; i++) {
            final int rating = i;
            ratingLayout.findViewById(buttonIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rateCurrentImage(rating);
                }
            });
        }
    }

    private void setupNavigationButtons() {
        ratingLayout.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateImages(-1);
            }
        });
        ratingLayout.findViewById(R.id.btnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteZeroStarImages();
                showWelcomeLayout();
            }
        });
        ratingLayout.findViewById(R.id.btnSkip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateImages(1);
            }
        });
    }

    private void rateCurrentImage(int rating) {
        if (currentImageIndex >= 0 && currentImageIndex < imageFiles.size()) {
            DocumentFile currentImage = imageFiles.get(currentImageIndex);
            String oldName = currentImage.getName();
            boolean ratingChanged = true;

            // Check if the image already has a star rating
            if (oldName.matches("^[★☆]{5}.*")) {
                int currentRating = countStars(oldName.substring(0, 5));

                // If the new rating is the same as the current rating, don't rename the file
                if (rating == currentRating) {
                    ratingChanged = false;
                }
            }

            if (ratingChanged) {
                String newName = getStarPrefix(rating) + " " + oldName.replaceFirst("^[★☆]{5} ", "");

                try {
                    Uri newUri = DocumentsContract.renameDocument(getContentResolver(), currentImage.getUri(), newName);
                    if (newUri != null) {
                        DocumentFile renamedFile = DocumentFile.fromSingleUri(this, newUri);
                        imageFiles.set(currentImageIndex, renamedFile);
                        if (rating == 0) {
                            zeroStarImages.add(renamedFile);
                        } else {
                            zeroStarImages.remove(currentImage);
                        }
                    } else {
                        throw new Exception("Failed to rename file");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error rating image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            // Show rating overlay if enabled
            if (showRatingOverlay) {
                showRatingOverlay(rating);
            } else {
                displayNextUnratedImage();            }
        }
    }
    private void showRatingOverlay(int rating) {
        if (showRatingOverlay) {
            ratingOverlayView.setVisibility(View.VISIBLE);
            ratingOverlayView.setRating(rating);
            handler.postDelayed(() -> {
                ratingOverlayView.setVisibility(View.GONE);
                navigateImages(1);
            }, 166);
        } else {
            navigateImages(1);
        }
    }

    private void showLoadingAnimation() {
        imageView.setVisibility(View.GONE);
        loadingStars.setVisibility(View.VISIBLE);
        toggleStatusText.setVisibility(View.VISIBLE);
        toggleStatusText.setText("loading...");
        tvFileName.setText("");
        loadingStars.startAnimation();
    }

    private void hideLoadingAnimation() {
        loadingStars.setVisibility(View.GONE);
        toggleStatusText.setVisibility(View.GONE);

        imageView.setVisibility(View.VISIBLE);
    }

    // New helper method to count stars in a rating prefix
    private int countStars(String ratingPrefix) {
        int count = 0;
        for (char c : ratingPrefix.toCharArray()) {
            if (c == '★') count++;
        }
        return count;
    }

    private String getStarPrefix(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }

    private void navigateImages(int direction) {
        currentImageIndex = (currentImageIndex + direction + imageFiles.size()) % imageFiles.size();
        displayCurrentImage();
    }

    private void displayNextUnratedImage() {
        int startIndex = currentImageIndex;
        do {
            currentImageIndex = (currentImageIndex + 1) % imageFiles.size();
            if (!isImageRated(imageFiles.get(currentImageIndex))) {
                displayCurrentImage();
                return;
            }
        } while (currentImageIndex != startIndex);

        Toast.makeText(this, "No more unrated images", Toast.LENGTH_SHORT).show();
        showWelcomeLayout();
    }


    private boolean isImageRated(DocumentFile image) {
        String name = image.getName();
        return name != null && name.matches("^[★☆]{5}.*");
    }

    private void deleteZeroStarImages() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (DocumentFile file : zeroStarImages) {
                    file.delete();
                }
                imageFiles.removeAll(zeroStarImages);
                zeroStarImages.clear();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (ratingLayout.getVisibility() == View.VISIBLE) {
                hideSystemUI();
            } else {
                showSystemUI();
            }
        }
    }

}