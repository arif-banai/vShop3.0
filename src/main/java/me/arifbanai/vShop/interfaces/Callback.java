package me.arifbanai.vShop.interfaces;

public interface Callback<T> {
    public void onSuccess(T result);
    public void onFailure(Throwable cause);
}
