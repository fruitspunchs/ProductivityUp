package finalproject.productivityup.ui.accountability;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import finalproject.productivityup.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccountabilityChartActivityFragment extends Fragment {

    public AccountabilityChartActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accountability_chart, container, false);
    }
}
