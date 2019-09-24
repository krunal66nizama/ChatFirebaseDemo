package com.allianzcloud.chatfirebasedemo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allianzcloud.chatfirebasedemo.Model.MessagesVo;
import com.allianzcloud.chatfirebasedemo.Model.UserVo;
import com.allianzcloud.chatfirebasedemo.Util.Constant;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements ChatAdapter.ClickListener {

    FirebaseFirestore mFirestore;

    Context context;
    UserVo userVo;

    ArrayList<MessagesVo> messagesVos;
    RecyclerView recyclerView;

    EditText editMessage;
    ImageView imgAttach, imgSend, imgRecord;
    private int REQUEST_TAKE_GALLERY_VIDEO = 101;

    public String type = "";
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;

    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        mFirestore = FirebaseFirestore.getInstance();

        imgAttach = findViewById(R.id.imgAttach);
        imgSend = findViewById(R.id.imgSend);
        imgRecord = findViewById(R.id.imgRecord);
        editMessage = findViewById(R.id.editMessage);

        userVo = getIntent().getParcelableExtra("data");

        recyclerView = findViewById(R.id.recyclerView);

        CollectionReference contactListener = mFirestore.collection(Constant.collectionMessage);
        contactListener.addSnapshotListener((documentSnapshots, error) -> {
            if (error != null) {
                //Log.e(TAG, errorMsg, error);
            } else {
                //final Parser<T> parser = getParserFor(targetType);
                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
                    getChat();
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
                    messagesVo.setType(Constant.typeText);
                    messagesVo.setReceiver("");
                    messagesVo.setThumb("");
                    messagesVo.setSender(userVo.getId());
                    messagesVo.setTimeStamp(System.currentTimeMillis());
                    sendMessage(messagesVo);
                }
            }
        });

        imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_chooser);

                dialog.findViewById(R.id.txtImage).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("IntentReset")
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        type = Constant.typeImage;

                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");

                        @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");

                        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                        startActivityForResult(chooserIntent, REQUEST_TAKE_GALLERY_VIDEO);
                    }
                });

                dialog.findViewById(R.id.txtVideo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        type = Constant.typeVideo;

                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");

                        @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("video/*");

                        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                        startActivityForResult(chooserIntent, REQUEST_TAKE_GALLERY_VIDEO);

                    }
                });


                dialog.show();
            }
        });

        imgRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isRecording) {
                    if (checkPermission()) {

                        String name = CreateRandomAudioFileName(5);
                        if (name != null) {
                            AudioSavePathInDevice =
                                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                            name + "AudioRecording.3gp";

                            MediaRecorderReady();

                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();

                                isRecording = true;
                            } catch (IllegalStateException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            imgRecord.setBackgroundResource(R.drawable.ic_action_record_stop);

                            Toast.makeText(MainActivity.this, "Recording started",
                                    Toast.LENGTH_LONG).show();
                        }

                    } else {
                        requestPermission();
                    }
                } else {
                    mediaRecorder.stop();

                    imgRecord.setBackgroundResource(R.drawable.ic_action_record_stop);

                    Log.e("file", AudioSavePathInDevice);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference();

                    StorageReference ref = null;
                    if (type.equalsIgnoreCase(Constant.typeImage)) {
                        ref = storageReference.child(Constant.storageImage + UUID.randomUUID().toString());
                    } else if (type.equalsIgnoreCase(Constant.typeVideo)) {
                        ref = storageReference.child(Constant.storageVideo + UUID.randomUUID().toString());
                    } else {
                        ref = storageReference.child(Constant.storageAudio + UUID.randomUUID().toString());
                    }
                    StorageReference finalRef = ref;

                    Uri uri = Uri.fromFile(new File(AudioSavePathInDevice));

                    ref.putFile(uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    finalRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Log.e("path", String.valueOf(uri));

                                            MessagesVo messagesVo = new MessagesVo();

                                            messagesVo.setFile(String.valueOf(uri));
                                            messagesVo.setMsg("");

                                            messagesVo.setType(Constant.typeAudio);
                                            messagesVo.setThumb("");

                                            messagesVo.setReceiver("");
                                            messagesVo.setSender(userVo.getId());
                                            messagesVo.setTimeStamp(System.currentTimeMillis());
                                            sendMessage(messagesVo);

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

                StorageReference ref = null;
                if (type.equalsIgnoreCase(Constant.typeImage)) {
                    ref = storageReference.child(Constant.storageImage + UUID.randomUUID().toString());
                } else if (type.equalsIgnoreCase(Constant.typeVideo)) {
                    ref = storageReference.child(Constant.storageVideo + UUID.randomUUID().toString());
                }

                StorageReference finalRef = ref;
                ref.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                finalRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Log.e("path", String.valueOf(uri));

                                        MessagesVo messagesVo = new MessagesVo();
                                        messagesVo.setFile(String.valueOf(uri));
                                        messagesVo.setMsg("");

                                        if (type.equalsIgnoreCase(Constant.typeImage)) {
                                            messagesVo.setType(Constant.typeImage);
                                            messagesVo.setThumb("");
                                        } else if (type.equalsIgnoreCase(Constant.typeVideo)) {
                                            messagesVo.setType(Constant.typeVideo);

                                            Bitmap bitmap = null;
                                            try {
                                                bitmap = retriveVideoFrameFromVideo(String.valueOf(uri));
                                            } catch (Throwable throwable) {
                                                throwable.printStackTrace();
                                            }

                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            assert bitmap != null;
                                            bitmap = Bitmap.createScaledBitmap(bitmap, 200, 120, false);
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);

                                            byte[] b = baos.toByteArray();

                                            messagesVo.setThumb(Base64.encodeToString(b, Base64.DEFAULT));
                                        }

                                        messagesVo.setReceiver("");
                                        messagesVo.setSender(userVo.getId());
                                        messagesVo.setTimeStamp(System.currentTimeMillis());
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

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
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
            mFirestore.collection(Constant.collectionMessage)
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
        Collections.sort(messagesVos, new Comparator<MessagesVo>() {
            @Override
            public int compare(MessagesVo c1, MessagesVo c2) {
                return Double.compare(c1.getTimeStamp(), c2.getTimeStamp());
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ChatAdapter adapter = new ChatAdapter(context, messagesVos, userVo.getId());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onVideoClick(MessagesVo data) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_video_play);

        VideoView videoView = dialog.findViewById(R.id.video);

        videoView.setVideoPath(data.getFile());
        videoView.start();


        dialog.show();
    }

    @Override
    public void onAudioClick(MessagesVo data) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(data.getFile());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.start();
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string) {
        try {
            random = new Random();

            StringBuilder stringBuilder = new StringBuilder(string);
            int i = 0;
            while (i < string) {
                stringBuilder.append(RandomAudioFileName.
                        charAt(random.nextInt(RandomAudioFileName.length())));

                i++;
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("create File", e.toString());
            return null;
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            if (grantResults.length > 0) {
                boolean StoragePermission = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                boolean RecordPermission = grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED;

                if (StoragePermission && RecordPermission) {
                    Toast.makeText(MainActivity.this, "Permission Granted",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
