package br.com.aguardente.economix.daos;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import br.com.aguardente.economix.models.Gasto;
import br.com.aguardente.economix.models.Usuario;

/**
 * Created by joao on 26/04/17.
 */

public class GastoDao {

    private static final String TAG = "GASTO_DAO";

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private boolean canDo = false;
    private Activity activity;
    private Context context;
    private Toast toastOnStart;

    public GastoDao(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            database = FirebaseDatabase.getInstance();
        }
        if (database != null) {
            canDo = true;
        }
    }

    public GastoDao(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            database = FirebaseDatabase.getInstance();
        }
        if (database != null) {
            canDo = true;
        }
    }

    public void salvarGasto(Usuario usuario, Gasto gasto) {
        if (canDo) {
            Toast.makeText(activity, "Registrando gasto...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Registrando gasto...");
            DatabaseReference myRef = database.getReference();
            String key = myRef.child("users").child(usuario.getUid()).push().getKey();
            gasto.setUid(key);

            Map<String, Object> gastoValues = gasto.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/users/" + usuario.getUid() + "/gastos/" + key, gastoValues);
            childUpdates.put("/users/" + usuario.getUid() + "/dias/" + gasto.getData() + "/" + key, gastoValues);

            myRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(activity, "Gasto registrado!", Toast.LENGTH_SHORT).show();
                        activity.setResult(Activity.RESULT_OK);
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "Gasto não registrado!", Toast.LENGTH_SHORT).show();
                        activity.setResult(Activity.RESULT_CANCELED);
                        activity.finish();
                    }
                }
            });

//            myRef.child("users").child(usuario.getUid()).child("gastos").child(key)
//                    .setValue(gasto).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        onSuccess("Gasto registrado!");
//                    } else {
//                        onFail("Gasto não registrado!");
//                    }
//                }
//            });
        }
    }

    public void deletarGasto(Usuario usuario, Gasto gasto, final CardView cardView) {
        if (canDo) {
            Toast.makeText(context, "Deletando gasto...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Deletando gasto...");
            DatabaseReference myRef =
                    database.getReference("users/" + usuario.getUid() + "/gastos/" + gasto.getUid());

            DatabaseReference myRef2 =
                    database.getReference("users/" + usuario.getUid() + "/dias/"
                            + gasto.getData() + "/" + gasto.getUid());

            myRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Gasto deletado!", Toast.LENGTH_SHORT).show();
                        cardView.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "Gasto não deletado!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            myRef2.removeValue();
        }
    }
}
