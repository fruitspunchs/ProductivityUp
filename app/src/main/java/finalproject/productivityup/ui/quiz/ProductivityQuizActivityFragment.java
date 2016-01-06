package finalproject.productivityup.ui.quiz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import finalproject.productivityup.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProductivityQuizActivityFragment extends Fragment {

    public ProductivityQuizActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productivity_quiz, container, false);
    }
}
