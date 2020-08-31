package xyz.lebalex.weatherirk;

public interface Callback<R> {
    void onComplete(R result);
}