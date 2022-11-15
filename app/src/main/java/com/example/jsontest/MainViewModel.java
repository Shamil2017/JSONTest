package com.example.jsontest;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainViewModel extends AndroidViewModel {

    private static final String BASE_URL = "https://dog.ceo/api/breeds/image/random";
    private static  final String KEY_MESSAGE = "message";
    private static  final String KEY_STATUS = "status";
    private  static final String TAG_ViewModel = "MainViewModel";

    private MutableLiveData<DogImage> dogImage = new MutableLiveData<>();
    private  MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<DogImage> getDocImage()
    {
        return dogImage;
    }

    public void loadImage()
    {
       isLoading.setValue(true);
       Disposable disposable = loadDocImageRx()
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<DogImage>() {
                   @Override
                   public void accept(DogImage image) throws Throwable {
                         isLoading.setValue(false);
                         dogImage.setValue(image);
                   }
               }, new Consumer<Throwable>() {
                   @Override
                   public void accept(Throwable throwable) throws Throwable {
                       isLoading.setValue(false);
                       Log.d(TAG_ViewModel, "Error: "+throwable.getMessage());
                   }
               });
        compositeDisposable.add(disposable);

    }

    private Single<DogImage> loadDocImageRx()
    {
        return Single.fromCallable(new Callable<DogImage>() {
            @Override
            public DogImage call() throws Exception {

                    URL url = new URL(BASE_URL);
                    HttpURLConnection urlOpenConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = urlOpenConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder data = new StringBuilder();
                    String result;
                    do {
                        result = bufferedReader.readLine();
                        if (result!=null )
                        {
                            data.append(result);
                        }

                    } while (result!=null);
                    JSONObject jsonObject = new JSONObject(data.toString());
                    String message = jsonObject.getString(KEY_MESSAGE);
                    String status = jsonObject.getString(KEY_STATUS);
                    return new DogImage(message,status);

            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
