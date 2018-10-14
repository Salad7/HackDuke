package com.example.cci_loaner.reactandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cci_loaner.reactandroid.Models.Bucket;
import com.example.cci_loaner.reactandroid.Models.Note;
import com.example.cci_loaner.reactandroid.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cci-loaner on 3/1/18.
 */

public class FragmentChooseUser extends Fragment {

    ArrayList<User> users;
    ArrayList<User> originalUsers;
    ArrayList<Note> notes;
    RecyclerView recyclerView;
    CustomUsersAdapter customUserAdapter;
    FirebaseDatabase database;
    DatabaseReference usersRef;
    DatabaseReference batchRef;
    String currentUser;
    ProgressBar pbChooseUser;
    EditText search;
    RecyclerView searchListView;
    private FirebaseAuth mAuth;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_user,container,false);
        mAuth = FirebaseAuth.getInstance();
        originalUsers = new ArrayList<>();
        currentUser = convertEmailToParseable(mAuth.getCurrentUser().getEmail());
        recyclerView = v.findViewById(R.id.recyclelist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        pbChooseUser = v.findViewById(R.id.progress_choose_user);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        batchRef = database.getReference("batch");
        search = v.findViewById(R.id.market_search_et);
        searchListView = v.findViewById(R.id.search_list);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ArrayList<User> searchedUsers = new ArrayList<>();
                for (User someuser: originalUsers
                     ) {
                    try {
                        if (someuser.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            searchedUsers.add(someuser);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(count == 0) {
                   users.clear();
                   users.addAll(originalUsers);
                   customUserAdapter.notifyDataSetChanged();
                    //Log.d("FragmentChooseUser","Adding original users");

                }
                else{
                    users.clear();
                    users.addAll(searchedUsers);
                    customUserAdapter.notifyDataSetChanged();
                    //Log.d("FragmentChooseUser","Adding search query");

                }
                //Log.d("FragmentChooseUser","EditText count "+count);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        users = new ArrayList<>();
        initFirebaseTask();
        return v;
    }

    public void toggleHideUser(){
        for (User _u: users
             ) {
            boolean isHideUser = _u.isHideUser();
            _u.setHideUser(!isHideUser);
        }
        customUserAdapter.notifyDataSetChanged();
    }

    public void initFirebaseTask(){
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                originalUsers.clear();
                for (DataSnapshot userSnap : dataSnapshot.getChildren()
                        ) {
                    User u = new User();
                    u.setName(userSnap.child("name").getValue(String.class));
                    u.setDate(userSnap.child("date").getValue(String.class));
                    u.setNotes((ArrayList<Note>) userSnap.child("notes").getValue());
                    ArrayList<Note> snapNotes = new ArrayList<>();
                    for (DataSnapshot noteSnap: userSnap.child("notes").getChildren()
                         ) {
                            Note n = new Note();
                            n.setTime(noteSnap.child("time").getValue(String.class));
                            n.setText(noteSnap.child("text").getValue(String.class));
                            n.setDateByEnglish(noteSnap.child("date").getValue(String.class));
                            snapNotes.add(n);
                    }
                    u.setNotes(snapNotes);
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
                            b.setRecordingURL(bucketSnap.child("recordingURL").getValue(String.class));
                            try {
                                String dateToText = bucketSnap.child("date").getValue(String.class);
                                String[] splitDate = dateToText.split(" ");
                                int month = Integer.parseInt(splitDate[0]);
                                int day = Integer.parseInt(splitDate[1]);
                                int year = Integer.parseInt(splitDate[2]);
                                b.setDateMonthDayYear(month,day,year);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            u.addBucket(b);
                        }
                    }
                    u.setNumBuckets(u.getBuckets().size());
                    users.add(u);
                    originalUsers.add(u);


                }
                customUserAdapter.notifyDataSetChanged();
                pbChooseUser.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        customUserAdapter = new CustomUsersAdapter(users,getActivity(),R.layout.custom_user);
        recyclerView.setAdapter(customUserAdapter);
        customUserAdapter.notifyDataSetChanged();

    }


    public void deleteAllRecords(final String theUserWhoseRecordsToDelete){
        usersRef.child(currentUser).child(theUserWhoseRecordsToDelete).child("records").removeValue();
        // dialogBuilder.dismiss();
        Toast.makeText(getContext(),"Removed user records!",Toast.LENGTH_SHORT).show();
    }

    public void deleteUser(final String theUserWhoseRecordsToDelete){
        usersRef.child(currentUser).child(theUserWhoseRecordsToDelete).removeValue();
        Toast.makeText(getContext(),"Removed entire user",Toast.LENGTH_SHORT).show();
    }



    public ArrayList<User> addUser(final User userToAdd){
        final ArrayList<User> temp = new ArrayList<>();
        final ArrayList<Bucket> tempBuckets = new ArrayList<>();

        usersRef.child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()
                     ) {
                    User uTemp = new User();
                    uTemp.setDate(snap.child("date").getValue(String.class));
                    uTemp.setName(snap.child("name").getValue(String.class));
                    uTemp.setSpeechContexts((ArrayList) snap.child("bucketTerms").getValue());
                    try {
                        //uTemp.setBuckets((ArrayList) snap.child("records"));
                        for(DataSnapshot record: dataSnapshot.child("records").getChildren()){
                            Bucket b = new Bucket();
                            b.setGsURl(record.child("gsURL").getValue(String.class));
                            b.setSampleText(record.child("sampleText").getValue(String.class));
                            b.setFullText(record.child("fullText").getValue(String.class));
                            b.setLength(record.child("sampleText").getValue(String.class).length());
                            b.setRecordingURL(record.child("recordingURL").getValue(String.class));
                            if(record.child("bucketName").getValue(String.class) != null){
                                b.setBucketName(record.child("bucketName").getValue(String.class));
                            }
                            tempBuckets.add(b);
                            uTemp.setBuckets(tempBuckets);
                            temp.add(uTemp);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
                temp.add(userToAdd);
                 HashMap<String,User> hashMap = new HashMap<>();
                for (User uniqueUser: temp
                     ) {
                    hashMap.put(uniqueUser.getName().replace(" ",""),uniqueUser);
                    
                }
        usersRef.child(currentUser).updateChildren((HashMap)hashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return temp;

    }
    public int[] getCurrentDateInMonthDayYear(){
        Calendar myCal = new GregorianCalendar();
        int[] dates = {myCal.get(Calendar.MONTH)+1 ,myCal.get(Calendar.DAY_OF_MONTH), myCal.get(Calendar.YEAR)};
        return dates;
    }

    public String getCurrentDate(){
        Calendar myCal = new GregorianCalendar();
        System.out.println("Day: " + myCal.get(Calendar.DAY_OF_MONTH));
        System.out.println("Month: " + myCal.get(Calendar.MONTH) + 1);
        System.out.println("Year: " + myCal.get(Calendar.YEAR));
        //Log.d("FragmentChooseUser","Day: " + myCal.get(Calendar.DAY_OF_MONTH)+ " Month: " + (myCal.get(Calendar.MONTH)+1) + " "+"Year: " + myCal.get(Calendar.YEAR));
        return "Day: " + myCal.get(Calendar.DAY_OF_MONTH)+ "Month: " + (myCal.get(Calendar.MONTH) + 1) + " "+"Year: " + myCal.get(Calendar.YEAR);
    }
    public void updateRecording(String recordingTitle, String transcription,String recordingUrl, String gsURl,String userRecordPath, long recordingLength) {
        Bucket b = new Bucket();
        ArrayList<Bucket> buckets = new ArrayList<>();
        try {
            b.setBucketName(recordingTitle);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        b.setFullText(transcription);
        b.setLength(recordingLength);
        b.setRecordingURL(recordingUrl);
        int[] dates = getCurrentDateInMonthDayYear();
        b.setDateMonthDayYear(dates[0],dates[1],dates[2]);
        b.setGsURl(gsURl);
        buckets.add(b);
        HashMap<String, Bucket> hashMap2 = new HashMap<>();
        for (Bucket bk: buckets
             ) {
            String key = usersRef.child(currentUser+"/"+userRecordPath).child("records").push().getKey();
            hashMap2.put(key,bk);
        }
       // hashMap2.put(key, buckets);
        usersRef.child(currentUser+"/"+userRecordPath).child("records").updateChildren((HashMap) hashMap2);
    }

    public class CustomUsersAdapter extends RecyclerView.Adapter{
        ArrayList<User> users;
        Context context;
        int res;


        public CustomUsersAdapter(ArrayList<User> users, Context context, int res) {
            this.users = users;
            this.context = context;
            this.res = res;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((SimpleViewHolder) holder).bindData(users.get(position),position);
        }



        @Override
        public int getItemCount() {
            return users.size();
        }
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView date;
        TextView numRecords;
        TextView numNotes;
        FloatingActionButton recordFab;
        FloatingActionButton viewFab;
        FloatingActionButton deleteFab;
        FloatingActionButton editPhrases_fab;
        FloatingActionButton notesFab;
        FloatingActionButton videoFab;
        ListView recListView;
        int optionSelectedFromRadioButton;



        public SimpleViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            date = itemView.findViewById(R.id.user_date);
            numRecords = itemView.findViewById(R.id.user_records);
            recordFab = itemView.findViewById(R.id.record_user_fab);
            viewFab = itemView.findViewById(R.id.view_record_fab);
            deleteFab = itemView.findViewById(R.id.delete_records_fab);
            recListView = itemView.findViewById(R.id.list_of_recordings_date);
            editPhrases_fab = itemView.findViewById(R.id.edit_phrases_fab);
            notesFab = itemView.findViewById(R.id.fab_notes);
            numNotes = itemView.findViewById(R.id.user_noteCount);
            videoFab = itemView.findViewById(R.id.fab_video);
            optionSelectedFromRadioButton = -1;
        }
        public void bindData(final User user, int position) {
            name.setText(user.getName());
            date.setText(user.getDate());
            numRecords.setText(user.getNumBuckets()+"");
            try {
                if (user.getNotes().size() > 0){
                    numNotes.setText(user.getNotes().size()+"");
                }
                else{
                    numNotes.setVisibility(View.INVISIBLE);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                numNotes.setVisibility(View.INVISIBLE);
            }
            if(user.getNumBuckets() == 0){
                //recordFab.setVisibility(View.INVISIBLE);
                //viewFab.setVisibility(View.INVISIBLE);
                //deleteFab.setVisibility(View.INVISIBLE);
                //date.setText("Click here to add recording");
//                date.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        ((MainActivity) getActivity()).initRecording(user.getName().replace(" ",""), user.getParcelableSpeechContext());
//                    }
//                });
            }

            //Changed for HackDuke
            if(user.isHideUser() && 0 > 1){
                name.setVisibility(View.INVISIBLE);
            }
            else{
                name.setVisibility(View.VISIBLE);

            }

            ArrayList<RecordingUtil> recordingUtils = new ArrayList<>();
            for(int i = 0; i < user.getBuckets().size(); i++){
                Bucket b = user.getBuckets().get(i);
                RecordingUtil recordingUtil = new RecordingUtil(position);
                if(i != 0) {
                    //Log.d("FragmentChooseUser","Adding: "+b.getLength()+" To "+Long.parseLong(recordingUtils.get(0).getLength()));
                    recordingUtils.get(0).setLength((Long.parseLong(recordingUtils.get(0).getLength())+b.getLength())+"");
                }
                recordingUtil.setLength(b.getLength() + "");
                recordingUtil.setNumSamples(b.getTranscripts().size());
                recordingUtil.setRecordingDate(getCurrentDate());
                recordingUtils.add(recordingUtil);
            }
            //customUserAdapter.notifyDataSetChanged();
            CustomRecordingSample customRecordingSample = new CustomRecordingSample(recordingUtils,getContext(),R.layout.custom_recording_sample,user);
            recListView.setAdapter(customRecordingSample);

            videoFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).startVideoFrag(user.getName());

                }
            });

            viewFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //run FragmentBucketStack;
                    if(user.getBuckets().size() > 0) {
                        Log.d("FragmentChooseUser","User that has been selected: "+user.getName().replace(" ", ""));
                        ((MainActivity) getActivity()).startStackFrag(user.getName().replace(" ", ""));
                    }
                    else{
                        Toast.makeText(getContext(),"There are no buckets to show",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recordFab.setOnClickListener(new View.OnClickListener() {
                User u = user;
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).initRecording(u.getName().replace(" ",""),u.getSpeechContexts(),null);
                }
            });
            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).initRecording(user.getName().replace(" ",""),user.getSpeechContexts(),null);
                }
            });

            deleteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //((MainActivity) getActivity()).initRecording(user.getName().replace(" ",""),user.getParcelableSpeechContext());
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setTitle("Options");
                    final String[] options = {"Remove all records","Remove entire user"};
                    dialogBuilder.setSingleChoiceItems(options, -1, new DialogInterface
                            .OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            Toast.makeText(getContext(),"Hit item "+item,Toast.LENGTH_SHORT).show();
                            optionSelectedFromRadioButton = item;

                        }
                    });
                    dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(optionSelectedFromRadioButton == 0){
                                deleteAllRecords(user.getName().replace(" ",""));
                            }
                            else if (optionSelectedFromRadioButton == 1){
                                deleteUser(user.getName().replace(" ",""));
                            }
                        }
                    });
                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // dialogBuilder.dismiss();

                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                }
            });

            editPhrases_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setTitle("Add keywords to search for in speeches");
                    //Load dialog for showing data
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_edit_bucketphrases, null);
                    dialogBuilder.setView(dialogView);
                    final EditText et_phrase =  dialogView.findViewById(R.id.et_phrase);
                    final Button addPhrase = dialogView.findViewById(R.id.btn_add_phrase);
                    final ListView phrase_lv = dialogView.findViewById(R.id.phrases_lv);
                    ArrayList<String> usersPhrases = user.getSpeechContexts();
                    if(usersPhrases == null){
                        user.setSpeechContexts(new ArrayList<String>());
                    }
                    final CustomDialogPhraseAdapter customDialogPhraseAdapter = new CustomDialogPhraseAdapter(getContext(),R.layout.custom_bucketphrase_for_dialog,user.getSpeechContexts(),user);
                    phrase_lv.setAdapter(customDialogPhraseAdapter);
                    final AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    addPhrase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(et_phrase.getText().toString().length() == 0){
                                Toast.makeText(getContext(),"Please enter a phrase",Toast.LENGTH_LONG).show();
                            }
                            else {
                                HashMap<String,ArrayList<String>> hashMap = new HashMap<>();
                                ArrayList<String> userPhrases = user.getSpeechContexts();
                                userPhrases.add(et_phrase.getText().toString());
                                hashMap.put("speechContexts",userPhrases);
                                usersRef.child(currentUser).child(user.getName().replace(" ","")).updateChildren( (HashMap) hashMap);
                                Toast.makeText(getContext(),"Added phrase to user",Toast.LENGTH_LONG).show();
                                customDialogPhraseAdapter.notifyDataSetChanged();
                                et_phrase.setText("");
                            }
                        }
                    });
                }
            });

            notesFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    search.setText("");
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setTitle("View notes");
                    //Load dialog for showing data
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_edit_bucketphrases, null);
                    dialogBuilder.setView(dialogView);

                    final EditText et_phrase =  dialogView.findViewById(R.id.et_phrase);
                    final Button addPhrase = dialogView.findViewById(R.id.btn_add_phrase);
                    final ListView phrase_lv = dialogView.findViewById(R.id.phrases_lv);
                    notes = user.getNotes();
                    if(notes == null){
                        notes = new ArrayList<>();
                        user.setNotes(notes);
                    }
                    final CustomNoteAdapter customNoteAdapter = new CustomNoteAdapter(getContext(),R.layout.dialog_note,user.getNotes(),user);
                    phrase_lv.setAdapter(customNoteAdapter);
                    final AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    addPhrase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(et_phrase.getText().toString().length() == 0){
                                Toast.makeText(getContext(),"Please enter a phrase",Toast.LENGTH_LONG).show();
                            }
                            else {
                                HashMap<String,ArrayList<Note>> hash = new HashMap<>();
                                Calendar c = Calendar.getInstance();
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);
                                Note n = new Note();
                                n.setText(et_phrase.getText().toString());
                                n.setDate(month,day,year);
                                n.setTime(getCurrentTime());
                                notes.add(n);
                                hash.put("notes",notes);
                                usersRef.child(currentUser).child(user.getName().replace(" ","")).updateChildren( (HashMap) hash);
                                Toast.makeText(getContext(),"Added phrase to user",Toast.LENGTH_LONG).show();
                                customNoteAdapter.notifyDataSetChanged();
                                et_phrase.setText("");
                            }
                        }
                    });

                    dialogBuilder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                }
            });
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        if(search != null){
            search.setText("");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    public class RecordingUtil {
        String length;
        int numSamples;
        int recordingPosition;
        String recordingDate;

        public int getRecordingPosition() {
            return recordingPosition;
        }

        public void setRecordingPosition(int recordingPosition) {
            this.recordingPosition = recordingPosition;
        }

        public String getRecordingDate() {
            return recordingDate;
        }

        public void setRecordingDate(String recordingDate) {
            this.recordingDate = recordingDate;
        }

        public RecordingUtil(int pos){
            recordingPosition = pos;
        }
        public RecordingUtil(String length, int numSamples) {
            this.length = length;
            this.numSamples = numSamples;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public int getNumSamples() {
            return numSamples;
        }

        public void setNumSamples(int numSamples) {
            this.numSamples = numSamples;
        }
    }
    public class CustomRecordingSample extends BaseAdapter {

        ArrayList<RecordingUtil> recordingUtils;
        Spinner langSpinnner;
        Context context;
        User specificUser;
        int res;

        public CustomRecordingSample(ArrayList<RecordingUtil> recordingUtils, Context context, int res, User u) {
            this.recordingUtils = recordingUtils;
            this.context = context;
            this.res = res;
            specificUser = u;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return recordingUtils.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(res,parent,false);
            TextView length = v.findViewById(R.id.recording_length);
            langSpinnner = v.findViewById(R.id.lang_pref);
            final String[] langArray = getResources().getStringArray(R.array.languages);
            int langPosition = 0;
            for (String language: langArray
                 ) {
                try {
                    if (specificUser.getLanguageCode() != null && specificUser.getLanguageCode().equals(language)) {
                        langSpinnner.setSelection(langPosition);
                    }
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                langPosition++;

            }
            //if(specificUser.getLanguageCode())
            langSpinnner.post(new Runnable() {
                public void run() {
                    langSpinnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            //Update the users lang pref settings
                            usersRef.child(currentUser).child(specificUser.getName().replace(" ","")).child("languageCode").setValue(langArray[position]);
                            Toast.makeText(getContext(),"Language Preference Set! "+langArray[position],Toast.LENGTH_SHORT).show();
                            langSpinnner.setSelection(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            });

            if(recordingUtils != null && recordingUtils.size() > 0){
                int numSeconds = (Integer.parseInt(recordingUtils.get(position).getLength())) / 1000;
                //Since we'ere not using a list, just continuously add the length to the first user
               // recordingUtils.get(0).setLength(Integer.parseInt(recordingUtils.get(0).length) + numSeconds + "");
                length.setText("Total length: " + getDurationBreakdown(Long.parseLong(recordingUtils.get(position).getLength())));
                //TextView bucketName = v.findViewById(R.id.recording_bucket_names);
                //TextView bucketDate = v.findViewById(R.id.recording_date);
                //bucketName.setText("Key Phrases Found: "+recordingUtils.get(position).getNumSamples()+"");
                //bucketDate.setText(/*"Conversation Date: "+recordingUtils.get(position).getRecordingDate()*/"");
            }
            else{
                length.setText("No records yet");
            }
            return v;
        }

        public String getDurationBreakdown(long millis) {
            if(millis < 0) {
                throw new IllegalArgumentException("Duration must be greater than zero!");
            }

            long days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

            StringBuilder sb = new StringBuilder(64);
            //sb.append(days);
            //sb.append(" Days ");
            //sb.append(hours);
            //sb.append(" Hours ");
            sb.append(minutes);
            sb.append(" Minutes ");
            sb.append(seconds);
            sb.append(" Seconds");

            return(sb.toString());
        }
    }

    public class CustomDialogPhraseAdapter extends ArrayAdapter{
        ArrayList<String> phrases;
        Context context;
        User chosenUser;
        int res;
        public CustomDialogPhraseAdapter(@NonNull Context context, int resource, @NonNull ArrayList objects,User chosenUser) {
            super(context, resource, objects);
            phrases = objects;
            this.context = context;
            this.chosenUser = chosenUser;
            res = resource;
        }
        @Override
        public int getCount() {
            return phrases.size();
        }

        @Override
        public Object getItem(int position) {
            return phrases.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(res,parent,false);
            TextView phrase = convertView.findViewById(R.id.tv_phrase);
            ImageView delete_phrase = convertView.findViewById(R.id.iv_delete_phrase);
            phrase.setText(phrases.get(position));
            return convertView;
        }
    }

    public class CustomNoteAdapter extends ArrayAdapter{
        ArrayList<Note> notesList;
        Context context;
        User chosenUser;
        int res;
        public CustomNoteAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Note> objects,User chosenUser) {
            super(context, resource, objects);
            notesList = objects;
            this.context = context;
            this.chosenUser = chosenUser;
            res = resource;
        }
        @Override
        public int getCount() {
            return notesList.size();
        }

        @Override
        public Object getItem(int position) {
            return notesList.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(res,parent,false);
            TextView noteText = convertView.findViewById(R.id.note_txt);
            TextView noteTime = convertView.findViewById(R.id.note_time);
            TextView noteDate = convertView.findViewById(R.id.note_date);
            ImageView noteDelete = convertView.findViewById(R.id.note_delete);
            Note n = notesList.get(position);
            noteText.setText(n.getText());
            noteTime.setText(n.getTime());
            noteDate.setText(n.getDate());
            return convertView;
        }
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

    public String getCurrentTime(){
        Calendar currentDateTime = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm a");
        String  currentTime = df.format(currentDateTime.getTime());
        return currentTime;
    }

    public void saveVideoURLPathUser(String videoStoragePath, String videoDropboxPath){

    }



}
