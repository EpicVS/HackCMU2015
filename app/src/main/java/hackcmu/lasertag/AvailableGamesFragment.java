package hackcmu.lasertag;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AvailableGamesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AvailableGamesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AvailableGamesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private ListView availableGamesList;
    ArrayAdapter<String> adapter;
    List<String> adapterData;

    // TODO: Rename and change types and number of parameters
    public static AvailableGamesFragment newInstance() {
        AvailableGamesFragment fragment = new AvailableGamesFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public AvailableGamesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//          if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_available_games, container, false);

        adapterData = new ArrayList<String>();

        availableGamesList = (ListView) view.findViewById(R.id.available_hosts_list);

        adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_1, adapterData);

        availableGamesList.setAdapter(adapter);


        availableGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ((JoinGameActivity) getActivity()).selectHost(position);
            }

        });

//        updateList(new ArrayList<String>());

        return view;
    }


    public void updateList(List<String> values){
        adapterData.clear();
        adapterData.addAll(values);

        ((BaseAdapter) availableGamesList.getAdapter()).notifyDataSetChanged();
    }

}
