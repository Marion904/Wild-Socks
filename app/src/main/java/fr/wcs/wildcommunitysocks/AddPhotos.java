package fr.wcs.wildcommunitysocks;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AddPhotos extends Fragment implements View.OnClickListener{

    //private StorageReference mStorageRef;
    private static final int PICK_PHOTO = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 234;
    private String sdf = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    ImageView showPhoto;
    private Uri imageUri;
    private Button buttonTakePicture;
    private Button buttonSelectFromGallery;
    private Button buttonUpload;
    private String mCurrentPhotoPath;
    private StorageReference mStorageRef;
    private Chaussette mChaussette;
    private EditText mEditTextLegende;
    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;


    public static AddPhotos newInstance() {
        AddPhotos fragment = new AddPhotos();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStorageRef= FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);
        View view = inflater.inflate(R.layout.fragment_add_photos, container, false);

        showPhoto = (ImageView) view.findViewById(R.id.imageView);
        buttonSelectFromGallery = (Button) view.findViewById(R.id.galleryButton);
        buttonTakePicture=(Button) view.findViewById(R.id.cameraButton);
        buttonUpload=(Button) view.findViewById(R.id.buttonUpload);
        mEditTextLegende = (EditText) view.findViewById(R.id.editsockName);

        buttonTakePicture.setOnClickListener(this);
        buttonSelectFromGallery.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        return view;
    }



    private void openGallery() {
         Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
         gallery.setType("image/*");

         gallery.putExtra("crop", "true");
         gallery.putExtra("scale", true);
         gallery.putExtra("outputX", 256);
         gallery.putExtra("outputY", 256);
         gallery.putExtra("aspectX", 1);
         gallery.putExtra("aspectY", 1);
         gallery.putExtra("return-data", true);

         startActivityForResult(gallery, PICK_PHOTO);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /**Ensure there is a camera activity to handle the Intent*/
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            /** Create the file where the photo should go
             */
            File photoFile=null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),"com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            }


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == PICK_PHOTO) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            showPhoto.setImageBitmap(imageBitmap);
            imageUri = data.getData();
            //showPhoto.setImageURI(imageUri);
            //galleryAddPic();

            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageUri=data.getData();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            showPhoto.setImageBitmap(imageBitmap);
            //photo.setImageURI(imageUri);
            galleryAddPic();
            return;
        }
    }
    private File createImageFile() throws IOException {
       // Create an image file name
        String sdf = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + sdf + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(imageUri!=null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Chargement en cours...");
            progressDialog.show();


            StorageReference picRef = mStorageRef.child(Constants.STORAGE_PATH_UPLOADS+ System.currentTimeMillis()+"."+getFileExtension(imageUri));

            picRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Upload successfull", Toast.LENGTH_LONG);
                            mChaussette = new Chaussette(taskSnapshot.getDownloadUrl().toString(),mEditTextLegende.getText().toString().trim(),FirebaseAuth.getInstance().getCurrentUser().getUid());

                            //adding an upload to firebase database
                            String uploadId = mDatabase.push().getKey();
                            mDatabase.child(uploadId).setValue(mChaussette);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG);

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded :"+(int)progress+"%");

                        }


                    });

        }
        else{
            Toast.makeText(getActivity(),"Foirage", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonTakePicture) {
            dispatchTakePictureIntent();
        }
        if (v == buttonSelectFromGallery) {
            openGallery();
        }
        if (v == buttonUpload) {
            uploadFile();
        }

    }



}
