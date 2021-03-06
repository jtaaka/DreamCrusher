package com.example.juho.dreamcrusher;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private SortedSet<Integer> lottoNumbers;
    private TreeSet<Object> computerLotto;
    private Intent intent;
    private Intent toLottoService;
    private LocalBroadcastManager manager;
    private Button startButton;
    private TextView timeTextView;
    private int skillLevel = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, LottoService.class);
        toLottoService = new Intent("lottoService");
        manager = LocalBroadcastManager.getInstance(this);

        timeTextView = findViewById(R.id.textView);

        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);

        lottoNumbers = new TreeSet<>();

        manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mMessageReceiver, new IntentFilter("lotto"));

        Toast toast = Toast.makeText(this,"Select 7 numbers", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double years = intent.getDoubleExtra("years", 0);
            int weeks = intent.getIntExtra("weeks", 0);
            boolean userWon = intent.getBooleanExtra("userWon", false);

            if (userWon) {
                userWonAlert();
            }

            Serializable serializable = intent.getSerializableExtra("numbers");
            Object[] object = (Object[]) serializable;

            computerLotto = new TreeSet<>();

            if (object != null) {
                computerLotto.addAll(Arrays.asList(object));
                if (weeks != 1) {
                    timeTextView.setText(String.format("%.2f years (= %d weeks) have passed", years, weeks));
                } else {
                    timeTextView.setText(String.format("%.2f years (= %d week) have passed", years, weeks));
                }
            }

            animateButtons();
        }
    };

    public void animateButtons() {
        for (int i = 1; i < 41; i++) {
            if (computerLotto.contains(i)) {
                String buttonID = "button" + i;

                ValueAnimator animator = ValueAnimator.ofFloat(0.2f, 1f);
                animator.addUpdateListener(animation -> getButtonID(buttonID).setAlpha((Float) animation.getAnimatedValue()));

                animator.setDuration(300);
                animator.start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.plus:
                setCalculationSpeed(10L, "Speed increased");
                return true;

            case R.id.minus:
                setCalculationSpeed(3000L, "Speed decreased");
                return true;

            case R.id.level4:
                setSkillLevel(4);
                return true;

            case R.id.level5:
                setSkillLevel(5);
                return true;

            case R.id.level6:
                setSkillLevel(6);
                return true;

            case R.id.level7:
                setSkillLevel(7);
                return true;
        }

        return false;
    }

    public void setCalculationSpeed(long value, String message) {
        toLottoService.putExtra("calculationSpeed", value);
        manager.sendBroadcast(toLottoService);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void setSkillLevel(int value) {
        this.skillLevel = value;
        toLottoService.putExtra("skillLevel", skillLevel);
        manager.sendBroadcast(toLottoService);
        Toast.makeText(this, "Get " + value + " correct numbers", Toast.LENGTH_SHORT).show();
    }

    public void startGame(View view) {
        startButton.setEnabled(false);
        intent.putExtra("skillLevel", skillLevel);

        int index = 0;

        for (int number : lottoNumbers) {
            intent.putExtra("lottoNumbers" + index, number);
            index++;
        }

        this.startService(intent);
    }

    int numbersClicked;

    public void resetGame(View view) {
        Intent in = new Intent("lottoService");
        in.putExtra("stopService", true);
        manager.sendBroadcast(in);
        numbersClicked = 0;
        startButton.setEnabled(false);

        for (int buttonNum : lottoNumbers) {
            getButtonID("button" + buttonNum).getBackground().clearColorFilter();
        }

        lottoNumbers.clear();
    }

    public View getButtonID(String buttonID) {
        int id = getResources().getIdentifier(buttonID, "id", getPackageName());

        return findViewById(id);
    }

    public void userWonAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You won the jackpot!");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialog, id) -> dialog.dismiss());
        builder.setNegativeButton("Really?", (dialog, id) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void buttonOnClick(View view) {
        Button clickedButton = (Button) view;
        int lottoNumber = Integer.valueOf(clickedButton.getText().toString());

        if (!lottoNumbers.contains(lottoNumber) && numbersClicked <= 7) {
            numbersClicked++;
        }

        if (numbersClicked <= 7) {
            clickedButton.getBackground().setColorFilter(getResources().getColor(R.color.blue),
                    PorterDuff.Mode.MULTIPLY);
            lottoNumbers.add(lottoNumber);
        }

        if (numbersClicked == 7) {
            startButton.setEnabled(true);
        }
    }
}
