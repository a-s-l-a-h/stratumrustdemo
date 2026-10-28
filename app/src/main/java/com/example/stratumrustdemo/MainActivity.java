package com.example.stratumrustdemo;

public class MainActivity extends com.stratum.runtime.StratumActivity {
    @Override
    protected boolean onImportMain() {
        System.loadLibrary("rustdemo");
        return true;
    }
}