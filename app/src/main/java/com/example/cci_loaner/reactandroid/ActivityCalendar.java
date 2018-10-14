package com.example.cci_loaner.reactandroid;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cci_loaner.reactandroid.Models.Bucket;
import com.example.cci_loaner.reactandroid.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cci-loaner on 4/7/18.
 */



public class ActivityCalendar extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private MaterialCalendarView calendarView;
    private ProgressBar progressBar;
    private CardView cardView;
    private ImageView backBtn;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private ArrayList<User> users;
    ArrayList<CalenderItem> calenderItems;
    ArrayList<CalenderItem> calendarItemsGroupedByTitle;
    HashMap<String,ArrayList<CalenderItem>> hashmap;
    private CardView viewForDateNotSelected;


    String currentUser;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();
        viewForDateNotSelected = findViewById(R.id.cal_no_date);
        hashmap = new HashMap<>();
        progressBar = findViewById(R.id.cal_progress);
        cardView = findViewById(R.id.cal_card);
        if(mAuth == null){
            Toast.makeText(this,"User Must Sign in",Toast.LENGTH_SHORT).show();
        }
        else{
            currentUser = convertEmailToParseable(mAuth.getCurrentUser().getEmail());
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.calenderRecycler);
        calendarView = findViewById(R.id.calendarView);
        backBtn = findViewById(R.id.cal_finish);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        users = new ArrayList<>();
        User u = new User();
        u.setName("Mohamed Salad");
        users.add(u);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        RecyclerViewMargin decoration = new RecyclerViewMargin(0, 1);
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        calenderItems = new ArrayList<>();
        calendarItemsGroupedByTitle = new ArrayList<>();
        mAdapter = new MyAdapter(calendarItemsGroupedByTitle);
        mRecyclerView.setAdapter(mAdapter);
        cardView.setVisibility(View.INVISIBLE);
        viewForDateNotSelected.setVisibility(View.VISIBLE);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                loadDataOnDateSelected(date.getMonth()+1,date.getDay(),date.getYear());
                Log.d("ActivityCalendar","Date selected: M/D/Y "+(date.getMonth()+1)+""+date.getDay()+""+date.getYear());
            }
        });
        initFirebaseTask();

    }
    public ArrayList<CalenderItem> groupUserByTitle(ArrayList<CalenderItem> itemsForSpecificDay){
        HashMap<String,CalenderItem> calendarHash = new HashMap<>();
        ArrayList<CalenderItem> calItems = new ArrayList<>();
        ArrayList<String> listOfKeys = new ArrayList<>();
        for (CalenderItem calItem: itemsForSpecificDay
             ) {
            if(calendarHash.containsKey(calItem.getTitle())){
                CalenderItem temp = calendarHash.get(calItem.getTitle());
                temp.setLength(temp.getLength() + calItem.getLength());
                int tempNum = temp.getNumItems();
                temp.setNumItems(tempNum+1);
                calendarHash.put(calItem.getTitle(),temp);
            }
            else{
                calendarHash.put(calItem.getTitle(),calItem);
                listOfKeys.add(calItem.getTitle());
            }
        }
        for (String key: listOfKeys
             ) {
            calItems.add(calendarHash.get(key));
            Log.d("ActivityCalendar","Key found: "+key);
        }
        return calItems;
    }
    public void printUsers(){
        for (User u: users
             ) {
            for (Bucket b: u.getBuckets()
                 ) {
                try {
                    Log.d("ActivityCalendar", "User: " + u.getName() + " Bucket " + b.getDate());
                }
                catch (Exception e){

                }

            }
        }
    }

    public void loadDataOnDateSelected(int month, int day, int year){
        calendarItemsGroupedByTitle.clear();
        calenderItems.clear();
        progressBar.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.INVISIBLE);
        if(hashmap.containsKey(month+""+day+""+year)) {
            calenderItems.addAll(hashmap.get(month+""+day+""+year));
            calendarItemsGroupedByTitle.addAll(groupUserByTitle(calenderItems));
        }
        mAdapter.notifyDataSetChanged();
        if(calendarItemsGroupedByTitle.size() > 0){
            cardView.setVisibility(View.VISIBLE);
            viewForDateNotSelected.setVisibility(View.INVISIBLE);
        }
        else {
            cardView.setVisibility(View.INVISIBLE);
            viewForDateNotSelected.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.VISIBLE);

    }
    public void calendarDates(){
        //printUsers();
        for(int i = 0; i < users.size(); i++){
            User u = users.get(i);
            for (Bucket b: u.getBuckets()
                 ) {
                try {
                    int[] getDays = convertEnglishDateToNumbers(b.getDate());
                    int month = getDays[0];
                    int day = getDays[1];
                    int year = getDays[2];
                    calendarView.addDecorator(new CustomDayDecorator(this,getDays[1],getDays[0],getDays[2] ));
                        CalenderItem calenderItem = new CalenderItem();
                        calenderItem.setFullName(u.getName());
                        calenderItem.setLength(b.getLength());
                        calenderItem.setTitle(b.getBucketName());
                        if(hashmap.containsKey((month+""+day+""+year))){
                        ArrayList<CalenderItem> temp = hashmap.get((month+""+day+""+year));
                        temp.add(calenderItem);
                        hashmap.put(month+""+day+""+year,temp);
                            Log.d("ActivityCalendar","Adding to hashmap M/D/Y "+month + "" + day + "" + year);

                        } else{
                            ArrayList<CalenderItem> temp = new ArrayList<>();
                            temp.add(calenderItem);
                            Log.d("ActivityCalendar","Adding to hashmap M/D/Y "+month + "" + day + "" + year);
                        hashmap.put(month + "" + day + "" + year, temp);
                    }
                    Log.d("ActivityCalendar", "User: " + u.getName() + " Bucket " + b.getDate());
                }
                catch (Exception e){
                    //e.printStackTrace();
                }

            }
        }
        mAdapter.notifyDataSetChanged();

    }


    public int[] convertEnglishDateToNumbers(String date){
        int[] days = new int[3];
        String day = date.split(" ")[1];
        String month = date.split(" ")[0];
        String year = date.split(" ")[2];
        if(month.toLowerCase().contains("jan")){
            days[0] = 1;
        }
        else if(month.toLowerCase().contains("feb")){
            days[0] = 2;
        }else if(month.toLowerCase().contains("mar")){
            days[0] = 3;
        }else if(month.toLowerCase().contains("apr")){
            days[0] = 4;
        }else if(month.toLowerCase().contains("may")){
            days[0] = 5;
        }else if(month.toLowerCase().contains("jun")){
            days[0] = 6;
        }else if(month.toLowerCase().contains("jul")){
            days[0] = 7;
        }else if(month.toLowerCase().contains("aug")){
            days[0] = 8;
        }else if(month.toLowerCase().contains("sept")){
            days[0] = 9;
        }else if(month.toLowerCase().contains("oct")){
            days[0] = 10;
        }else if(month.toLowerCase().contains("nov")){
            days[0] = 11;
        }else if(month.toLowerCase().contains("dec")){
            days[0] = 12;
        }
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(day);
        while (m.find()) {
            System.out.println(m.group());
            days[1] = Integer.parseInt(m.group());
        }
        days[2] = Integer.parseInt(year);
        return days;
    }
    public String convertEmailToParseable(String email){
        Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match= pt.matcher(email);
        while(match.find())
        {
            String s= match.group();
            email=email.replace(match.group(), "");        }
        return email;
    }
    public void initFirebaseTask(){
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                progressBar.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.INVISIBLE);
                //originalUsers.clear();
                for (DataSnapshot userSnap : dataSnapshot.getChildren()
                        ) {
                    User u = new User();
                    u.setName(userSnap.child("name").getValue(String.class));
                    u.setDate(userSnap.child("date").getValue(String.class));
                    u.setLanguageCode(userSnap.child("languageCode").getValue(String.class));
                    u.setSpeechContexts((ArrayList<String>) userSnap.child("speechContexts").getValue());
                    if (userSnap.child("bucketTerms").exists()) {
                        u.setSpeechContexts((ArrayList) userSnap.child("bucketTerms").getValue());
                    }
                    if (userSnap.child("records").exists()) {
                        for (DataSnapshot bucketSnap : userSnap.child("records").getChildren()
                                ) {
                            Bucket b = new Bucket();
                            b.setGsURl(bucketSnap.child("gsURL").getValue(String.class));
                            b.setSampleText(bucketSnap.child("sampleText").getValue(String.class));
                            b.setFullText(bucketSnap.child("fullText").getValue(String.class));
                            b.setLength(bucketSnap.child("length").getValue(Integer.class));
                            b.setBucketName(bucketSnap.child("bucketName").getValue(String.class));
                            b.setRecordingURL(bucketSnap.child("recordingURL").getValue(String.class));
                            try {
                                String dateToText = bucketSnap.child("date").getValue(String.class);
                                int[] splitDate = convertEnglishDateToNumbers(dateToText);
                                b.setDateMonthDayYear(splitDate[0],splitDate[1],splitDate[2]);
                            }
                            catch (Exception e){
                                //e.printStackTrace();
                            }
                            u.addBucket(b);
                        }
                    }
                    u.setNumBuckets(u.getBuckets().size());
                    users.add(u);
                }
                mAdapter.notifyDataSetChanged();
                calendarDates();
                progressBar.setVisibility(View.INVISIBLE);
                calendarView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }




    public static class CalenderItem{
       String fullName;
       String title;
       long length;
       int numItems;

       public CalenderItem(){
           numItems = 1;
       }

        public int getNumItems() {
            return numItems;
        }

        public void setNumItems(int numItems) {
            this.numItems = numItems;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<CalenderItem> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public  class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTitle;
            public TextView mFullName;
            public TextView mCount;
            public TextView mLength;
            public ViewHolder(View v) {
                super(v);
                mTitle = v.findViewById(R.id.cal_title);
                mFullName = v.findViewById(R.id.cal_name);
                mCount = v.findViewById(R.id.cal_count);
                mLength = v.findViewById(R.id.cal_length);
                //mTextView = v.findViewById();
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<CalenderItem> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_calendar_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
//            holder.mTextView.setText(mDataset.get(position));
            Log.d("ActivityCalender","Name: "+mDataset.get(position).getFullName()+" "+"Title: "+mDataset.get(position).getTitle());
            CalenderItem calItem = mDataset.get(position);
            holder.mFullName.setText(calItem.getFullName());
            holder.mTitle.setText(calItem.getTitle());
            holder.mLength.setText(calItem.getLength()+"");
            holder.mCount.setText(calItem.getNumItems()+"");

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }



    public class RecyclerViewMargin extends RecyclerView.ItemDecoration {
        private final int columns;
        private int margin;

        /**
         * constructor
         *
         * @param margin  desirable margin size in px between the views in the recyclerView
         * @param columns number of columns of the RecyclerView
         */
        public RecyclerViewMargin(@IntRange(from = 0) int margin, @IntRange(from = 0) int columns) {
            this.margin = margin;
            this.columns = columns;

        }

        /**
         * Set different margins for the items inside the recyclerView: no top margin for the first row
         * and no left margin for the first column.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildLayoutPosition(view);
            //set right margin to all
            outRect.right = margin;
            //set bottom margin to all
            outRect.bottom = margin;
            //we only add top margin to the first row
            if (position < columns) {
                outRect.top = margin;
            }
            //add left margin only to the first column
            if (position % columns == 0) {
                outRect.left = margin;
            }
        }
    }
    public class CustomDayDecorator implements DayViewDecorator {
        private Drawable drawable;
        private int day;
        private int month;
        private int year;
        CalendarDay currentDay;
        public CustomDayDecorator(Activity context, int day, int month, int year) {
            this.day = day;
            this.month = month-1;
            this.year = year;
            drawable = ContextCompat.getDrawable(context,     R.drawable.first_day_month);
           Calendar c = Calendar.getInstance();
           c.set(this.year,this.month,this.day);
            currentDay = CalendarDay.from(c);
            //currentDay = CalendarDay.from(;
        }
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(currentDay);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setSelectionDrawable(drawable);
        }
    }


}
