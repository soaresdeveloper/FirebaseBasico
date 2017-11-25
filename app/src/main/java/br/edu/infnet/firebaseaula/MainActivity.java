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
import android.widget.ImageView;
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

    private static final String URL_STORAGE = "gs://fir-aula-a14f8.appspot.com/";
    private static final String MENSAGEM_SUCESSO_UP = "Upload realizado com sucesso!";
    private static final String MENSAGEM_ERRO_UP = "Ocorreu uma falha durante o upload!";
    Button btnUploadImagem, btnUploadArquivo, btnDownloadImagem;
    ImageView imageView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUploadImagem = (Button) findViewById(R.id.btnUploadImagem);
        btnUploadArquivo = (Button) findViewById(R.id.btnUploadArquivo);
        btnDownloadImagem = (Button) findViewById(R.id.btnDownloadImagem);
        imageView = (ImageView) findViewById(R.id.imgView);

        /**
         * Obtem a referencia ao objeto FirebaseStorage ondem os arquivos são armazenados.
         */
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReferenceFromUrl(URL_STORAGE).child("firebase.png");

        /**
         * Realiza o UPLOAD DE IMAGEM
         */

        btnUploadImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerUploadImagem(storageReference);
            }
        });

        /**
         * Realiza UPLOAD DE ARQUIVO
         */
        btnUploadArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerUploadArquivo(storageReference, storage);
            }
        });

        btnDownloadImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerDownloadImagem();
            }
        });
    }

    private void fazerUploadImagem(StorageReference storageReference) {
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
            showProgressDialog("Realizando upload da imagem", "Por favor, aguarde...");

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                    dismissProgressDialog();
                    Toast.makeText(MainActivity.this, MENSAGEM_ERRO_UP, Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dismissProgressDialog();
                    Toast.makeText(MainActivity.this, MENSAGEM_SUCESSO_UP, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fazerUploadArquivo(StorageReference storageReference, FirebaseStorage storage) {
        storageReference = storage.getReferenceFromUrl(URL_STORAGE).child("teste_upload.txt");

        //Upload input stream to Firebase
        showProgressDialog("Realizando upload do arquivo", "Por favor, aguarde...");

        InputStream stream = getResources().openRawResource(R.raw.teste);
        UploadTask uploadTask = storageReference.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                dismissProgressDialog();
                Toast.makeText(MainActivity.this, MENSAGEM_ERRO_UP, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                dismissProgressDialog();
                Toast.makeText(MainActivity.this, MENSAGEM_SUCESSO_UP, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fazerDownloadImagem() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(URL_STORAGE).child("Fluminense_FC_escudo.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;

        //baixa o arquivo como um array de bytes
        showProgressDialog("Realizando o download da imagem", "Por favor, aguarde...");
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                dismissProgressDialog();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(MainActivity.this, "Download realizado com sucesso!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(String titulo, String mensagem) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(titulo);
        progressDialog.setMessage(mensagem);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
