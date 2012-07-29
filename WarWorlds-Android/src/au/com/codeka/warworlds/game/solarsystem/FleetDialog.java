package au.com.codeka.warworlds.game.solarsystem;

import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import au.com.codeka.warworlds.DialogManager;
import au.com.codeka.warworlds.R;
import au.com.codeka.warworlds.ctrl.FleetList;
import au.com.codeka.warworlds.game.FleetMoveDialog;
import au.com.codeka.warworlds.game.FleetSplitDialog;
import au.com.codeka.warworlds.model.Fleet;
import au.com.codeka.warworlds.model.Star;
import au.com.codeka.warworlds.model.StarManager;

public class FleetDialog extends Dialog implements DialogManager.DialogConfigurable,
                                                   StarManager.StarFetchedHandler{
    public static final int ID = 1003;

    public FleetDialog(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.solarsystem_fleet_dlg);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = LayoutParams.MATCH_PARENT;
        params.width = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

    }

    @Override
    public void onStop() {
        StarManager.getInstance().removeStarUpdatedListener(this);
    }

    @Override
    public void setBundle(final Activity activity, Bundle bundle) {
        final String starKey = bundle.getString("au.com.codeka.warworlds.StarKey");

        StarManager.getInstance().requestStar(starKey, false, this);
        StarManager.getInstance().addStarUpdatedListener(starKey, this);

        final FleetList fleetList = (FleetList) findViewById(R.id.fleet_list);
        fleetList.setOnFleetActionListener(new FleetList.OnFleetActionListener() {
            @Override
            public void onFleetSplit(Star star, Fleet fleet) {
                Bundle args = new Bundle();
                args.putParcelable("au.com.codeka.warworlds.Fleet", fleet);
                DialogManager.getInstance().show(activity, FleetSplitDialog.class, args);
            }

            @Override
            public void onFleetMove(Star star, Fleet fleet) {
                Bundle args = new Bundle();
                args.putString("au.com.codeka.warworlds.StarKey", fleet.getStarKey());
                args.putString("au.com.codeka.warworlds.FleetKey", fleet.getKey());
                DialogManager.getInstance().show(activity, FleetMoveDialog.class, args);
            }
        });
    }

    @Override
    public void onStarFetched(Star s) {
        TreeMap<String, Star> stars = new TreeMap<String, Star>();
        stars.put(s.getKey(), s);

        final FleetList fleetList = (FleetList) findViewById(R.id.fleet_list);
        fleetList.refresh(getOwnerActivity(), s.getFleets(), stars);
    }
}