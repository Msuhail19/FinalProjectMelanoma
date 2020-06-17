package com.example.finalprojectmelanoma.Fragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalprojectmelanoma.DisplayResultsActivity;
import com.example.finalprojectmelanoma.MultipleRunActivity.NewRunActivity;
import com.example.finalprojectmelanoma.R;
import com.example.finalprojectmelanoma.HistoryRecyclerViewAdapter;
import com.example.finalprojectmelanoma.RunDetails;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Declare values to alter for this view
    private RecyclerView mRecycler;
    private static List<RunDetails>  lstDetails = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_history, container, false);

        // Load previous results from shared preferences
        loadData();

        // Set recycler
        mRecycler = view.findViewById(R.id.RecyclerView);
        setmRecycler();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void removeItem(final int position){
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to remove this item?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getActivity(), "Item removed", Toast.LENGTH_SHORT).show();
                        lstDetails.remove(position);
                        saveData();
                        setmRecycler();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void setmRecycler(){
        // add adapter and add layout manager
        HistoryRecyclerViewAdapter recAdapter = new HistoryRecyclerViewAdapter(getContext(), lstDetails);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recAdapter.setOnItemClickListener(new HistoryRecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }

            @Override
            public void onClickShowResults(int position) {
                Toast.makeText(getContext(), "Showing results", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DisplayResultsActivity.class);
                intent.putExtra("multiple", lstDetails.get(position).isMultipleImages());
                if(lstDetails.get(position).isMultipleImages()){
                    intent.putExtra("paths", lstDetails.get(position).getImagePaths());
                    intent.putExtra("probability", lstDetails.get(position).getProbabilities());
                }
                else{
                    intent.putExtra("image", lstDetails.get(position).getImagePath());
                    intent.putExtra("probability",lstDetails.get(position).getProbability());
                }

                startActivity(intent);
                setmRecycler();

            }
        });
        mRecycler.setAdapter(recAdapter);
    }

    public static void addItem(String path, float mal_prob, float ben_prob, String date){
        lstDetails.add(new RunDetails(path,mal_prob,ben_prob,date));
    }

    public static void addItem(ArrayList<String> paths, ArrayList<float[][]> probabilities, String date){
        lstDetails.add(new RunDetails(paths,probabilities,date));
    }

    /* Save arraylist to sharedPreferences */
    private void saveData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(lstDetails);
        editor.putString("run list", json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("run list", null);
        Type type = new TypeToken<ArrayList<RunDetails>>() {}.getType();
        List<RunDetails> localDetailList = gson.fromJson(json, type);
        try{
            if(localDetailList.size() > 0){
                lstDetails = localDetailList;
            }
        }
        catch(Exception e){ }
    }


    @Override
    public void setUserVisibleHint(boolean userVisibleHint){
        super.setUserVisibleHint(userVisibleHint);
        if (userVisibleHint){
            if(mRecycler != null ){
                // add adapter and add layout manager
                setmRecycler();
                saveData();
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        Log.v("", "HELLO");
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v("Action:  ","Deattatched ! ");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the results and potentially other fragments contained in that
     * results.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
