package com.example.tom.fruitable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The MainActivity of my Fruitable App allows users to track how many portions
 * of fruit and vegetables they have eaten in a day. It also tracks how many days in
 * a row they have eaten 5+ portions and displays this as a streak.
 *
 * @author Tom Hughes
 * @version  1.0
 * @since 20-11-2018
 */
public class MainActivity extends AppCompatActivity {

    private int fruitTotal;
    private int vegTotal;
    private int sumTotal() {
        return vegTotal + fruitTotal;
    }
    private int streak;
    public static final String FRUIT = "Fruit";
    public static final String VEG = "Veg";

    ArrayList<String> actions = new ArrayList<>();

    TextView vegTotalDisplay;
    TextView fruitTotalDisplay;
    TextView sumTotalDisplay;
    TextView dateToday;
    TextView streakDisplay;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private final MyDateChangeReceiver mDateReceiver = new MyDateChangeReceiver();

    /**
     * This class extends BroadcastReceiver and  by overriding it's onReceive method
     * it can detect a new day and take the actions required.
     */
    public class MyDateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (newDay()) {
                dailyUpdate();
            }
        }
    }


    /**
     * This method initialises the display and the necessary variables when the app is created.
     *
     * @param savedInstanceState onCreate always takes saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vegTotalDisplay = findViewById(R.id.vegTotalDisplay);
        fruitTotalDisplay = findViewById(R.id.fruitTotalDisplay);
        sumTotalDisplay = findViewById(R.id.sumTotalDisplay);
        dateToday = findViewById(R.id.dateToday);
        streakDisplay = findViewById(R.id.streakDisplay);

        sharedPref = getSharedPreferences("fiveADayStats", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.apply();

        fruitTotal = sharedPref.getInt("fruitTotal", 0);
        vegTotal = sharedPref.getInt("vegTotal", 0);
        streak = sharedPref.getInt("streak", 0);

        createScreen();
    }

    /**
     * This method initially checks if the date has changed and registers a receiver that, while the app is in the foreground,
     * continues to check whether the date has changed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDateReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        if (newDay()) {
            dailyUpdate();
        }
    }

    /**
     * This method is called when the activity is no longer in the foreground and unregisters
     * the receiver that is checking whether the date has changed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDateReceiver);
    }

    /**
     * This method checks if the day has changed
     *
     * @return boolean This returns whether it is a new day or not
     */
    private boolean newDay() {
        Calendar cal = Calendar.getInstance();
        Date now = Calendar.getInstance().getTime();
        cal.setTime(now);

        int thisDay = cal.get(Calendar.DAY_OF_MONTH);
        int thisMonth = cal.get(Calendar.MONTH);
        int thisYear = cal.get(Calendar.YEAR);

        int savedDay = sharedPref.getInt("savedDay", 0);
        int savedMonth = sharedPref.getInt("savedMonth", 0);
        int savedYear = sharedPref.getInt("savedYear", 0);
        return (thisDay != savedDay || thisMonth != savedMonth || thisYear != savedYear);
    }

    /**
     * This method is called at the start of a new day and resets all values to zero.
     * If the sum total is less than 5, the streak broken and reset to 0
     */
    private void dailyUpdate() {
        //streak update
        if (sumTotal() < 5) {
            streak = 0;
        }
        //reset values
        fruitTotal = 0;
        vegTotal = 0;
        actions.clear();
        setDate();

        //display changes and save to memory
        updateHomeScreen();
        saveHomeScreen();
    }

    /**
     * This method initialises all the variables when onCreate is called.
     * If it is the same day as the last time the app was open
     * the values are taken from shared preferences.
     * If the day is different they're reset to 0.
     */
    private void createScreen() {
        if (newDay()) {
            dailyUpdate();
        } else {
            setDate();
            updateHomeScreen();
            saveHomeScreen();
        }

    }

    /**
     * This method is called on the press of the undo button, and removes the last
     * added portion of fruit or veg from the totals. The array list of actions
     * which records the order the fruit and veg was added to the total
     * is viewed and the last action's effects are undone.
     * It is then removed from the list.
     *
     * @param view required as an on click method
     */
    public void undo(View view) {
        if (!actions.isEmpty()) { //if actions is empty there's no action to undo
            reduceTotal(); //knock one off either fruit or veg totals
            updateStreak(sumTotal() + 1, sumTotal()); //check if undo has taken total to <5
            actions.remove(actions.size() - 1); //remove the last action from the array list
        }
    }
    private  void reduceTotal(){
        String action = (actions.get(actions.size() - 1));
        if(action.equals(FRUIT)){
            fruitTotal--;

            fruitTotalDisplay.setText(getString(R.string.display_f_total, fruitTotal));
            sumTotalDisplay.setText((getString(R.string.display_sum_total,sumTotal())));
            editor.putInt("fruitTotal", fruitTotal);
            editor.apply();
        } else if (action.equals(VEG)){
            vegTotal--;

            vegTotalDisplay.setText(getString(R.string.display_v_total, vegTotal));
            sumTotalDisplay.setText((getString(R.string.display_sum_total,sumTotal())));
            editor.putInt("vegTotal", vegTotal);
            editor.apply();
        }
    }
    /**
     * This method is called when the user taps the vegetable button, it
     * adds one to the veg total and sum total , updates the home screen and
     * records the action in the array list.
     *
     * @param view required as an on click method
     */
    public void addVeg(View view) {
        vegTotal++;

        updateStreak(sumTotal() - 1, sumTotal());
        vegTotalDisplay.setText(getString(R.string.display_v_total, vegTotal));
        sumTotalDisplay.setText((getString(R.string.display_sum_total,sumTotal())));

        actions.add(VEG);   //record the action in the array list
        editor.putInt("vegTotal", vegTotal);
        editor.apply();
    }

    /**
     * This method is called when the user taps the fruit button, it
     * adds one to the fruit total and sum total, updates the home screen and
     * records the action in the array list.
     *
     * @param view required as an on click method
     */
    public void addFruit(View view) {
        fruitTotal++;
        updateStreak(sumTotal() - 1, sumTotal());
        fruitTotalDisplay.setText(getString(R.string.display_f_total, fruitTotal));
        sumTotalDisplay.setText((getString(R.string.display_sum_total,sumTotal())));

        actions.add(FRUIT);     //record the action in the array list
        editor.putInt("fruitTotal", fruitTotal);
        editor.apply();
    }

    /**
     * This method finds today's date, formats it and displays it.
     * It also saves a copy of the date in shared preferences
     * for the purpose of detecting a change in the date in newDay.
     */
    private void setDate() {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime(); //getting date
        cal.setTime(today);

        int savedDay = cal.get(Calendar.DAY_OF_MONTH);
        int savedMonth = cal.get(Calendar.MONTH);
        int savedYear = cal.get(Calendar.YEAR);
        editor.putInt("savedDay", savedDay);
        editor.putInt("savedMonth", savedMonth);
        editor.putInt("savedYear", savedYear);
        editor.apply(); //save copy of the date to shared preferences

        SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd/MM", Locale.ENGLISH);
        String date = formatter.format(today); //format date
        dateToday.setText(date); //display
    }

    /**
     * This method checks when 5 or more portions of fruit and veg are eaten and adds 1 to the streak.
     * If the streak is broken it is reset to zero in dailyUpdate.
     *
     * @param prevTotal An integer defining the previous sumTotal
     * @param newTotal  An integer defining the current sumTotal
     */
    private void updateStreak(int prevTotal, int newTotal) {
        if (prevTotal == 4 && newTotal == 5) {
            streak++;   //if it hits five from below => +1
        }
        if (prevTotal == 5 && newTotal == 4) {
            streak--;   //if its four from above => -1
        }
        streakDisplay.setText(getString(R.string.streak, streak));
        editor.putInt("streak", streak);
        editor.apply();
    }
    private void saveHomeScreen(){
        editor.putInt("fruitTotal", fruitTotal);
        editor.putInt("vegTotal", vegTotal);
        editor.putInt("streak", streak);
        editor.apply();
    }
    private void updateHomeScreen(){
        fruitTotalDisplay.setText(getString(R.string.display_f_total, fruitTotal));
        vegTotalDisplay.setText(getString(R.string.display_v_total, vegTotal));
        sumTotalDisplay.setText((getString(R.string.display_sum_total,sumTotal())));
        streakDisplay.setText(getString(R.string.streak, streak));
    }
}
