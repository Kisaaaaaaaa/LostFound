package com.example.lostfound.ui.post;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.databinding.ActivityPostBinding;
import com.example.lostfound.data.entity.ItemPost;
import com.example.lostfound.ui.map.MapSelectActivity;
import com.example.lostfound.util.ImageUtils;
import com.example.lostfound.util.SharedPrefsManager;
import com.example.lostfound.viewmodel.ItemPostViewModel;

public class PostActivity extends AppCompatActivity {
    private ActivityPostBinding binding;
    private ItemPostViewModel viewModel;
    private String savedImageUri = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String locationName = "";
    private int editPostId = -1;
    private ItemPost existingPost;

    // 使用 PickVisualMedia 替代 GetContent，它更现代且稳定
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    savedImageUri = ImageUtils.saveImageToInternalStorage(this, uri);
                    if (savedImageUri != null) {
                        binding.ivPreview.setVisibility(View.VISIBLE);
                        binding.ivPreview.setImageURI(Uri.parse(savedImageUri));
                    }
                }
            });

    private final ActivityResultLauncher<Intent> selectLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    latitude = result.getData().getDoubleExtra("lat", 0.0);
                    longitude = result.getData().getDoubleExtra("lng", 0.0);
                    locationName = result.getData().getStringExtra("address");
                    binding.tvLocationName.setText(locationName);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ItemPostViewModel.class);

        editPostId = getIntent().getIntExtra("edit_post_id", -1);
        if (editPostId != -1) {
            setupEditMode();
        }

        binding.btnSelectImage.setOnClickListener(v -> checkPermissionAndSelectImage());

        binding.btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapSelectActivity.class);
            selectLocationLauncher.launch(intent);
        });

        binding.btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void setupEditMode() {
        setTitle("编辑信息");
        binding.btnSubmit.setText("保存修改");
        
        new Thread(() -> {
            existingPost = AppDatabase.getDatabase(this).itemPostDao().getPostByIdSync(editPostId);
            if (existingPost != null) {
                runOnUiThread(() -> {
                    binding.etTitle.setText(existingPost.title);
                    binding.etDescription.setText(existingPost.description);
                    
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.spinnerCategory.getAdapter();
                    if (adapter != null) {
                        int pos = adapter.getPosition(existingPost.category);
                        binding.spinnerCategory.setSelection(pos);
                    }
                    
                    if (existingPost.type == 0) binding.rbLost.setChecked(true);
                    else binding.rbFound.setChecked(true);
                    
                    savedImageUri = existingPost.imageUri;
                    if (savedImageUri != null && !savedImageUri.isEmpty()) {
                        binding.ivPreview.setVisibility(View.VISIBLE);
                        binding.ivPreview.setImageURI(Uri.parse(savedImageUri));
                    }
                    
                    latitude = existingPost.latitude;
                    longitude = existingPost.longitude;
                    locationName = existingPost.locationName;
                    binding.tvLocationName.setText(locationName);
                });
            }
        }).start();
    }

    private void checkPermissionAndSelectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 推荐直接使用 PickVisualMedia，不需要手动申请权限
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            // Android 12及以下仍需权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            }
    );

    private void submitPost() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        int type = binding.rbLost.isChecked() ? 0 : 1;
        int userId = SharedPrefsManager.getInstance(this).getUserId();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "标题和描述不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editPostId != -1 && existingPost != null) {
            existingPost.title = title;
            existingPost.description = description;
            existingPost.category = category;
            existingPost.type = type;
            existingPost.imageUri = savedImageUri;
            existingPost.latitude = latitude;
            existingPost.longitude = longitude;
            existingPost.locationName = locationName;
            viewModel.update(existingPost);
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        } else {
            ItemPost post = new ItemPost(type, title, category, description, savedImageUri,
                    latitude, longitude, locationName, System.currentTimeMillis(), userId, false);
            viewModel.insert(post);
            Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
