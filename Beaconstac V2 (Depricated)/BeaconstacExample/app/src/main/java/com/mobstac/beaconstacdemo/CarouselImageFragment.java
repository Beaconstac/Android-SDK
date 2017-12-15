package com.mobstac.beaconstacdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;


public class CarouselImageFragment extends Fragment {

    NetworkImageView image;


    public static CarouselImageFragment newInstance(String url) {
        CarouselImageFragment fragment = new CarouselImageFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    public CarouselImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_carousel_dialog, container, false);

        image = (NetworkImageView) rootView.findViewById(R.id.carousel_image);

        String url = getArguments().getString("url");

        ImageLoader imgLoader = MyVolley.getInstance(getActivity().getApplicationContext())
                .getImageLoader();

        image.setImageUrl(url, imgLoader);

        return rootView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


}