package com.example.juho.dreamcrusher;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.TreeSet;

public class LottoService extends IntentService {
    private ArrayList<Integer> playersLotto;
    private TreeSet<Integer> randomLotto;
    private LocalBroadcastManager manager;
    private boolean stopService;
    private double yearsPassed;
    private int weeksPassed;
    private long calculationSpeed;
    private int skillLevel;
    private Intent broadCastIntent;

    public LottoService() {
        super("hello from lotto service");

        playersLotto = new ArrayList<>();
        randomLotto = new TreeSet<>();

        broadCastIntent = new Intent("lotto");

        manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mMessageReceiver, new IntentFilter("lottoService"));

        setStop(false);
        setCalculationSpeed(2000);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            calculationSpeed = intent.getLongExtra("calculationSpeed", 2000);
            setCalculationSpeed(calculationSpeed);

            stopService = intent.getBooleanExtra("stopService", false);
            setStop(stopService);

            skillLevel = intent.getIntExtra("skillLevel", 7);
            setSkillLevel(skillLevel);
        }
    };

    public void setYears(double years) {
        this.yearsPassed = years;
    }

    public double getYears() {
        return this.yearsPassed;
    }

    public void setCalculationSpeed(long speed) {
        this.calculationSpeed = speed;
    }

    public long getCalculationSpeed() {
        return this.calculationSpeed;
    }

    public void setStop(boolean stop) {
        this.stopService = stop;
    }

    public boolean getStop() {
        return this.stopService;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int getSkillLevel() {
        return this.skillLevel;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        weeksPassed = 0;
        setSkillLevel(intent != null ? intent.getIntExtra("skillLevel", 7) : 0);
        calculateLotto(intent);
    }

    public void calculateLotto(Intent intent) {
        while (!getStop()) {
            try {
                Thread.sleep(getCalculationSpeed());

                addComputerLotto();
                addPlayerLotto(intent);

                int correctNumbers = 0;

                for (int i = 0; i < 7; i++) {
                    if (randomLotto.contains(playersLotto.get(i))) {
                        correctNumbers++;
                    }

                    if (correctNumbers == getSkillLevel()) {
                        broadCastIntent.putExtra("userWon", true);
                        setStop(true);
                    }
                }

                weeksPassed++;
                yearsPassed = yearsPassed + 0.0191653649;   // 1 week = 0.0191653649 years
                setYears(yearsPassed);
                sendBroadCast(weeksPassed);

                randomLotto.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBroadCast(int weeksPassed) {
        broadCastIntent.putExtra("years", getYears());
        broadCastIntent.putExtra("weeks", weeksPassed);
        broadCastIntent.putExtra("numbers", randomLotto.toArray());
        manager.sendBroadcast(broadCastIntent);
    }

    public void addComputerLotto() {
        while (randomLotto.size() < 7) {
            randomLotto.add((int) (Math.random() * ((40 - 1) + 1)) + 1);
        }
    }

    public void addPlayerLotto(Intent intent) {
        for (int i = 0; i < 7; i++) {
            int lottoNumber = intent.getIntExtra("lottoNumbers" + i, 0);
            playersLotto.add(lottoNumber);
        }
    }
}
