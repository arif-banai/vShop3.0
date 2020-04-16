package me.arifbanai.vShop.interfaces;

public interface VShopCallback<T> {
    public void onSuccess(T result);
    public void onFailure(Throwable cause);
}
