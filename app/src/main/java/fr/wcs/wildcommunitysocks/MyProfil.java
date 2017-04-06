package fr.wcs.wildcommunitysocks;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProfil extends Fragment implements View.OnClickListener {

    public static MyProfil newInstance() {
        MyProfil fragment = new MyProfil();
        return fragment;
    }

    //recyclerview object
    private RecyclerView rView;

    private GridLayoutManager lLayout;

    //adapter object
    AdapterFlux rcAdapter;

    //database reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<Chaussette> rowListItem;

    private TextView textViewDisplayName;
    private Button buttonModifyProfil;

    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_profil, container, false);


        textViewDisplayName = (TextView) view.findViewById(R.id.textViewDisplayName);
        buttonModifyProfil = (Button) view.findViewById(R.id.buttonModifyProfil);
        buttonModifyProfil.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // Display name User
        textViewDisplayName.setText(user.getDisplayName());

        final List<Chaussette> rowListItem = getAllItemList();
        lLayout = new GridLayoutManager(getActivity(), 3);

        rView = (RecyclerView) view.findViewById(R.id.recyclerViewMyProfile);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(lLayout);

        //progressDialog = new ProgressDialog(getActivity());

        rcAdapter = new AdapterFlux(getActivity(), rowListItem);
        rView.setAdapter(rcAdapter);

        //progressDialog.setMessage("Please wait...");
        //progressDialog.show();

        firebaseAuth =FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_SOCKS);
        Query query = mDatabase.child(firebaseAuth.getCurrentUser().getUid()).child(Constants.DATABASE_PATH_UPLOADS);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //progressDialog.dismiss();
                rowListItem.clear();

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Chaussette sock = postSnapshot.getValue(Chaussette.class);

                    rowListItem.add(sock);
                    rcAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        rView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), rView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        int itemPosition = rView.getChildLayoutPosition(view);
                        Chaussette item = rowListItem.get(itemPosition);
                        Intent intent = new Intent(getActivity(), MySocksActivity.class);
                        intent.putExtra("sock", item);
                        intent.putExtra("position", itemPosition);
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        int itemPosition = rView.getChildLayoutPosition(view);
                        final Chaussette item = rowListItem.get(itemPosition);

                        AlertDialog.Builder delete = new AlertDialog.Builder(getActivity());
                        delete.setTitle(getString(R.string.longClick));
                        delete.setMessage(getString(R.string.deleteSocks));
                        delete.setNegativeButton(getString(R.string.cancel), null);
                        delete.setPositiveButton(getString(R.string.confirm), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //rowListItem.add(sock);
                                mDatabase.child(firebaseAuth.getCurrentUser().getUid()).child(Constants.DATABASE_PATH_UPLOADS).child(item.getmIdChaussette()).removeValue();
                                mDatabase.child(Constants.DATABASE_PATH_ALL_UPLOADS).child(item.getmIdChaussette()).removeValue();
                            }
                        });
                        delete.show();
                    }
                })
        );



        return view;
    }
    private List<Chaussette> getAllItemList(){

        List<Chaussette> allItems = new ArrayList<>();


        return allItems;
    }

    @Override
    public void onClick(View v) {

        if(v == buttonModifyProfil) {
            startActivity(new Intent(getActivity(), ModifyProfil.class));

        }

    }
}