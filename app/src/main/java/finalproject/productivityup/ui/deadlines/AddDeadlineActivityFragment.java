package finalproject.productivityup.ui.deadlines;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import finalproject.productivityup.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddDeadlineActivityFragment extends DialogFragment {

    public AddDeadlineActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_deadline, container, false);
    }
}
