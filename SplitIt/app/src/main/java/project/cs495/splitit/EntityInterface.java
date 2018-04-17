package project.cs495.splitit;

import com.google.firebase.database.DatabaseReference;

public interface EntityInterface {
    void commitToDB(DatabaseReference mDatabase);
}
