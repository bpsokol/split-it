package project.cs495.splitit;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Utils {
    private static FirebaseDatabase database;
    private static DatabaseReference databaseRef;

    public static FirebaseDatabase getDatabaseInstance() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }

    public static DatabaseReference getDatabaseReference() {
        if (databaseRef == null) {
            databaseRef = Utils.getDatabaseInstance().getReference();
        }
        return databaseRef;
    }
}