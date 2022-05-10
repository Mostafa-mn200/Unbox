package com.example.Unbox.Group;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.Unbox.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class groupFragment extends Fragment {
View view;
ListView list_View;
ArrayAdapter<String>arrayAdapter;
ArrayList<String>list_of_group=new ArrayList<>();
DatabaseReference GroupRef;
    public groupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_group, container, false);
        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        Intializefield();
        RetriveAndDisplayGroups();

        list_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGroupName=adapterView.getItemAtPosition(position).toString();
                Intent groupchatIntent=new Intent(getContext(), GroupChatActivity.class);
                groupchatIntent.putExtra("GroupName",currentGroupName);
                startActivity(groupchatIntent);
            }
        });

        return view;
    }

    private void Intializefield() {
        list_View=view.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,list_of_group);
        list_View.setAdapter(arrayAdapter);
    }

    private void RetriveAndDisplayGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String>set=new HashSet<>();
                //read line gy line
                Iterator iterator=snapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    //get all keys of groups it mean all groupname
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                list_of_group.clear();
                list_of_group.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}