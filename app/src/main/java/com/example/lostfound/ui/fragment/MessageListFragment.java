package com.example.lostfound.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lostfound.data.AppDatabase;
import com.example.lostfound.databinding.FragmentMessageListBinding;
import com.example.lostfound.ui.ChatActivity;
import com.example.lostfound.ui.adapter.ContactAdapter;
import com.example.lostfound.util.SharedPrefsManager;

public class MessageListFragment extends Fragment {
    private FragmentMessageListBinding binding;
    private ContactAdapter adapter;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = SharedPrefsManager.getInstance(getContext()).getUserId();

        setupRecyclerView();
        observeContacts();
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(userId, contact -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("receiver_id", contact.id);
            intent.putExtra("receiver_name", contact.nickname != null ? contact.nickname : contact.username);
            startActivity(intent);
        });
        binding.rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvContacts.setAdapter(adapter);
    }

    private void observeContacts() {
        AppDatabase.getDatabase(requireContext()).messageDao().getContactList(userId)
                .observe(getViewLifecycleOwner(), messages -> {
                    if (messages == null || messages.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.rvContacts.setVisibility(View.GONE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                        binding.rvContacts.setVisibility(View.VISIBLE);
                        adapter.setContacts(messages);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
