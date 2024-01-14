package com.example.ciscarpool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ciscarpool.R;
import com.example.ciscarpool.classes.User;
import java.util.List;

/**
 * {@link LeaderboardAdapter} extends {@link RecyclerView.Adapter} to format data from Firebase
 * Firestore into a {@link RecyclerView} for {@link com.example.ciscarpool.fragments.LeaderboardFragment}
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public class LeaderboardAdapter extends
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<User> mUsers;

    /**
     * {@link ViewHolder} extends {@link RecyclerView.ViewHolder} to instantiate each individual
     * element in the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView totalRidesDone, personName, personRanking, totalCarbonSaved;

        // Constructor to instantiate each element.
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            totalRidesDone = (TextView) itemView.findViewById(R.id.totalRidesDone);
            personName = (TextView) itemView.findViewById(R.id.personName);
            personRanking = (TextView) itemView.findViewById(R.id.personRanking);
            totalCarbonSaved = (TextView) itemView.findViewById(R.id.totalCarbonSaved);
        }
    }

    // Pass in the User array into the constructor
    public LeaderboardAdapter(List<User> users) {
        mUsers = users;
    }

    /**
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return viewHolder instance.
     */
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.leaderboard_ranking_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    /**
     * Populates data into the item through the ViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(LeaderboardAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        User user = mUsers.get(position);

        // Set item views based on your views and data model
        TextView tv = holder.totalRidesDone;
        tv = holder.personName;
        tv.setText((CharSequence) (user.getFirstName() + " " + user.getLastName()));

        tv = holder.personRanking;
        tv.setText((CharSequence) ("#" + String.valueOf(position+1)));

        tv = holder.totalCarbonSaved;
        String numericKiloCarbonSavedStr = String.valueOf(user.getKiloCarbonSaved());
        tv.setText((CharSequence) ("Saved " + numericKiloCarbonSavedStr + " kg of carbon"));
    }


    /**
     * @return Returns the total count of items in the list
     */
    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}