package nz.ac.aut.dms.reminder;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import nz.ac.aut.dms.reminder.bt.ReceivedThread;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MonthLoader.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    private List<WeekViewEvent> events;
    private List<String> eventId;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        ServerConnectThread sct = new ServerConnectThread(BluetoothAdapter.getDefaultAdapter(), ShareBT.UUID);
        sct.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        final TextView userNameTV = (TextView) header.findViewById(R.id.username_header);
        final TextView emailTV = (TextView) header.findViewById(R.id.email_header);

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(mAuth.getCurrentUser().getUid());

        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        String displayName = dataSnapshot.child("display").getValue().toString();
                        String disPlayEmail = dataSnapshot.child("email").getValue().toString();
                        userNameTV.setText(displayName);
                        emailTV.setText(disPlayEmail);
                        // ...
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        events = new ArrayList<>();
        eventId = new ArrayList<>();


        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        mWeekView.setShowNowLine(true);

        mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        DatabaseReference calendarRef = database.getReference(mAuth.getCurrentUser().getUid()).child("Schedule");

        calendarRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // Get calendar value
                        pd = new ProgressDialog(MainActivity.this);
                        pd.setMessage("updating calendar");
                        pd.show();
                        events.clear();
                        int i = 1;
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                            eventId.add(postSnapshot.getKey());

                            String title = postSnapshot.child("title").getValue().toString();

                            Calendar startCal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                            try {
                                startCal.setTime(sdf.parse(postSnapshot.child("startDate").getValue().toString() + " " + postSnapshot.child("startTime").getValue().toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Calendar endCal = Calendar.getInstance();
                            try {
                                endCal.setTime(sdf.parse(postSnapshot.child("endDate").getValue().toString() + " " + postSnapshot.child("endTime").getValue().toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            WeekViewEvent weekViewEvent = new WeekViewEvent(i, title, startCal, endCal);
                            events.add(weekViewEvent);
                            i++;
                        }
                        mWeekView.notifyDatasetChanged();
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_today:
                mWeekView.goToToday();
                break;
            case R.id.nav_one:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);
                    Calendar c = Calendar.getInstance();
                    mWeekView.goToHour(c.get(Calendar.HOUR_OF_DAY));

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                break;
            case R.id.nav_week:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);
                    Calendar c = Calendar.getInstance();
                    mWeekView.goToHour(c.get(Calendar.HOUR_OF_DAY));

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                }
                break;
            case R.id.nav_three:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);
                    Calendar c = Calendar.getInstance();
                    mWeekView.goToHour(c.get(Calendar.HOUR_OF_DAY));

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                break;

            case R.id.nav_logout:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                mAuth.signOut();
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Intent intent = new Intent(MainActivity.this, ViewEventActivity.class);
        intent.putExtra("id", eventId.get((int) event.getId() - 1));
        startActivity(intent);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> change = new ArrayList<>();
        for (WeekViewEvent ev : events) {
            if (checkDate(ev, newMonth, newYear)) {
                change.add(ev);
            }
        }
        return change;
    }

    private boolean checkDate(WeekViewEvent ev, int newMonth, int newYear) {
        return (ev.getStartTime().get(Calendar.MONTH) == newMonth - 1) && ev.getStartTime().get(Calendar.YEAR) == (newYear) &&
                (ev.getEndTime().get(Calendar.MONTH) == newMonth - 1) && ev.getEndTime().get(Calendar.YEAR) == (newYear);
    }

    public class ServerConnectThread extends Thread {
        private BluetoothSocket bTSocket;
        private BluetoothAdapter adapter;
        private UUID mUUID;

        public ServerConnectThread(BluetoothAdapter bTAdapter, UUID mUUID) {
            this.adapter = bTAdapter;
            this.mUUID = mUUID;
        }

        @Override
        public void run() {
            acceptConnect(adapter, mUUID);
        }

        public void acceptConnect(BluetoothAdapter bTAdapter, UUID mUUID) {
            BluetoothServerSocket temp = null;
            try {
                temp = bTAdapter.listenUsingRfcommWithServiceRecord("Service_Name", mUUID);
            } catch (IOException e) {
                Log.d("SERVERCONNECT", "Could not get a BluetoothServerSocket:" + e.toString());
            }
            while (true) {
                try {
                    bTSocket = temp.accept();
                    System.out.println("accept");
                } catch (IOException e) {
                    Log.d("SERVERCONNECT", "Could not accept an incoming connection.");
                    break;
                }
                if (bTSocket != null) {
                    try {
                        temp.close();
                    } catch (IOException e) {
                        Log.d("SERVERCONNECT", "Could not close ServerSocket:" + e.toString());
                    }
                    break;
                }
            }
            if(bTSocket!=null) {
                ReceivedThread receivedThread = new ReceivedThread(bTSocket);
                synchronized (receivedThread) {
                    receivedThread.start();
                }
            }
        }

        public void closeConnect() {
            try {
                bTSocket.close();
            } catch (IOException e) {
                Log.d("SERVERCONNECT", "Could not close connection:" + e.toString());
            }
        }
    }
}
