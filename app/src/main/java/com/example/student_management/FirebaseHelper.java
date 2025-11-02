package com.example.student_management;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    // Lấy instance FirebaseAuth
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    // Lấy instance FirebaseFirestore
    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }
}
