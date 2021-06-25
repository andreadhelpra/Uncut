package com.andrea.uncut;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.andrea.uncut.Model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close; // The 'close' button
    ImageView image_profile; // The profile image
    TextView save; // The 'save' button
    TextView tv_change; // The 'change photo' button
    MaterialEditText fullname; // The 'Full Name' text input
    MaterialEditText username; // The 'Username' text input
    MaterialEditText bio; // The 'Bio' text input

    FirebaseUser firebaseUser; // The firebase user

    private Uri mImageUri; // The URI of the profile image
    private StorageTask uploadTask; // The upload to storage task
    StorageReference storageRef; // The storage reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // Get layout file

        // Assign View to each corresponding variable
        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get instance of the current user
        storageRef = FirebaseStorage.getInstance().getReference("uploads"); // location of uploaded image

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()); // Location of user
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // User ID
                fullname.setText(user.getFullname()); // Set 'fullname' to the current full name of the user
                username.setText(user.getUsername());  // Set 'username' to the current username of the user
                bio.setText(user.getBio()); // Set 'bio' to the current  bio of the user
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile); // Set 'image_profile' to the current image of the user
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Terminate current activity when 'close' is pressed
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Save changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(fullname.getText().toString(), // Update full name
                        username.getText().toString(),  // Update username
                        bio.getText().toString());  // Update bio
            }
        });

        // Prompt user to choose a new image and crop it when 'change photo' is selected
        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setAspectRatio(1,1).start(EditProfileActivity.this);
            }
        });

        // Prompt user to choose a new image and crop it when the profile image is selected
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setAspectRatio(1,1).start(EditProfileActivity.this);
            }
        });
    }

    // Update the changes in the database
    private void updateProfile(String fullname, String username, String bio){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()); // Get location of the user

        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", fullname); // Update full name
        map.put("username", username); // Update username
        map.put("bio", bio); // Update bio

        reference.updateChildren(map); // Push to database

        // Inform user the operation was successful
        Toast.makeText(EditProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
    }

    // Get the Mime type file extension for the profile image
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver(); // Provide access to content providers
        MimeTypeMap mime = MimeTypeMap.getSingleton(); // Get singleton instance of MimeTypeMap
        return mime.getExtensionFromMimeType(cR.getType(uri)); //Return registered extension for profile image
    }

    // Upload the image to the database
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show(); // Inform user the image is being uploaded
        if (mImageUri != null){ // When the image is not null
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri)); // Store unique reference for image uri

            uploadTask = fileReference.putFile(mImageUri); // Upload the image uri to the location reference
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException(); // If the task is not succcessful log the error
                    }
                    return fileReference.getDownloadUrl(); // Otherwise retrieve a download URL to share the file
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() { // Once the task is complete
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {  // If the task is successful
                        Uri downloadUri = task.getResult(); // Pass the download URL to the variable
                        String miUrlOk = downloadUri.toString(); // Convert the download URI to String

                        // Location of the current user in the db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String, Object> map1 = new HashMap<>(); // Hash map containing updated profile image
                        map1.put("imageurl", ""+miUrlOk); // Put profile image in the hash map
                        reference.updateChildren(map1); // Update database with the hash map

                        pd.dismiss(); // Close the progress dialog
                        // Inform user of the successful outcome
                        Toast.makeText(EditProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();

                    } else { // If the task fails inform user with the outcome
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() { // If operation does not complete show error message
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else { // If user did not select an image inform them
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Get activity result from CropImage
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) { // If image is cropped successfully

            CropImage.ActivityResult result = CropImage.getActivityResult(data); // Get its result
            mImageUri = result.getUri(); // Get the Uri of the result

            uploadImage(); // Upload the image to the db

        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();  // Otherwise show error message
        }
    }
}