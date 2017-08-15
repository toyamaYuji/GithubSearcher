package com.github.intern.yuji.githubsearcher.viewmodel;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.ObservableInt;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.github.intern.yuji.githubsearcher.contract.MainActivityContract;
import com.github.intern.yuji.githubsearcher.model.GithubRepository;
import com.github.intern.yuji.githubsearcher.model.GithubService;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by nns on 2017/08/15.
 */
@BindingMethods({
        @BindingMethod(type = SearchView.class, attribute = "android:onQueryTextSubmit", method = "setOnQueryTextListener"),
        @BindingMethod(type = SearchView.class, attribute = "android:onQueryTextChange", method = "setOnQueryTextListener")})
public class MainActivityViewModel {
    public final ObservableInt progressBarVisibility = new ObservableInt(View.GONE);
    private GithubService service;
    private MainActivityContract contract;

    public MainActivityViewModel(MainActivityContract contract, GithubService service) {
        this.contract = contract;
        this.service = service;

        loadRepositories("android");
    }

    private void loadRepositories(String keywords) {
        progressBarVisibility.set(View.VISIBLE);
        // 作成日のフォーマット
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String text = DateFormat.format("yyyy-MM-dd", calendar).toString();
        // 非同期通信でリポジトリを取得
        Observable<GithubRepository> repos = service.getRepos(keywords + "+" + "created:>" + text);
        repos
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GithubRepository>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull GithubRepository githubRepository) {
                        progressBarVisibility.set(View.GONE);
                        Log.d("hoge", githubRepository.toString());
                        contract.showRepository(githubRepository);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        contract.showError("リポジトリを取得できませんでした");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public boolean onQuerySubmit(String query) {
        loadRepositories(query);
        return true;
    }

    public boolean onQueryChange(String next) {
        return false;
    }
}
