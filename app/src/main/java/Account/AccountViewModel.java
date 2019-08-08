package Account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository mAccountRepository;
    private LiveData<Account> mAccountLiveData;
    private LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;

    public AccountViewModel(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String id) {
        super(application);
        mAccountRepository = new AccountRepository(redditDataRoomDatabase, id);
        mAccountLiveData = mAccountRepository.getAccountLiveData();
        mAccountsExceptCurrentAccountLiveData = mAccountRepository.getAccountsExceptCurrentAccountLiveData();
    }

    public LiveData<Account> getAccountLiveData() {
        return mAccountLiveData;
    }

    public LiveData<List<Account>> getAccountsExceptCurrentAccountLiveData() {
        return mAccountsExceptCurrentAccountLiveData;
    }

    public void insert(Account userData) {
        mAccountRepository.insert(userData);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mUsername;

        public Factory(@NonNull Application application, RedditDataRoomDatabase redditDataRoomDatabase, String username) {
            mApplication = application;
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mUsername = username;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountViewModel(mApplication, mRedditDataRoomDatabase, mUsername);
        }
    }
}
