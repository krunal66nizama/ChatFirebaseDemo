package com.allianzcloud.chatfirebasedemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allianzcloud.chatfirebasedemo.Model.MessagesVo;
import com.allianzcloud.chatfirebasedemo.Model.UserVo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore mFirestore;

    Context context;
    UserVo userVo;

    ArrayList<MessagesVo> messagesVos;
    RecyclerView recyclerView;

    EditText editMessage;
    ImageView imgAttach, imgSend;
    private int REQUEST_TAKE_GALLERY_VIDEO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        mFirestore = FirebaseFirestore.getInstance();

        imgAttach = findViewById(R.id.imgAttach);
        imgSend = findViewById(R.id.imgSend);
        editMessage = findViewById(R.id.editMessage);

        userVo = getIntent().getParcelableExtra("data");

        recyclerView = findViewById(R.id.recyclerView);

        CollectionReference contactListener = mFirestore.collection("Messages");
        contactListener.addSnapshotListener((documentSnapshots, error) -> {
            if (error != null) {
                //Log.e(TAG, errorMsg, error);
            } else {
                //final Parser<T> parser = getParserFor(targetType);
                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                    getChat();
                    Toast.makeText(context, "change", Toast.LENGTH_SHORT).show();
                }
            }
        });


        getChat();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editMessage.getText().length() > 0) {

                    MessagesVo messagesVo = new MessagesVo();
                    messagesVo.setFile("");
                    messagesVo.setMsg(editMessage.getText().toString());
                    messagesVo.setType("text");
                    messagesVo.setReceiver("");
                    messagesVo.setSender(userVo.getId());
                    sendMessage(messagesVo);
                }
            }
        });

        imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference();

                StorageReference ref = storageReference.child("Videos/" + UUID.randomUUID().toString());
                ref.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Uri downloadUrl = uri;
                                        Log.e("path", String.valueOf(uri));

                                        MessagesVo messagesVo = new MessagesVo();
                                        messagesVo.setFile(String.valueOf(uri));
                                        messagesVo.setMsg("");
                                        messagesVo.setType("image");
                                        messagesVo.setReceiver("");
                                        messagesVo.setSender(userVo.getId());
                                        sendMessage(messagesVo);
                                    }

                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                Log.e("uploaded", "Uploaded " + (int) progress + "%");

                            }
                        });

            }
        }
    }

    private void sendMessage(MessagesVo messagesVo) {
        mFirestore.collection("Messages")
                .add(messagesVo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        editMessage.setText("");
                        Log.d("MessageSend", "Event document added - id: "
                                + documentReference.getId());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MessageSend", "Error adding event document", e);
                        Toast.makeText(context, "Event document could not be added", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getChat() {
        try {
            mFirestore.collection("Messages")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //cancelDialog();
                                messagesVos = new ArrayList<>();

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    Log.e("items", document.getId() + " => " + document.getData());

                                    JSONObject jsonObject = new JSONObject(document.getData());
                                    Gson gson = new Gson();
                                    MessagesVo userVo = gson.fromJson(jsonObject.toString(), MessagesVo.class);

                                    messagesVos.add(userVo);

                                }

                                setData();
                            } else {
                                //cancelDialog();
                                Log.e("items", "Error getting documents.", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    private void setData() {

        Collections.reverse(messagesVos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        ChatAdapter adapter = new ChatAdapter(context, messagesVos, userVo.getId());
        recyclerView.setAdapter(adapter);

    }
}
