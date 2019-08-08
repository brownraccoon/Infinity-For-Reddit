package Account;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Query("SELECT * FROM accounts WHERE is_current_user = 0")
    List<Account> getAllNonCurrentAccounts();

    @Query("UPDATE accounts SET is_current_user = 0 WHERE is_current_user = 1")
    void markAllAccountsNonCurrent();

    @Query("DELETE FROM accounts")
    void deleteAllAccounts();

    @Query("SELECT * FROM accounts WHERE username = :username COLLATE NOCASE LIMIT 1")
    LiveData<Account> getAccountLiveData(String username);

    @Query("SELECT * FROM accounts WHERE username = :username COLLATE NOCASE LIMIT 1")
    Account getAccountData(String username);

    @Query("SELECT * FROM accounts WHERE is_current_user = 1 LIMIT 1")
    Account getCurrentAccount();

    @Query("UPDATE accounts SET profile_image_url = :profileImageUrl, banner_image_url = :bannerImageUrl, " +
            "karma = :karma WHERE username = :username")
    void updateAccountInfo(String username, String profileImageUrl, String bannerImageUrl, int karma);

    @Query("SELECT * FROM accounts WHERE username != :username")
    LiveData<List<Account>> getAccountsExceptCurrentAccountLiveData(String username);
}
