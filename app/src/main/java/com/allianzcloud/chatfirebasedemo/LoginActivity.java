package com.allianzcloud.chatfirebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.allianzcloud.chatfirebasedemo.Model.UserVo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore mFirestore;

    EditText edtUsername , edtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        mFirestore = FirebaseFirestore.getInstance();

        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });

    }

    private void login() {

        try {
            mFirestore.collection("Users")
                    .whereEqualTo("name", edtUsername.getText().toString())
                    .whereEqualTo("password", edtPassword.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //cancelDialog();
                                //galleryVos = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.e("items", document.getId() + " => " + document.getData());

                                    JSONObject jsonObject = new JSONObject(document.getData());
                                    Gson gson = new Gson();
                                    UserVo userVo = gson.fromJson(jsonObject.toString(), UserVo.class);
                                    userVo.setId(document.getId());

                                    Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                                    intent.putExtra("data" , userVo);
                                    startActivity(intent);

                                    break;
                                }
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
}
