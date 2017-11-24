package br.edu.infnet.firebaseaula;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Button btnUploadImagem;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUploadImagem = (Button) findViewById(R.id.btnUploadImagem);

        /**
         * Obtem a referencia ao objeto FirebaseStorage ondem os arquivos são armazenados.
         */
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReferenceFromUrl("gs://fir-aula-a14f8.appspot.com/").child("firebase.png");

        /**
         * Realiza o UPLOAD DE IMAGEM
          */

        btnUploadImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerUploadImagem(storageReference);
            }
        });

    }

    private void fazerUploadImagem(StorageReference storageReference){
        AssetManager assetManager = MainActivity.this.getAssets();
        InputStream istr;
        Bitmap bitmap;
        try {
            //recupera a imagem na pasta de assets
            istr = assetManager.open("firebase.png");
            bitmap = BitmapFactory.decodeStream(istr);

            //decodifica em bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] data = outputStream.toByteArray();

            //carrega no firebase
            showProgressDialog("Fazendo upload de imagem", "Por favor, aguarde...");

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                    dismissProgressDialog();
                    Toast.makeText(MainActivity.this, "Ocorre uma falha durante o upload!", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dismissProgressDialog();
                    Toast.makeText(MainActivity.this, "Upload realizado com sucesso!", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog(String titulo, String mensagem){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(titulo);
        progressDialog.setMessage(mensagem);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
