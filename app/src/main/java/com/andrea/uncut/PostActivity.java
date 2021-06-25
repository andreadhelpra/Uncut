package com.andrea.uncut;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUri; //The Uri of the Post image to pull from the database
    String myUrl = ""; // The Uri of the Post image that will be stored in the database
    StorageTask uploadTask; // The type of action to commit to the firebase storage
    StorageReference storageReference; // The location of the post image in the storage

    ImageView close; // The 'close' button
    ImageView imageAdded; // The image that is being added by the user
    TextView post; // The 'POST' text to press in order to publish the post
    RatingBar ratingBar; // 5 star rating bar
    float score; // 1-5 rating
    EditText title; // The film title
    EditText description; // The review of the post


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close=findViewById(R.id.close); // The 'close' button
        imageAdded=findViewById(R.id.image_added); // The image that is being added by the user
        post=findViewById(R.id.post); // The 'POST' text to press in order to publish the post
        ratingBar=findViewById(R.id.ratingBar); // 5 star rating bar
        title=findViewById(R.id.title); // The film title
        description=findViewById(R.id.description); // The review of the post
        storageReference= FirebaseStorage.getInstance().getReference("Posts"); //The post image stored in "Posts"

        // Handle 'close' button
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class)); // Return to Main Activity
                finish(); // Terminate this activity
            }
        });

        // Handle 'POST' text when pressed
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadReview();
            } // Upload the review to the database
        });

        // Prompt user to crop a selected image with ratio 2:3
        CropImage.activity().setAspectRatio(2,3).start(PostActivity.this);
    }

    // Create MIME type file extension to be added to the image uri
    private String getFileExtension (Uri uri) {
        ContentResolver contentResolver = getContentResolver(); // Provide access to content providers
        MimeTypeMap mime = MimeTypeMap.getSingleton(); // Get singleton instance of MimeTypeMap
        return mime.getExtensionFromMimeType(contentResolver.getType(uri)); //Return registered extension for post image
    }

    // Upload a review to the database
    private void uploadReview(){
        if (title.length() <= 0){ // Validate title is not empty
            Toast.makeText(PostActivity.this, "Film title is mandatory", Toast.LENGTH_SHORT).show();
        } else if(ratingBar.getRating() == 0){ // Validate rating is not empty
            Toast.makeText(PostActivity.this, "Rating is mandatory", Toast.LENGTH_SHORT).show();
        } else{ // Post is validated therefore publish
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Posting");
            progressDialog.show(); // Show a progress dialog informing user that item is being posted
            if (imageUri != null){
                // Store unique reference for image uri
                StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
                uploadTask = fileReference.putFile(imageUri); // Upload the image uri to the reference
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException(); // If the task is not succcessful log the error
                        }
                        return fileReference.getDownloadUrl(); // Otherwise retrieve a download URL to share the file
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() { // Once the task is complete
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){ // If the task is successful
                            Uri downloadUri = task.getResult(); // Pass the download URL to the variable
                            myUrl = downloadUri.toString(); // Convert the download URI to String
                            score = ratingBar.getRating(); // 1-5 rating in float
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts"); // Get reference where to store the post
                            String postID = reference.push().getKey(); // Create post ID
                            // Push to database
                            HashMap<String, Object> hashMap = new HashMap<>(); // Store hash map in the db
                            hashMap.put("postID", postID); // Set post ID
                            hashMap.put("postImage", myUrl); // Set image url
                            hashMap.put("score", score); // Set 1-5 score
                            hashMap.put("title", title.getText().toString()); // Set title
                            hashMap.put("description", description.getText().toString()); // Set description
                            hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid()); // Set publisher
                            reference.child(postID).setValue(hashMap); // Push hash map

                            progressDialog.dismiss(); // Close the progress dialog
                            startActivity(new Intent(PostActivity.this, MainActivity.class)); // Return to MainActivity
                            finish(); // Terminate current activity
                        } else { // If task was not successful show error message
                            Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {  // If task fails show error message
                        Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{ // If user does not select an image show toast
                Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get activity result from CropImage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){ // If image is cropped successfully
            CropImage.ActivityResult result = CropImage.getActivityResult(data); // Get its result
            imageUri = result.getUri(); // Get the Uri of the result
            imageAdded.setImageURI(imageUri); // Set the Uri to the image added
        } else {
            startActivity(new Intent(PostActivity.this, MainActivity.class)); // Otherwise go back to the Main Activity
            finish(); // Terminame current activiy
        }
    }
}