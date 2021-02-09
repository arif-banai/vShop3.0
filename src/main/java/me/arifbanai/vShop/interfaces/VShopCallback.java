package me.arifbanai.vShop.interfaces;


//TODO Use a default method in the case where we do nothing when onSuccess() is called
public interface VShopCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception cause);
}
